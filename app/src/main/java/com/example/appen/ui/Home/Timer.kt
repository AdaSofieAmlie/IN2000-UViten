package com.example.appen.ui.Home

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.appen.R
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.*


class Timer (advancedIn : View) {

    lateinit var tvTimer : TextView
    lateinit var progress : MaterialProgressBar
    lateinit var buttonStart : Button
    lateinit var buttonPause : Button
    lateinit var buttonStop : Button
    lateinit var timer : CountDownTimer
    private var timerLengthInSeconds = 0L
    lateinit var timeState : TimeState
    private var secondsRemaining = 0L

    val advanced = advancedIn

    enum class TimeState{
        stopped, paused, running
    }

    companion object{
        val nowSeconds:Long
            get() {
                val date = Date()
                return date.time
            }

        fun setAlarm(context: Context, nowSecond: Long, secondsRemaining: Long): Long{

            val wakeUpTime = nowSecond + secondsRemaining * 1000

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpired::class.java)

            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            sharedPreferences.setAlarmSetTime(nowSeconds, context)

            return wakeUpTime
        }

        fun removeAlarm(context: Context){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpired::class.java)
            val pendingInten = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.cancel(pendingInten)
            sharedPreferences.setAlarmSetTime(0, context)
            sharedPreferences.setTimerLengthSecondsRemaining(0, context)
        }

    }

    fun settUpTimer(tid : Long) : Timer{
        tvTimer = advanced.findViewById(R.id.tvDigitalTime)
        buttonStart = advanced.findViewById(R.id.buttonStart)
        buttonPause = advanced.findViewById(R.id.buttonPause)
        buttonStop = advanced.findViewById(R.id.buttonStop)
        progress = advanced.findViewById(R.id.progress_circular)
        progress.visibility = View.VISIBLE

        val savedTime= sharedPreferences.getTimerLengthSeconds(advanced.context)
        if ( savedTime == 0L){
            timerLengthInSeconds = tid
            timeState = TimeState.stopped
            sharedPreferences.setTimerLengthSecondsRemaining(timerLengthInSeconds, advanced.context)
        } else {
            secondsRemaining = savedTime
            timeState = sharedPreferences.getTimeState(advanced.context)
            sharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, advanced.context)
        }
        sharedPreferences.setTimerLengthSeconds(tid, advanced.context)

        sharedPreferences.setTimeState(timeState, advanced.context)
        progress.max = timerLengthInSeconds.toInt()


        buttonStart.setOnClickListener{
            startTimer()
            timeState = TimeState.running
            sharedPreferences.setTimeState(timeState, advanced.context)
            updateButtons()
        }

        buttonPause.setOnClickListener{
            pauseTimer()
            timeState = TimeState.paused
            updateButtons()
        }

        buttonStop.setOnClickListener{
            stopTimer()
            timeState = TimeState.stopped
            updateButtons()
        }
        buttonStop.isEnabled = false
        buttonPause.isEnabled = false

        return this
    }

    fun initTimer(){
        timeState = sharedPreferences.getTimeState(advanced.context)
        Notification.hideTimerNotification(advanced.context)

        if (timeState == TimeState.stopped){
            setNewTimerLength()
        } else {
            setPrviousTimerLength()
        }

        secondsRemaining =
            if (timeState == TimeState.running || timeState == TimeState.paused) {
                sharedPreferences.getTimerLengthSecondsRemaining(advanced.context)
            } else { sharedPreferences.getTimerLengthSeconds(advanced.context) }

        val alarmSetTime = sharedPreferences.getAlarmSetTime(advanced.context)
        removeAlarm(advanced.context)

        if (alarmSetTime > 0){
            val minus = (nowSeconds - alarmSetTime ) / 1000
            secondsRemaining -= minus
        }

        if (secondsRemaining <= 0){
            timer.cancel()
        } else if (timeState == TimeState.running){
            startTimer()
        }
        updateButtons()
        updateTimerUI()
    }

    fun setNewTimerLength(){
        val lengthInMinutes = sharedPreferences.getTimerLengthSeconds(advanced.context)
        timerLengthInSeconds = lengthInMinutes
        progress.max = timerLengthInSeconds.toInt()
        settUpTimer(timerLengthInSeconds)
    }

    fun setPrviousTimerLength(){
        secondsRemaining = sharedPreferences.getTimerLengthSecondsRemaining(advanced.context)
        progress.max = sharedPreferences.getTimerLengthSeconds(advanced.context).toInt()
    }

    fun startTimer(){
        Notification.hideTimerNotification(advanced.context)

        if (timeState == TimeState.paused){
            setPrviousTimerLength()
        }
        timeState = TimeState.running
        sharedPreferences.setTimeState(timeState, advanced.context)
        progress.progress = secondsRemaining.toInt()
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                sharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, advanced.context)
                updateTimerUI()
            }

            override fun onFinish() {
                stopTimer()
                Notification.showTimerExpired(advanced.context)
            }

        }.start()
    }

    fun pauseTimer() {
        timeState = TimeState.paused
        sharedPreferences.setTimerLengthSeconds(timerLengthInSeconds, advanced.context)
        sharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, advanced.context)
        sharedPreferences.setTimeState(timeState, advanced.context)
        timer.cancel()
    }

    fun stopTimer() {
        timer.cancel()
        timeState = TimeState.stopped
        setNewTimerLength()

        tvTimer.text = "0:00:00"
        progress.progress = 0

        sharedPreferences.setTimeState(timeState, advanced.context)
        sharedPreferences.setTimerLengthSecondsRemaining(timerLengthInSeconds, advanced.context)
        secondsRemaining = timerLengthInSeconds

        updateButtons()
        updateTimerUI()

    }

    fun updateTimerUI(){
        var timeRemaining = secondsRemaining
        val hoursUntilFinished = timeRemaining/60/60
        timeRemaining = timeRemaining - (hoursUntilFinished * 60 * 60)
        val minutesUntilFinsished = timeRemaining /60
        val minutesStr = minutesUntilFinsished.toString()
        timeRemaining = timeRemaining - minutesUntilFinsished * 60
        val secondsInMinutesUntilFinished = timeRemaining
        val timeStr = secondsInMinutesUntilFinished.toString()
        progress.progress = secondsRemaining.toInt()

        var stringTimer = hoursUntilFinished.toString()
        stringTimer += ":"
        if (minutesStr.length == 2){
            stringTimer += minutesStr
        }
        else {
            stringTimer += "0"
            stringTimer += minutesStr
        }
        stringTimer += ":"
        if (timeStr.length == 2){
            stringTimer += timeStr
        }
        else {
            stringTimer += "0"
            stringTimer += timeStr
        }
        tvTimer.text = stringTimer

    }

    fun updateButtons(){
        timeState = sharedPreferences.getTimeState(advanced.context)
        when (timeState){
            TimeState.running -> {
                buttonStart.isEnabled = false
                buttonPause.isEnabled = true
                buttonStop.isEnabled = true
            }
            TimeState.paused -> {
                buttonStart.isEnabled = true
                buttonPause.isEnabled = false
                buttonStop.isEnabled = true
            }
            TimeState.stopped -> {
                buttonStart.isEnabled = true
                buttonPause.isEnabled = true
                buttonStop.isEnabled = false
            }
        }
    }

    fun saveOnPause(){
        sharedPreferences.setTimerLengthSeconds(timerLengthInSeconds, advanced.context)
        sharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, advanced.context)
        sharedPreferences.setTimeState(timeState, advanced.context)
        timer.cancel()
    }

    fun onPauseStartBackgroundTimer() : Long {
        val wakeUpTime : Long = setAlarm(advanced.context, nowSeconds, secondsRemaining)
        return wakeUpTime
    }
}

