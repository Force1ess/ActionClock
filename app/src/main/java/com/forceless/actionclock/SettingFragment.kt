package com.forceless.actionclock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.forceless.actionclock.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSettingBinding.bind(view)
        with(binding.expandableList){
            layoutManager = LinearLayoutManager(context)
            adapter = MyClassItemRecyclerViewAdapter(ImageNetClasses.IMAGENET_CLASSES)
        }
        //FastScrollerBuilder(binding.expandableList).useMd2Style().build()
    }
}