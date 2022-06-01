package com.forceless.actionclock

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.room.Room

class ClockFragment : Fragment() {

    private var columnCount = 1
    private lateinit var dao: ClockDao

    private var onValuePosted: ((clocks: List<Clock>) -> Unit)? = null
    private var values: List<Clock>? = null
        set(value) {
            onValuePosted?.let {
                if (value != null) {
                    it(value)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = requireContext().let {
            Room.databaseBuilder(it, ClockDB::class.java, "Clock")
                .build()
                .clockDao()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AlarmManager.context = requireContext()
        dao.getAll().asLiveData().observe(viewLifecycleOwner) {
            Log.d("ClockFragment", "$it")
            if (it.isNotEmpty())
                this@ClockFragment.values = it
            else
                this@ClockFragment.values = listOf(Clock(0, "12", "00", listOf(), true))
        }
        val view = inflater.inflate(R.layout.clock_list, container, false)
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                onValuePosted = { values ->
                    adapter = MyClockListRecyclerViewAdapter(dao, values, parentFragmentManager)
                }
            }
        }
        return view
    }
}