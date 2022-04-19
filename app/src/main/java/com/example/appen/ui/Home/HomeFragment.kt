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
import java.text.SimpleDateFormat
import java.util.*


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

    private val simple = SimpleDisplayFragment()
    private val advanced = AdvancedDisplayFragment()

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        if (position==0) return simple
        else return advanced
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
                                    simpleDisp.updateUi(innUv)
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
class SimpleDisplayFragment : Fragment() {

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
        timerObject.saveOnPause()

    }

    override fun onResume() {
        super.onResume()
        timerObject.initTimer()
        Timer.removeAlarm(advanced.context)
    }
}

//TEST BRANCH