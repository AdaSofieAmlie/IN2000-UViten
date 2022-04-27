package com.example.appen.ui.Home

import Uv
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class HomeFragment : Fragment() {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    var uvObjekt: Uv? = null
    private lateinit var demoCollectionAdapter: HomeCollectionAdapter
    lateinit var viewPager: ViewPager2
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
        viewPager.isUserInputEnabled = false

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, view.findViewById(R.id.pager)) { tab, position ->
            tab.text = "Tab ${(position + 1)}"
        }.attach()

        val tab = tabLayout.getTabAt(1)
        if (tab != null) {
            tab.select()
        }

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

    override fun getItemCount(): Int = 3

    private var uvobjekt: Uv? = null
    private var info = InfoDisplayFragment()
    private var simple = SimpleDisplayFragment(uvobjekt)
    private var advanced = AdvancedDisplayFragment()

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        if (position==1) {
            simple = SimpleDisplayFragment(uvobjekt)
            return simple
        }
        if (position==2) {
            info = InfoDisplayFragment()
            return info
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
                                    simpleDisp.kalkuler()
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
            // endre ikon her:
            var icon = getDrawable(requireContext(), R.drawable.sunone)
            if (2.5 <= uv && uv <= 5.4) {
                icon = getDrawable(requireContext(), R.drawable.suntwo)
            }else if (5.5 <= uv && uv <= 7.4){
                icon = getDrawable(requireContext(), R.drawable.sunthree)
            } else if (7.5 <= uv && uv <= 10.4){
                icon = getDrawable(requireContext(), R.drawable.sunfour)
            } else if ( 10.5 <= uv){
                icon = getDrawable(requireContext(), R.drawable.sunfive)
            }
            entries.add(BarEntry(i.toFloat(), uv.roundToInt().toFloat()).also { it.icon = icon })
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

        tv = simple.findViewById<TextView>(R.id.tvSimple)

        for (i in innUv.properties.timeseries){
            val time = i.time.split("T")
            val clock = time[1].split(":")
            val hour = clock[0]
            if (hour.toInt() == currentDateAndTime.toInt() ){
                uvTime = i.data.instant.details.ultraviolet_index_clear_sky.toFloat()
                Log.d("Uv for nå", uvTime.toString())
                updateIcons(uvTime)
                Log.d("HEI1", tv.text.toString())
                Log.d("HEI2", uvTime.toString())
                innUv.uvTime = uvTime
                tv.text = uvTime.toString()
                break
            }
        }

    }

    fun kalkuler() {
        var seekBar1ValueRegning = sharedPreferencesUser.getSliderValue(requireContext())
        seekBar1ValueRegning = seekBar1ValueRegning+1
        val seekBar1ValueRegningDouble : Double = seekBar1ValueRegning * 16.67

        var beskyttelseScore = seekBar1ValueRegningDouble.toInt()

        val fjell = sharedPreferencesUser.getTooglesValue(requireContext(), 1)
        val snoo = sharedPreferencesUser.getTooglesValue(requireContext(), 2)
        val sand = sharedPreferencesUser.getTooglesValue(requireContext(), 3)
        val vann = sharedPreferencesUser.getTooglesValue(requireContext(), 4)

        if(fjell) {
            beskyttelseScore -= 10
        }
        if(snoo) {
            beskyttelseScore -= 10
        }
        if(sand) {
            beskyttelseScore -= 10
        }
        if(vann) {
            beskyttelseScore -= 10
        }
        Log.d("noe besky", beskyttelseScore.toString())
        anbefaling(beskyttelseScore)
    }

    fun anbefaling(beskyttelse: Int) {
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
        //Test
    }

    fun updateIcons(uvTime : Float){
        Log.d("noe", uvTime.toString())
        val imgGlasses: ImageView = simple.findViewById(R.id.glassesImg)
        val imgSunscreen: ImageView = simple.findViewById(R.id.sunscreenImg)
        val imgCap: ImageView = simple.findViewById(R.id.capImg)
        val imgClothes: ImageView = simple.findViewById(R.id.clothesImg)
        val imgShade: ImageView = simple.findViewById(R.id.shadeImg)

        imgGlasses.isVisible = true
        imgSunscreen.isVisible = true
        imgCap.isVisible = true
        imgClothes.isVisible = true
        imgShade.isVisible = true

        if (0 <= uvTime && uvTime <= 2.4){
            Log.d("show one icon", "Glasses")
            imgGlasses.isVisible = true
            imgSunscreen.isVisible = false
            imgCap.isVisible = false
            imgClothes.isVisible = false
            imgShade.isVisible = false
        } else if (2.5 <= uvTime && uvTime <= 5.4){
            Log.d("show two icons", "Glasses, Sunscreen")
            imgGlasses.isVisible = true
            imgSunscreen.isVisible = true
            imgCap.isVisible = false
            imgClothes.isVisible = false
            imgShade.isVisible = false
        } else if (5.5 <= uvTime && uvTime <= 7.4){
            Log.d("show three icons", "Glasses, Sunscreen, Cap")
            imgGlasses.isVisible = true
            imgSunscreen.isVisible = true
            imgCap.isVisible = true
            imgClothes.isVisible = false
            imgShade.isVisible = false
        } else if (7.5 <= uvTime && uvTime <= 10.4){
            Log.d("show four icons", "Glasses, Sunscreen, Cap, Clothes")
            imgGlasses.isVisible = true
            imgSunscreen.isVisible = true
            imgCap.isVisible = true
            imgClothes.isVisible = true
            imgShade.isVisible = false
        } else {
            Log.d("show five icons", "Glasses, Sunscreen, Cap, Clothes, Shade")
        }
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
        timerObject = Timer(advanced).settUpTimer(7200)       //2 hours

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
        timerObject.startButtons()
        timerObject.initTimer()
        Timer.removeAlarm(advanced.context)
    }
}

//INFOOOOOOOOOOOO
class InfoDisplayFragment : Fragment() {

    lateinit var info: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        info = inflater.inflate(R.layout.fragment_info_display, container, false)


        return info
    }

    override fun onPause() {
        super.onPause()


    }

    override fun onResume() {
        super.onResume()
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

