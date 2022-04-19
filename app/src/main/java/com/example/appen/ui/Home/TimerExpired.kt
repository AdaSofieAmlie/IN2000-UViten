package com.example.appen.ui.Home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import io.ktor.utils.io.concurrent.*
import java.lang.UnsupportedOperationException

class TimerExpired: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Show notification

        Notification.showTimerExpired(context)

        sharedPreferences.setTimeState(Timer.TimeState.stopped, context)
        sharedPreferences.setAlarmSetTime(0, context)

    }
}