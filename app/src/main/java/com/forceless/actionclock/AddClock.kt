package com.forceless.actionclock

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.forceless.actionclock.ImageNetClasses.IMAGENET_CLASSES
import com.forceless.actionclock.databinding.AddClockBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.*
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias inferlistener = (top: IntArray) -> Unit

private class ClassificationAnalyzer(private val listener: inferlistener, val path:String) : ImageAnalysis.Analyzer {
    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        try{
        val timestamp = System.currentTimeMillis()
        val mInputTensorBuffer =
            Tensor.allocateFloatBuffer(3 * 224 * 224)
        val mInputTensor = Tensor.fromBlob(
            mInputTensorBuffer,
            longArrayOf(1, 3, 224, 224)
        )

        TensorImageUtils.imageYUV420CenterCropToFloatBuffer(
            image.image, 0,224,224,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB,
            mInputTensorBuffer, 0)

        val module = LiteModuleLoader.load(path)
        val outputTensor = module.forward(IValue.from(mInputTensor)).toTensor();
        val TOP_K=3
        val scores = outputTensor.dataAsFloatArray
        val ixs: IntArray = Utils.topK(scores, TOP_K)
            CoroutineScope(Dispatchers.IO).launch {
                delay(500-(System.currentTimeMillis()-timestamp))
                image.close()
            }
        listener(ixs)
        }
        catch(ex:Exception){
            Log.i("fuck",ex.toString())
        }
    }
}

class add_clock : Fragment() {
    var hour:String? = null
    var minute:String? = null
    private lateinit var module:Module
    private var imageCapture: ImageCapture? = null
    private lateinit var viewBinding: AddClockBinding
    private lateinit var cameraExecutor: ExecutorService
    private val TAG = "CameraXAPP"
    private lateinit var dao: ClockDao

    private fun startCamera(context: Context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder().build()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)
            } catch(exc: Exception) {
                Log.d("fuck",exc.toString())
            }
            val path = assetFilePath(requireContext(),"model.ptl")
            val Inferece = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(224,224))
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ClassificationAnalyzer({ top->
                        viewBinding.editTextTextPersonName.post {
                            viewBinding.editTextTextPersonName.text = context.getString(R.string.class_result,top[0].toString(),
                                IMAGENET_CLASSES[top[0]])
                            viewBinding.editTextTextPersonName2.text=context.getString(R.string.class_result,top[1].toString(),
                                IMAGENET_CLASSES[top[1]])
                            viewBinding.editTextTextPersonName3.text=context.getString(R.string.class_result,top[2].toString(),
                                IMAGENET_CLASSES[top[2]])
                        }
                    }, path!!))
                }
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, Inferece)
        }, ContextCompat.getMainExecutor(context))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.add_clock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = AddClockBinding.bind(view)
        val time = Calendar.getInstance()
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("change clock time")
            .setHour(time.get(Calendar.HOUR_OF_DAY))
            .setMinute(time.get(Calendar.MINUTE))
            .build()
        picker.show(this.parentFragmentManager, "tag")
        picker.addOnPositiveButtonClickListener {
            var reshour=  picker.hour.toString()
            if(reshour.length<2){
                reshour="0"+reshour
            }
            var resminute =  picker.minute.toString()
            if(resminute.length<2){
                resminute="0"+resminute
            }
            this.hour = reshour
            this.minute = resminute
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera(requireContext())
        this.dao = requireContext().let {
            Room.databaseBuilder(it, ClockDB::class.java, "Clock")
                .build()
                .clockDao()
        }
        viewBinding.button.setOnClickListener {
            val class_id = viewBinding.autoCompleteTextView.text.toString().toInt()
                if (class_id>=0&&class_id<1000){
                    val clock = Clock(null,this.hour.toString(),this.minute.toString(), arrayListOf(class_id.toString()), true)
                    MainScope().launch(Dispatchers.IO) {
                        dao.insert(clock)
                        AlarmManager.updateAlarm()
                    }
                }
                else{
                    Toast.makeText(requireContext(), "Failed:Class Not Found", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun assetFilePath(context: Context, assetName: String?): String? {
        val file = File(context.filesDir, assetName!!)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }
}