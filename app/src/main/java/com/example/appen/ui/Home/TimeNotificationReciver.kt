package com.example.appen.ui.Home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimeNotificationReciver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "stop" -> {
                Timer.removeAlarm(context)
                sharedPreferences.setTimeState(Timer.TimeState.stopped, context)
                Notification.hideTimerNotification(context)
            }

            "pause" -> {
                var secondsRemaining = sharedPreferences.getTimerLengthSecondsRemaining(context)
                val alarmSetTime = sharedPreferences.getAlarmSetTime(context)
                val nowSeconds = Timer.nowSeconds

                secondsRemaining -= nowSeconds - alarmSetTime
                sharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, context)

                Timer.removeAlarm(context)
            }

            "resume" -> {
                val secondsRemaining = sharedPreferences.getTimerLengthSecondsRemaining(context)
                val wakeUpTime = Timer.setAlarm(context, Timer.nowSeconds, secondsRemaining)
                sharedPreferences.setTimeState(Timer.TimeState.running, context)
            }

            "start" -> {
                val secRemaining = sharedPreferences.getTimerLengthSeconds(context)
                val wakeUpTime = Timer.setAlarm(context, Timer.nowSeconds, secRemaining)
                sharedPreferences.setTimeState(Timer.TimeState.running, context)
                sharedPreferences.setTimerLengthSecondsRemaining(secRemaining, context)
            }
        }
    }
}