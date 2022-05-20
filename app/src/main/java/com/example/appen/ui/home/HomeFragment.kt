package com.example.appen.ui.home

import com.example.appen.base.Pos
import com.example.appen.base.Uv
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.isVisible
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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

//solvarsel fragment, flere ting skjer her, hvor det er en samling med tre fragments
//
class HomeFragment : Fragment() {

    //binding
    private lateinit var tvBinding: View
    private lateinit var main: MainActivity
    //Collection med fragmentsene (tab sidene)
    private lateinit var demoCollectionAdapter: HomeCollectionAdapter
    private lateinit var viewPager: ViewPager2
    //com.example.appen.base.Uv objektet (data) og uvTime(UV akkurat nå)
    private var uvObjekt: Uv? = null
    private var uvTime: Float = 0.0F


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat("uv", uvTime)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tvBinding = inflater.inflate(R.layout.fragment_solvarsel_display, container, false)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    //Velger riktig tab, inneholder en workaround for å få bruker tilbake til solvarsel taben
    override fun onResume() {
        super.onResume()
        val tabLayout = view?.findViewById<TabLayout>(R.id.tab_layout)
        var tab = tabLayout?.getTabAt(0)
        tab?.select()
        tab = tabLayout?.getTabAt(1)
        tab?.select()
        Log.d("Test HomeFragment onResume()", "Gjennopptar homefragment")
    }

    //Setter opp tabs, med ikoner og gjør klar viewpager for riktig view visning
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        main = activity as MainActivity
        demoCollectionAdapter = HomeCollectionAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = demoCollectionAdapter
        viewPager.isUserInputEnabled = false

