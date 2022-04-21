package com.example.appen.ui.Home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import Uv
import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.health.TimerStat
import android.preference.PreferenceManager
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.appen.MainActivity
import com.example.appen.R
import com.example.appen.ui.Home.Notification.Companion.hideTimerNotification
import kotlinx.coroutines.Dispatchers
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import okhttp3.Dispatcher
import java.util.prefs.Preferences
import android.app.Notification as Notification
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

    var uvObjekt: Uv? = null


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
            uvObjekt = it
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

    lateinit var tv: TextView

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

        sc.setVisibleYRange(-1f, (yAxisMaxVisible+2).toFloat(), sc.axisRight.axisDependency)
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
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        var main = activity as MainActivity?
        val simpleDateFormat = SimpleDateFormat("HH")
        val currentDateAndTime: String = simpleDateFormat.format(Date())

        tv = simple.findViewById<TextView>(R.id.tvSimple)

        for (i in innUv.properties.timeseries){
            val time = i.time.split("T")
            val clock = time[1].split(":")
            val hour = clock[0]
            if (hour.toInt() == currentDateAndTime.toInt() ){
                //Log.d("Uv for nå", i.toString())
                uvTime = i.data.instant.details.ultraviolet_index_clear_sky.toFloat()
                Log.d("HEI1", tv.text.toString())
                Log.d("HEI2", uvTime.toString())
                innUv.uvTime = uvTime
                tv.text = uvTime.toString()
                break
            }
        }
        homeViewModel._beskyttelsesScore.observe(viewLifecycleOwner){
            anbefaling(it)
        }

    }

    fun anbefaling(beskyttelse: Int) {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        when(uvTime){
            in 0.0F..2.0F     -> anbefalSpf(6)


            in 2.0F..4.0F     -> {
                when(beskyttelse){
                    in 0..20 -> anbefalSpf(20)
                    in 20..50 -> anbefalSpf(15)
                    in 50..70 -> anbefalSpf(10)
                    in 70..100 -> anbefalSpf(6)

                }
            }
            in 4.0F..6.0F     -> {
                when(beskyttelse){
                    in 0..20 -> anbefalSpf(30)
                    in 20..50 -> anbefalSpf(20)
                    in 50..70 -> anbefalSpf(15)
                    in 70..100 -> anbefalSpf(10)

                }
            }
            in 6.0F..8.0F    -> {
                when(beskyttelse){
                    in 0..20 -> anbefalSpf(40)
                    in 20..50 -> anbefalSpf(30)
                    in 50..70 -> anbefalSpf(20)
                    in 70..100 -> anbefalSpf(15)

                }
            }
            in 8.0F..11.0F   -> {
                when(beskyttelse){
                    in 0..20 -> anbefalSpf(50)
                    in 20..50 -> anbefalSpf(40)
                    in 50..100 -> anbefalSpf(30)
                }
            }
        }
    }

    fun anbefalSpf(spf: Int){
        tv.text = "Anbefaler Spf " + spf
    }
}

class AdvancedDisplayFragment : Fragment() {

    lateinit var advanced: View
    lateinit var timerObject: Timer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        advanced = inflater.inflate(R.layout.fragment_advanced_display, container, false)
        timerObject = Timer(advanced).settUpTimer(6)       //6 sec

        return advanced
    }

    override fun onPause() {
        super.onPause()

        if (sharedPreferences.getTimeState(advanced.context) == Timer.TimeState.running) {
            val wakeUpTime = timerObject.onPauseStartBackgroundTimer()
        }
        if (timerObject.timeState == Timer.TimeState.running || timerObject.timeState == Timer.TimeState.paused){
            timerObject.saveOnPause()
        }
    }

    override fun onResume() {
        super.onResume()
        timerObject.initTimer()
        Timer.removeAlarm(advanced.context)
    }
}

//TEST BRANCH

class sharedPreferencesUser() {
    companion object {

        const val sliderId = "com.example.appen.ui.home.sliderValue"

        fun getSliderValue(context: Context): Int {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            return pref.getInt(sliderId, 0)
        }

        fun setSliderValue(sliderValue: Int, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(sliderId, sliderValue)
            editor.apply()
        }

        var toogleId = "com.example.appen.ui.home.sliderValue"

        fun getTooglesValue(context: Context, id: Int ): Boolean {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            toogleId += id.toString()
            val returnValue: Boolean = pref.getBoolean(toogleId, false)
            Log.d("toogle", toogleId)
            Log.d("verdi", returnValue.toString())
            toogleId = toogleId.substring(0, toogleId.length - 1)
            return returnValue
        }

        fun setTooglesValue(verdi: Boolean, id: Int, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            toogleId += id.toString()
            editor.putBoolean(toogleId, verdi)
            toogleId = toogleId.substring(0, toogleId.length - 1)
            editor.apply()
        }
    }
}