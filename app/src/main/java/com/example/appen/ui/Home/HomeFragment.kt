package com.example.appen.ui.Home

import Uv
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.appen.MainActivity
import com.example.appen.R
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment() {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var demoCollectionAdapter: HomeCollectionAdapter
    private lateinit var viewPager: ViewPager2
    var uvTime: Float = 0.0F
    lateinit var tvBinding: View



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat("uv", uvTime)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tvBinding = inflater.inflate(R.layout.fragment_simple_display, container, false)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var main = activity as MainActivity?
        demoCollectionAdapter = HomeCollectionAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = demoCollectionAdapter
        val tv = tvBinding.findViewById<TextView>(R.id.tvSimple)
        val activity: Activity? = activity
        if (activity is MainActivity) {
            main = activity
            if(tv == null) {
                Log.d("HEIDU", "JA")
            }
        }

        main?.getMet()?.getUvPaaSted()?.observe(main){
            demoCollectionAdapter.update(it, main)
        }


    }
}

class HomeCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    private var uvobjekt: Uv? = null
    private var simple = SimpleDisplayFragment(uvobjekt)
    private var advanced = AdvancedDisplayFragment(uvobjekt)

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        if (position==0) {
            simple = SimpleDisplayFragment(uvobjekt)
            return simple
        }
        else {
            advanced = AdvancedDisplayFragment(uvobjekt)
            return advanced
        }
    }

    fun update(innUv : Uv, innMain : MainActivity){
        val fragments: List<Fragment> = innMain.supportFragmentManager.fragments
        for (frag in fragments) {
            if (frag.isVisible){
                val homeFragments : List<Fragment> = frag.childFragmentManager.fragments
                for (home in homeFragments){
                    if (home.isVisible){
                        val display: List<Fragment> = home.childFragmentManager.fragments
                        for (disp in display){
                            if (disp.isVisible){
                                if (disp == simple){
                                    val simpleDisp = disp as SimpleDisplayFragment
                                    uvobjekt = innUv
                                    Log.d("UpdateUI", uvobjekt!!.properties.timeseries.toString())
                                    simpleDisp.updateUi(innUv)
                                } else if (disp == advanced){
                                    val advancedDisp = disp as AdvancedDisplayFragment
                                    uvobjekt = innUv
                                    advancedDisp.updateUi(innUv)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Instances of this class are fragments representing a single
// object in our collection.
class SimpleDisplayFragment(uvobjekt: Uv?) : Fragment() {

    lateinit var simple : View
    var uvTime: Float = 0.0F

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        simple = inflater.inflate(R.layout.fragment_simple_display, container, false)
        return simple
    }

    fun updateUi (innUv : Uv){
        val simpleDateFormat = SimpleDateFormat("HH")
        val currentDateAndTime: String = simpleDateFormat.format(Date())

        val tv = simple.findViewById<TextView>(R.id.tvSimple)

        for (i in innUv.properties.timeseries){
            val time = i.time.split("T")
            val clock = time[1].split(":")
            val hour = clock[0]
            if (hour.toInt() == currentDateAndTime.toInt() ){
                //Log.d("Uv for nå", i.toString())
                uvTime = i.data.instant.details.ultraviolet_index_clear_sky.toFloat()
                Log.d("HEI1", tv.text.toString())
                Log.d("HEI2", uvTime.toString())
                tv.text = uvTime.toString()
                break
            }
        }
    }
}

class AdvancedDisplayFragment(uvobjekt: Uv?) : Fragment() {
    lateinit var advanced : View
    lateinit var sc: ScatterChart
    lateinit var scatterdata: ScatterData
    private var next12Hours = ArrayList<Int>()
    private var entries = ArrayList<BarEntry>()
    private var uvObjekt = uvobjekt

    private class MyFormatter(n12h: ArrayList<Int>): ValueFormatter(){
        var next12hours = n12h

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {

            return "${next12hours[value.toInt()]}"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        advanced = inflater.inflate(R.layout.fragment_advanced_display, container, false)
        return advanced
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sc = advanced.findViewById(R.id.SCchart)
        updatePlot()
    }

    fun updatePlot (){
        addEntries()
        sc.setDrawGridBackground(true)
        sc.setGridBackgroundColor(Color.BLACK)
        sc.setNoDataText("Loading...")

        //Scrolling
        sc.setTouchEnabled(true)
        sc.isDragEnabled = true
        sc.setScaleEnabled(false)
        sc.setPinchZoom(false)

        //Fjerner streker bak grafen, så den er blank
        sc.xAxis.setDrawGridLines(false)
        sc.axisLeft.setDrawGridLines(false)
        sc.axisRight.setDrawGridLines(false)
        sc.axisLeft.setDrawLabels(false)
        sc.axisRight.setDrawLabels(false)

        //Sett str på text på x aksen
        sc.xAxis.textSize = 20F
        sc.extraTopOffset = 14F

        sc.description.isEnabled = false
        var scatterDataSet = ScatterDataSet(entries as List<Entry>?, "")
        scatterdata = ScatterData(scatterDataSet)

        sc.data = scatterdata

        //fjerner flere linjer
        sc.data.isHighlightEnabled = false

        scatterDataSet.valueTextColor = Color.WHITE;
        scatterDataSet.valueTextSize = 18f;
        Log.d("Før Formatter: ", next12Hours.toString())
        sc.xAxis.valueFormatter = MyFormatter(next12Hours)
        //max synlig range?
        //sc.setVisibleXRangeMaximum(6F)
        //sc.setVisibleYRangeMaximum(4F, sc.axisLeft.axisDependency)

        sc.invalidate()
    }

    fun addEntries(){
        // ANTAKELSE: den første timen i timeseries er den vi er på nå
        Log.d("addEntries: ", "Legger til entries")
        val timeseries = uvObjekt!!.properties.timeseries
        for (i in 0..12){
            val time = timeseries[i].time.split("T")
            val hour = time[1].split(":")[0].toFloat()
            val uv = timeseries[i].data.instant.details.ultraviolet_index_clear_sky.toFloat()
            entries.add(BarEntry(i.toFloat(), uv))
            next12Hours.add(hour.toInt())
            Log.d("Added to index: ", next12Hours[i].toString())
        }
    }

    fun updateUi (innUv : Uv){
        uvObjekt = innUv
        //addEntries()
        //sc.invalidate()
        //sc.notifyDataSetChanged()
        //updatePlot()
    }

}