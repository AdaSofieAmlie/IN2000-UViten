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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


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
    private var advanced = AdvancedDisplayFragment()

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        if (position==0) {
            simple = SimpleDisplayFragment(uvobjekt)
            return simple
        }
        else {
            advanced = AdvancedDisplayFragment()
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
                                    simpleDisp.updatePlot(innUv)
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

    private var uvObjekt = uvobjekt
    lateinit var sc: ScatterChart
    lateinit var scatterdata: ScatterData
    private var next12Hours = ArrayList<Int>()
    private var entries = ArrayList<BarEntry>()
    private var yAxisMaxVisible: Int = 0
    private var update = true

    private class XaxisFormatter(n12h: ArrayList<Int>): ValueFormatter(){
        var next12hours = n12h

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return "${next12hours[value.toInt()]}"
        }
    }

    private class YaxisFormatter(): ValueFormatter(){
        override fun getFormattedValue(value: Float): String {
            return value.roundToInt().toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        simple = inflater.inflate(R.layout.fragment_simple_display, container, false)
        return simple
    }

    fun initializePlot (){
        sc = simple.findViewById(R.id.SCchart)
        //# Fjern entries og datasettet før det skal legges til nytt
        if (sc.data != null){
            sc.data.clearValues()
            sc.clear()
        }

        //# Data legges til
        addEntries()
        val scatterDataSet = ScatterDataSet(entries as List<Entry>?, "")
        scatterDataSet.valueTextColor = Color.WHITE;
        scatterDataSet.valueTextSize = 12f;

        scatterdata = ScatterData(scatterDataSet)
        sc.data = scatterdata

        //## Retter opp tider og runder av UV-indeks
        sc.xAxis.valueFormatter = XaxisFormatter(next12Hours)
        sc.data.setValueFormatter(YaxisFormatter())

        //# Visuelle ting
        sc.description.isEnabled = false
        sc.setDrawGridBackground(true)
        sc.setGridBackgroundColor(Color.BLACK)
        sc.xAxis.setDrawGridLines(false)
        sc.axisLeft.setDrawGridLines(false)
        sc.axisRight.setDrawGridLines(false)
        sc.axisLeft.setDrawLabels(false)
        sc.axisRight.setDrawLabels(false)
        sc.data.isHighlightEnabled = false //må skje etter at data er adda
        sc.legend.isEnabled = false
        //## Tekststørrelse på timene øverst i grafen
        sc.xAxis.textSize = 20F
        sc.extraTopOffset = 12F

        sc.setVisibleYRange(-1f, 6f, sc.axisRight.axisDependency)
        sc.setVisibleXRangeMaximum(6f)

        //# Alt som er relatert til touch og scrolling
        sc.setTouchEnabled(true)
        sc.isDragEnabled = true
        sc.setPinchZoom(false)


        //sc.setVisibleXRangeMaximum(5F)
        //sc.fitScreen()
        sc.invalidate()
    }

    fun addEntries(){
        // ANTAKELSE: den første timen i timeseries er den vi er på nå
        Log.d("addEntries: ", "Legger til entries")
        next12Hours.clear()
        val timeseries = uvObjekt!!.properties.timeseries
        for (i in 0..11){
            val time = timeseries[i].time.split("T")
            val hour = time[1].split(":")[0].toFloat()
            val uv = timeseries[i].data.instant.details.ultraviolet_index_clear_sky.toFloat()
            if (uv.roundToInt()>yAxisMaxVisible) yAxisMaxVisible = uv.roundToInt()
            entries.add(BarEntry(i.toFloat(), uv.roundToInt().toFloat()))
            next12Hours.add(hour.toInt())
            Log.d("Added to index: ", next12Hours[i].toString())
        }
    }

    fun updatePlot (innUv : Uv){
        if (!update) return
        update = false
        uvObjekt = innUv
        initializePlot()
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

class AdvancedDisplayFragment() : Fragment() {
    lateinit var advanced : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        advanced = inflater.inflate(R.layout.fragment_advanced_display, container, false)
        return advanced
    }
}