        //Definerer tabsa og setter de på
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, view.findViewById(R.id.pager)) { tab, position ->
            tab.text = "Tab ${(position + 1)}"
        }.attach()

        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_baseline_timer_24)
        tabLayout.getTabAt(0)?.tabLabelVisibility = TabLayout.TAB_LABEL_VISIBILITY_UNLABELED
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_round_wb_sun_24)
        tabLayout.getTabAt(1)?.tabLabelVisibility = TabLayout.TAB_LABEL_VISIBILITY_UNLABELED
        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_baseline_info_24)
        tabLayout.getTabAt(2)?.tabLabelVisibility = TabLayout.TAB_LABEL_VISIBILITY_UNLABELED

        tabLayout.getTabAt(1)?.select()

        val tv = tvBinding.findViewById<TextView>(R.id.uvTv)
        val activity: Activity? = activity
        if (activity is MainActivity) {
            main = activity
            if (tv == null) {
                Log.d("Test HomeFragment onViewCreated() activity", "Aktivitet:$activity")
            }
        }
        //observerer om UV er endret på nåværende posisjon
        main.getMet().getUvPaaSted().observe(main) {
            uvObjekt = it
            demoCollectionAdapter.update(it, main)
        }
    }

    //Testmetode
    fun setUvTimeTest(uv: Float) {
        uvTime = uv
    }
}
//Collection som inneholder de tre tab-fragmentsene
class HomeCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3
    private var uvobjekt: Uv? = null
    //De tre fragmentsene som også er tabsa; smørepåminnelse, solvarsel og info
    private var info = InfoFragment()
    private var solVarsel = SolvarselFragment(uvobjekt)
    private var smoerePaaminnelse = SmoerePaaminnelseFragment()

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        if (position==1) {
            solVarsel = SolvarselFragment(uvobjekt)
            return solVarsel
        }
        return if (position==2) {
            info = InfoFragment()
            info
        } else {
            smoerePaaminnelse = SmoerePaaminnelseFragment()
            smoerePaaminnelse
        }
    }

    //Oppdaterer og finner fragments som er i fokus/finnes...
    //...Kaller på update funksjoner
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
                                if (disp == solVarsel){
                                    val simpleDisp = disp as SolvarselFragment
                                    uvobjekt = innUv
                                    Log.d("Test HomeCollection update()", uvobjekt!!.properties.timeseries.toString())
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
class SolvarselFragment(uvobjekt: Uv?) : Fragment() {
    private lateinit var simple : View
    private var uvTime: Float = 0.0F

    //TextViews som viser bruker: Temp, SPF, lokasjon
    private lateinit var tv: TextView
    private lateinit var anbTv: TextView
    private lateinit var tempTv: TextView
    private lateinit var locTv: TextView

    //UV objekt
    private var uvObjekt = uvobjekt
    //Graf
    private lateinit var sc: ScatterChart
    private lateinit var scatterdata: ScatterData
    private var next12Hours = ArrayList<Int>()
    private var entries = ArrayList<BarEntry>()
    private var yAxisMaxVisible: Int = 0
    private var update = true

    //Grafens axis formatters
    private class XaxisFormatter(n12h: ArrayList<Int>): ValueFormatter(){
        var next12hours = n12h

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return "${next12hours[value.toInt()]}"
        }
    }

    //Kan ikke fjerne tom konstruktør / graf vil vises feil
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
        simple = inflater.inflate(R.layout.fragment_solvarsel_display, container, false)
        return simple
    }

    //GeoCode som viser lokasjon/lokasjonsnavn øverst på solvarselsiden
    private suspend fun setLoc(pos: Pos) {
        try {
            val client = HttpClient {
                install(JsonFeature) {
                    acceptContentTypes = acceptContentTypes + ContentType.Any
                }
                install(UserAgent) {
                    agent = "uio.no snorre@wenaas.org"
                }
            }
            val url = URL("https://api.bigdatacloud.net/data/reverse-geocode?latitude="+ pos.lat+ "&longitude=" + pos.lon + "&localityLanguage=no&key=e7e4fdceb8514a458a8dc231f6222030")
            val returnString: String = client.get(url)
            Log.d("Test SolvarselFragment setLoc()", "Geocode Api. I kommentar.") //returnstring
            val geocode = Geocoding.fromJson(returnString)
            if (geocode != null) {
                requireActivity().runOnUiThread {
                    locTv.text = geocode.locality + ", " + geocode.city
                }
            }
        }
        catch (exception: Exception) {
            println("A network request exception was thrown: ${exception.message}")
            requireActivity().runOnUiThread {
                locTv.text = "Fant ikke posisjon..."
            }
        }
    }

    //Initsialiserer grafen og bygger den opp
    private fun initializePlot (){
        sc = simple.findViewById(R.id.SCchart)
        //# Fjern entries og datasettet før det skal legges til nytt
        if (sc.data != null){
            sc.data.clearValues()
            sc.clear()
        }

        //# com.example.appen.base.Data legges til
        addEntries()
        val scatterDataSet = ScatterDataSet(entries as List<Entry>?, "")
        scatterDataSet.valueTextColor = Color.WHITE
        scatterDataSet.valueTextSize = 12f

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

        sc.invalidate()
    }

    //Legger til UV og Ikon på klokkeslett i graf
    private fun addEntries(){
        // ANTAKELSE: den første timen i timeseries er den vi er på nå
        Log.d("Test SolvarselFragment addEntries()", "Legger til entries i graf")
        next12Hours.clear()
        val timeseries = uvObjekt!!.properties.timeseries
        for (i in 0..11){
            val time = timeseries[i].time.split("T")
            val hour = time[1].split(":")[0].toFloat()
            val uv = timeseries[i].data.instant.details.ultraviolet_index_clear_sky.toFloat()
            if (uv.roundToInt()>yAxisMaxVisible) yAxisMaxVisible = uv.roundToInt()
            // endre ikon her:
            var icon = getDrawable(requireContext(), R.drawable.sunone)
            when {
                uv in 2.5..5.4 -> {
                    icon = getDrawable(requireContext(), R.drawable.suntwo)
                }
                uv in 5.5..7.4 -> {
                    icon = getDrawable(requireContext(), R.drawable.sunthree)
                }
                uv in 7.5..10.4 -> {
                    icon = getDrawable(requireContext(), R.drawable.sunfour)
                }
                10.5 <= uv -> {
                    icon = getDrawable(requireContext(), R.drawable.sunfive)
                }
            }
            entries.add(BarEntry(i.toFloat(), uv.roundToInt().toFloat()).also { it.icon = icon })
            next12Hours.add(hour.toInt())
            Log.d("Test SolvarselFragment addEntries()ferdig", next12Hours[i].toString())
        }
    }

    //Oppdaterer Graf med UV
    fun updatePlot (innUv : Uv){
        if (!update) return
        update = false
        uvObjekt = innUv
        initializePlot()
    }

    //Oppdaterer UI elementer slik at de samsvarer med UV
    fun updateUi (innUv : Uv){
        val simpleDateFormat = SimpleDateFormat("HH")
        val currentDateAndTime: String = simpleDateFormat.format(Date())

        tv = simple.findViewById(R.id.uvTv)
        anbTv = simple.findViewById(R.id.anbefaling)
        tempTv = simple.findViewById(R.id.tempTv)
        locTv = simple.findViewById(R.id.posTv)

        //sender med lokasjonsobjektet
        CoroutineScope(Dispatchers.IO).launch {
            val main = activity as MainActivity
            setLoc(main.loc.position)
        }

        for (i in innUv.properties.timeseries){
            val time = i.time.split("T")
            val clock = time[1].split(":")
            val hour = clock[0]
            if (hour.toInt() == currentDateAndTime.toInt() ){
                uvTime = i.data.instant.details.ultraviolet_index_clear_sky.toFloat()
                val tempTime = i.data.instant.details.air_temperature.toFloat()
                Log.d("Test SolvarselFragment updateUi() uvTime", uvTime.toString())
                //oppdaterer ikoner så de stemmer med UV
                updateIcons(uvTime)
                Log.d("Test SolvarselFragment updateUi() uvtTimeTv", tv.text.toString())
                Log.d("Test SolvarselFragment updateUi() uvTime", uvTime.toString())
                innUv.uvTime = uvTime
                tv.text = "\nUV:\n$uvTime"
                tempTv.text = "\nTemp:\n" + tempTime.toString() + "C"
                break
            }
        }
    }

    //Kaller på anbefaling og utfører kalkuleringen basert på profil informasjon
    fun kalkuler() {
        var uvTime2 = uvTime
        var seekBar1ValueRegning = SharedPreferencesUser.getSliderValue(requireContext())
        seekBar1ValueRegning += 1

        val hoydemeter = SharedPreferencesUser.getTooglesValue(requireContext(), 1)
        val snoo2 = SharedPreferencesUser.getTooglesValue(requireContext(), 2)

        if(hoydemeter) {
            uvTime2 += uvTime * 0.5F
        }
        if(snoo2) {
            uvTime2 += uvTime * 0.5F
        }
        Log.d("Test SolvarselFragment kalkuler() uvTime", uvTime.toString())
        Log.d("Test SolvarselFragment kalkuler() uvTimeKalkulert", uvTime2.toString())

        anbefaling(seekBar1ValueRegning, uvTime2, false)
    }
    /*
    anbefaling / algoritme for anbefaling av solkrem
    enkle whens med "in range
    */

    private fun anbefaling(beskyttelse: Int, uvTime2: Float, test: Boolean): Int{
        when(uvTime2){
            in 0.0F..0.3F     -> return anbefalSpf(0, test)
            in 0.3F..3.0F     -> {
                when(beskyttelse){
                    in(1..2) -> return anbefalSpf(30, test)
                    in(3..6) -> return anbefalSpf(0, test)
                }
            }
            in 3.0F..4.0F     -> {
                when(beskyttelse){
                    in(1..3) -> return anbefalSpf(30, test)
                    in(4..6) -> return anbefalSpf(0, test)
                }
            }
            in 4.0F..6.0F    -> {
                when(beskyttelse){
                    1 -> return anbefalSpf(50, test)
                    in(2..4) -> return anbefalSpf(30, test)
                    in(5..6) -> return anbefalSpf(0, test)
                }
            }
            in 6.0F..7.0F   -> {
                when(beskyttelse){
                    in(1..3) -> return anbefalSpf(50, test)
                    4 -> return anbefalSpf(30, test)
                    in(5..6) -> return anbefalSpf(0, test)
                }
            }
            in 7.0F..9.0F   -> {
                when(beskyttelse){
                    in(1..4) -> return anbefalSpf(50, test)
                    5 -> return anbefalSpf(30, test)
                    6 -> return anbefalSpf(0, test)
                }
            }
            in 9.0F..11.0F   -> {
                when(beskyttelse){
                    in(1..5) -> return anbefalSpf(50, test)
                    6 -> return anbefalSpf(30, test)
                }
            }
        }
        return 0
    }
    //kontrollerer SPF text view som viser anbefalt solkrem faktor
    fun anbefalSpf(spf: Int, test: Boolean): Int{
        //tv.text = "Anbefaler Spf " + spf
        if(!test) {
            anbTv.text = spf.toString()
            return 0
        }
        //Test
        return spf
    }

    //Oppdaterer slik at riktige ikoner viser ut ifra nåværende UV
    private fun updateIcons(uvTime : Float){
        Log.d("Test SolvarselFragment updateIcons() uvTime", uvTime.toString())
        val imgGlasses: ImageView = simple.findViewById(R.id.glassesImg)
        val imgSunscreen: ImageView = simple.findViewById(R.id.sunscreenImg)
        val imgCap: ImageView = simple.findViewById(R.id.capImg)
        val imgClothes: ImageView = simple.findViewById(R.id.clothesImg)
        val imgShade: ImageView = simple.findViewById(R.id.shadeImg)

        imgGlasses.isVisible = true
        imgGlasses.setOnClickListener {
            Toast.makeText(requireContext(), R.string.glasses, Toast.LENGTH_LONG).show()
        }
        imgSunscreen.isVisible = true
        imgSunscreen.setOnClickListener {
            Toast.makeText(requireContext(), R.string.sunscreen, Toast.LENGTH_LONG).show()
        }
        imgCap.isVisible = true
        imgCap.setOnClickListener {
            Toast.makeText(requireContext(), R.string.cap, Toast.LENGTH_LONG).show()
        }
        imgClothes.isVisible = true
        imgClothes.setOnClickListener {
            Toast.makeText(requireContext(), R.string.clothes, Toast.LENGTH_LONG).show()
        }
        imgShade.isVisible = true
        imgShade.setOnClickListener {
            Toast.makeText(requireContext(), R.string.shade, Toast.LENGTH_LONG).show()
        }

        when (uvTime) {
            in 0.0..2.4 -> {
                Log.d("Test SolvarselFragment updateIcons() 1ikoner", "Glasses")
                imgGlasses.isVisible = true
                imgSunscreen.isVisible = false
                imgCap.isVisible = false
                imgClothes.isVisible = false
                imgShade.isVisible = false
            }
            in 2.5..5.4 -> {
                Log.d("Test SolvarselFragment updateIcons() 2ikoner", "Glasses, Sunscreen")
                imgGlasses.isVisible = true
                imgSunscreen.isVisible = true
                imgCap.isVisible = false
                imgClothes.isVisible = false
                imgShade.isVisible = false
            }
            in 5.5..7.4 -> {
                Log.d("Test SolvarselFragment updateIcons() 3ikoner", "Glasses, Sunscreen, Cap")
                imgGlasses.isVisible = true
                imgSunscreen.isVisible = true
                imgCap.isVisible = true
                imgClothes.isVisible = false
                imgShade.isVisible = false
            }
            in 7.5..10.4 -> {
                Log.d("Test SolvarselFragment updateIcons() 4ikoner", "Glasses, Sunscreen, Cap, Clothes")
                imgGlasses.isVisible = true
                imgSunscreen.isVisible = true
                imgCap.isVisible = true
                imgClothes.isVisible = true
                imgShade.isVisible = false
            }
            else -> {
                Log.d("Test SolvarselFragment updateIcons() 5ikoner", "Glasses, Sunscreen, Cap, Clothes, Shade")
            }
        }
    }
}
//Timer taben / her vises timer
class SmoerePaaminnelseFragment : Fragment() {