class sharedPreferences(){
    companion object{

        const val timerLengthSecondsId = "com.example.appen.timer.timerLength"

        fun getTimerLengthSeconds(context: Context): Long{
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            return pref.getLong(timerLengthSecondsId, 0)
        }

        fun setTimerLengthSeconds(seconds : Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(timerLengthSecondsId, seconds)
            editor.apply()
        }

        private const val timeStateId = "com.example.appen.timer.timeState"

        fun getTimeState(context: Context): Timer.TimeState{
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = pref.getInt(timeStateId, 0)
            return Timer.TimeState.values()[ordinal]
        }

        fun setTimeState(state: Timer.TimeState, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(timeStateId, ordinal)
            editor.apply()
        }

        const val timerLengthSecondsRemainingId = "com.example.appen.timer.timerLengthSecondsRemaining"

        fun getTimerLengthSecondsRemaining(context: Context): Long{
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            return pref.getLong(timerLengthSecondsRemainingId, 0L)
        }

        fun setTimerLengthSecondsRemaining(seconds : Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(timerLengthSecondsRemainingId, seconds)
            editor.apply()
        }

        private const val alarmSetTimeId = "com.example.appen.timer.backgroundTimer.alarm"

        fun getAlarmSetTime (context: Context): Long{
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val returnValue = pref.getLong(alarmSetTimeId, 0)
            return returnValue
        }

        fun setAlarmSetTime(time:Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(alarmSetTimeId, time)
            editor.apply()
        }

    }
}
