package com.example.appen.ui.Home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
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
import com.example.appen.R
import com.example.appen.ui.Home.Notification.Companion.hideTimerNotification
import kotlinx.coroutines.Dispatchers
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import okhttp3.Dispatcher
import java.util.prefs.Preferences
import android.app.Notification as Notification

class HomeFragment : Fragment() {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var demoCollectionAdapter: HomeCollectionAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        demoCollectionAdapter = HomeCollectionAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = demoCollectionAdapter
    }
}

class HomeCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        if (position==0) return SimpleDisplayFragment()
        else return AdvancedDisplayFragment()
    }
}

// Instances of this class are fragments representing a single
// object in our collection.
class SimpleDisplayFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_simple_display, container, false)
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