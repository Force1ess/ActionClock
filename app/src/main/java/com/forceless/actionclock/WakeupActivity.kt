package com.forceless.actionclock

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.forceless.actionclock.databinding.AddClockBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class WakeupActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private lateinit var path:String
    private lateinit var class_id:String
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewBinding : AddClockBinding
    private var stoped = false


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
                val outputTensor = module.forward(IValue.from(mInputTensor)).toTensor()
                val TOP_K = 10
                val scores = outputTensor.dataAsFloatArray
                val ixs: IntArray = Utils.topK(scores, TOP_K)
                CoroutineScope(Dispatchers.IO).launch {
                    delay(500-(System.currentTimeMillis()-timestamp))
                    image.close()
                }
                listener(ixs)
            }
            catch(ex:Exception){
                Log.i("ActionClock",ex.toString())
            }
        }
    }

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
                Log.d("ActionClock",exc.toString())
            }
            val path = Utils.assetFilePath(this,"model.ptl")
            val Inferece = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(224,224))
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ClassificationAnalyzer({ top->
                        viewBinding.editTextTextPersonName.post {
                            viewBinding.editTextTextPersonName.text = context.getString(R.string.class_result,top[0].toString(),
                                ImageNetClasses.IMAGENET_CLASSES[top[0]])
                            viewBinding.editTextTextPersonName2.text=context.getString(R.string.class_result,top[1].toString(),
                                ImageNetClasses.IMAGENET_CLASSES[top[1]])
                            viewBinding.editTextTextPersonName3.text=context.getString(R.string.class_result,top[2].toString(),
                                ImageNetClasses.IMAGENET_CLASSES[top[2]])
                            for(id in top){
                                if(id==class_id.toInt()){
                                    stoped = true
                                    context.stopService(Intent(this@WakeupActivity,MusicPlay::class.java))
                                    CoroutineScope(Dispatchers.IO).launch {
                                        delay(500)
                                        finishAffinity()
                                        Log.d("ActionClock"+"Alarm"," Responded")
                                    }
                                }
                            }
                        }
                    }, path!!))
                }
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, Inferece)
        }, ContextCompat.getMainExecutor(context))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        path = Utils.assetFilePath(this,"model.ptl")!!
        val musicintent = Intent(this,MusicPlay::class.java)
        musicintent.putExtra("path",Utils.assetFilePath(this,"ring.mp3")!!)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar!!.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        startService(musicintent)
        startForegroundService(musicintent)

        class_id = intent.getStringExtra("class")!!
        viewBinding = AddClockBinding.inflate(layoutInflater)
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera(this)
        setContentView(viewBinding.root)

        viewBinding.autoCompleteLayout.setPadding(0,100,0,0)
        viewBinding.button.visibility = View.INVISIBLE
        viewBinding.autoCompleteTextView.isEnabled = false
        viewBinding.autoCompleteTextView.setTextColor(Color.BLACK)
        viewBinding.autoCompleteTextView.textSize = 24F
        viewBinding.autoCompleteTextView.setText("You need find a ${ImageNetClasses.IMAGENET_CLASSES[class_id.toInt()].split(',')[0]}")
        viewBinding.autoCompleteTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        viewBinding.autoCompleteTextView.typeface = Typeface.DEFAULT_BOLD
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!stoped){
            val broadcastIntent = Intent()
            broadcastIntent.action = "com.forceless.alarm"
            broadcastIntent.setClass(this,ClockReceiver::class.java)
            this.sendBroadcast(broadcastIntent)
        }
    }
}