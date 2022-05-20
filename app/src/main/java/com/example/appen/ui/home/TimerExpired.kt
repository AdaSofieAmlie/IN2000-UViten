package com.example.appen.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

//Når timer går ut. Broadcaster.
class TimerExpired: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        //Viser notifikasjon
        Notification.showTimerExpired(context)
        //setter timestate
        SharedPreferences.setTimeState(Timer.TimeState.Stopped, context)
        SharedPreferences.setAlarmSetTime(0, context)

    }
}