package com.example.appen.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimeNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "stop" -> {
                Timer.removeAlarm(context)
                SharedPreferences.setTimeState(Timer.TimeState.Stopped, context)
                Notification.hideTimerNotification(context)
            }

            "pause" -> {
                var secondsRemaining = SharedPreferences.getTimerLengthSecondsRemaining(context)
                val alarmSetTime = SharedPreferences.getAlarmSetTime(context)
                val nowSeconds = Timer.nowSeconds

                secondsRemaining -= nowSeconds - alarmSetTime
                SharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, context)

                Timer.removeAlarm(context)
            }

            "resume" -> {
                val secondsRemaining = SharedPreferences.getTimerLengthSecondsRemaining(context)
                Timer.setAlarm(context, Timer.nowSeconds, secondsRemaining)
                SharedPreferences.setTimeState(Timer.TimeState.Running, context)
            }

            "start" -> {
                val secRemaining = SharedPreferences.getTimerLengthSeconds(context)
                Timer.setAlarm(context, Timer.nowSeconds, secRemaining)
                SharedPreferences.setTimeState(Timer.TimeState.Running, context)
                SharedPreferences.setTimerLengthSecondsRemaining(secRemaining, context)
            }
        }
    }
}