    private lateinit var advanced: View
    private lateinit var timerObject: Timer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        advanced = inflater.inflate(R.layout.fragment_smoerepaaminnelse_display, container, false)
        timerObject = Timer(advanced).settUpTimer(7)       //2 hours = 7200
        Log.d("Test SmoerePaaminnelseFragment onCreateView()", "Bygget SmoerePaaminnelse")
        return advanced
    }
    //onPause for timer
    override fun onPause() {
        super.onPause()
        Log.d("Test SmoerePaaminnelseFragment onPause()", "Pauset SmoerePaaminnelse")
        if (SharedPreferences.getTimeState(advanced.context) == Timer.TimeState.Running) {
            timerObject.onPauseStartBackgroundTimer()
        }
        if (timerObject.timeState == Timer.TimeState.Running || timerObject.timeState == Timer.TimeState.Paused){
            timerObject.saveOnPause()
        }
    }
    //initsialiserer timer igjen
    override fun onResume() {
        super.onResume()
        Log.d("Test SmoerePaaminnelseFragment onResume()", timerObject.timeState.toString())
        timerObject.initTimer()
        Timer.removeAlarm(advanced.context)
    }
}

//Info siden som enkelt gir informasjon
class InfoFragment : Fragment() {

    private lateinit var info: View
    private lateinit var knappTilbake : ImageButton
    private lateinit var knappFremover : ImageButton
    private lateinit var vissteduTV : TextView
    private lateinit var vissteduTVUndertest : TextView
    private lateinit var uvKnapp: Button
    private lateinit var hudKnapp: Button
    private lateinit var solkremKnapp: Button
    private var plass : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        info = inflater.inflate(R.layout.fragment_info_display, container, false)
        return info
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        knappFremover = info.findViewById(R.id.knappFremover)
        knappTilbake = info.findViewById(R.id.knappTilbake)
        vissteduTV = info.findViewById(R.id.infoSvar)
        vissteduTVUndertest = info.findViewById(R.id.infoSvarUndertekst)
        uvKnapp = info.findViewById(R.id.uvKnapp)
        hudKnapp = info.findViewById(R.id.hudKnapp)
        solkremKnapp = info.findViewById(R.id.solKnapp)


        vissteduTV.text = resources.getString(R.string.solbrentParasoll)
        vissteduTVUndertest.text = resources.getString(R.string.solbrentParasollUndertekst)

        knappTilbake.setOnClickListener{
           fremBak(false)
        }
        knappFremover.setOnClickListener {
            fremBak(true)
        }

        uvKnapp.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.fhi.no/ml/miljo/straling/ultrafiolett-uv-straling/"))
            startActivity(i)
        }
        hudKnapp.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://kreftforeningen.no/forebygging/sol-solarium-og-hudkreft/"))
            startActivity(i)
        }
        solkremKnapp.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.fhi.no/ml/miljo/straling/ti-ting-du-ma-vite-om-soling-i-sommer/"))
            startActivity(i)
        }
    }
    //Knappene / visningen av ulik fakta / frem og tilbake
    private fun fremBak(flag: Boolean){
        if (flag) {
            plass += 1
        }
        else{
            plass -= 1
        }
        when(plass){
            0 -> {
                vissteduTV.text=resources.getString(R.string.solbrentParasoll)
                vissteduTVUndertest.text = resources.getString(R.string.solbrentParasollUndertekst)
            }

            1 -> {
                vissteduTV.text=resources.getString(R.string.solVindu)
                vissteduTVUndertest.text = resources.getString(R.string.solVinduUndertekst)

            }
            2 -> {
                vissteduTV.text=resources.getString(R.string.hudFaktor)
                vissteduTVUndertest.text = resources.getString(R.string.hudFaktorUndertekst)
            }

            3 -> {
                vissteduTV.text=resources.getString(R.string.solskade)
                vissteduTVUndertest.text = resources.getString(R.string.solskadeUndertekst)
            }

            4 -> {
                vissteduTV.text=resources.getString(R.string.hudkreft)
                vissteduTVUndertest.text = resources.getString(R.string.hudkreftUndertekst)
            }

            5 -> {
                vissteduTV.text=resources.getString(R.string.hudkreftNorge)
                vissteduTVUndertest.text = resources.getString(R.string.hudkreftNorgeUndertekst)
            }

            6 -> {
                vissteduTV.text=resources.getString(R.string.dVitamin)
                vissteduTVUndertest.text = resources.getString(R.string.dVitaminUndertekst)
            }

            7 -> {
                vissteduTV.text=resources.getString(R.string.solkremVinter)
                vissteduTVUndertest.text = resources.getString(R.string.solkremVinterUndertekst)
            }
            8 -> {
                vissteduTV.text=resources.getString(R.string.solbeskyttelse)
                vissteduTVUndertest.text = resources.getString(R.string.solbeskyttelseUndertekst)
            }
            9 -> {
                vissteduTV.text=resources.getString(R.string.solkremVoksen)
                vissteduTVUndertest.text = resources.getString(R.string.solkremVoksenUndertekst)
            }

            -1 -> {plass = 9}
            10 -> {plass = 0}
        }
    }
}

//SharedPreferences
class SharedPreferencesUser {
    companion object {

        private const val sliderId = "com.example.appen.ui.home.sliderValue"

        fun getSliderValue(context: Context): Int {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            return pref.getInt(sliderId, 0)
        }

        fun setSliderValue(sliderValue: Int, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(sliderId, sliderValue)
            editor.apply()
        }

        private var toogleId = "com.example.appen.ui.home.sliderValue"

        fun getTooglesValue(context: Context, id: Int ): Boolean {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            toogleId += id.toString()
            val returnValue: Boolean = pref.getBoolean(toogleId, false)
            Log.d("Test SharedPreferencesUser getTogglesValue() Toggle", toogleId)
            Log.d("Test SharedPreferencesUser getTogglesValue() Boolean", returnValue.toString())
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

