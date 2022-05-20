package com.example.appen.ui.home

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.appen.R
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

//Timer klasse
//Nedtelling som gir notifikasjon
//fungerer også når appen er lukket
//Mange depricatede funksjoner og klasser
class Timer (advancedIn : View) {
    //TV for forklaring på timer
    private lateinit var tvTimer : TextView

    //Sirkulær Progressbar
    //Material ProgressBar: https://github.com/zhanghai/MaterialProgressBar
    private lateinit var progress : MaterialProgressBar

    //knapper for timer
    private val  buttonStart : Button = advancedIn.findViewById(R.id.buttonStart)
    val buttonPause : Button = advancedIn.findViewById(R.id.buttonPause)
    val buttonStop : Button = advancedIn.findViewById(R.id.buttonStop)
    val tvTimerExpired : TextView = advancedIn.findViewById(R.id.tvTimerExpired)

    //Timer innstillinger og defaults
    private lateinit var timer : CountDownTimer
    private var timerLengthInSeconds = 0L
    var timeState : TimeState = TimeState.Stopped
    private var secondsRemaining = 0L
    private var wakeUpTime : Long = 0L

    val advanced = advancedIn

    //States for timer
    enum class TimeState{
        Stopped, Paused, Running
    }

    //Companion object for alarm/lyd på timer
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
            SharedPreferences.setAlarmSetTime(nowSeconds, context)

            return wakeUpTime
        }

        fun removeAlarm(context: Context){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpired::class.java)
            val pendingInten = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.cancel(pendingInten)
            SharedPreferences.setAlarmSetTime(0, context)
            SharedPreferences.setTimerLengthSecondsRemaining(0, context)
        }

    }

    //Selve byggingen av timer
    fun settUpTimer(tid : Long) : Timer{
        tvTimer = advanced.findViewById(R.id.tvDigitalTime)
        progress = advanced.findViewById(R.id.progress_circular)
        progress.visibility = View.VISIBLE
        SharedPreferences.setTimerLengthSeconds(tid, advanced.context)
        timerLengthInSeconds = tid
        //Oppdaterer knappene så det stemmer med timeren
        updateButtons()
        //Sjekker tid igjen og setter den til gjennstående tid eller til 00.00.00
        val savedTime = SharedPreferences.getTimerLengthSecondsRemaining(advanced.context)
        Log.d("Test Timer settUpTimer() lagretTid", savedTime.toString())
        if ( savedTime == 0L){
            timeState = TimeState.Stopped
            SharedPreferences.setTimerLengthSecondsRemaining(timerLengthInSeconds, advanced.context)
        } else {
            secondsRemaining = savedTime
            timeState = SharedPreferences.getTimeState(advanced.context)
            SharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, advanced.context)
            Log.d("Test Timer settUpTimer() lagretTidIgjen", secondsRemaining.toString())
        }
        //Lagrer TimeState i SharedPreferences
        SharedPreferences.setTimeState(timeState, advanced.context)
        progress.max = timerLengthInSeconds.toInt()

        //Setter på ClickListeners for Start/Pause/Stop
        buttonStart.setOnClickListener{
            buttonStop.text = "STOP"
            startTimer()
            timeState = TimeState.Running
            SharedPreferences.setTimeState(timeState, advanced.context)
            updateButtons()
            tvTimerExpired.text = ""
        }
        buttonPause.setOnClickListener{
            pauseTimer()
            timeState = TimeState.Paused
            updateButtons()
            tvTimerExpired.text = ""
        }
        buttonStop.setOnClickListener{
            stopTimer()
            timeState = TimeState.Stopped
            updateButtons()
            updateTimerUI()
            tvTimerExpired.text = ""
        }
        //oppdaterer UI for bruker etter oppsett
        updateTimerUI()
        return this
    }

    //initsialisering av timer / henter timestate fra sharedpreferences
    fun initTimer(){
        timeState = SharedPreferences.getTimeState(advanced.context)
        Notification.hideTimerNotification(advanced.context)

        if (timeState == TimeState.Stopped){
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }

        secondsRemaining = if (timeState == TimeState.Running || timeState == TimeState.Paused) {
            SharedPreferences.getTimerLengthSecondsRemaining(advanced.context)
        } else {
            SharedPreferences.getTimerLengthSeconds(advanced.context)
        }

        val alarmSetTime = SharedPreferences.getAlarmSetTime(advanced.context)
        removeAlarm(advanced.context)

        if (alarmSetTime > 0){
            val minus = (nowSeconds - alarmSetTime ) / 1000
            secondsRemaining -= minus
        } else {
            if (wakeUpTime != 0L && timeState == TimeState.Stopped){
                convertToTime(wakeUpTime)
            }

        }
        Log.d("Test Timer initTimer() tid", secondsRemaining.toString())
        //Dersom det er tid igjen i nedtellingen fortsettere den
        if (secondsRemaining <= 0){
            timer.cancel()
        } else if (timeState == TimeState.Running){
            startTimer()
        }
        //oppdaterer UI for bruker
        updateButtons()
        updateTimerUI()
        SharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, advanced.context)
    }

    private fun convertToTime(time : Long){
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
        val dateWhenTheTimerExpired = Date(time)
        Log.d("Test Timer convertToTime() tidUtgaatt", dateWhenTheTimerExpired.toString())
        var stringTimerExpired = "Nedtellingen gikk ut kl: "
        stringTimerExpired += dateFormat.format(dateWhenTheTimerExpired)
        stringTimerExpired += ". Husk å smøre deg på nytt! ;)"

        tvTimerExpired.text = stringTimerExpired
    }

    //Setter nåværende timer / nye timer i fokus lengde
    private fun setNewTimerLength(){
        val lengthInMinutes = SharedPreferences.getTimerLengthSeconds(advanced.context)
        progress.max = lengthInMinutes.toInt()
        settUpTimer(lengthInMinutes)
    }
    //Setter forrige / tid som er igjen
    private fun setPreviousTimerLength(){
        secondsRemaining = SharedPreferences.getTimerLengthSecondsRemaining(advanced.context)
        progress.max = SharedPreferences.getTimerLengthSeconds(advanced.context).toInt()
    }

    //Starter timer-objektet og lagrer Timestate i Sharedpreferences
    private fun startTimer(){
        val date = Date()
        Notification.hideTimerNotification(advanced.context)
        Log.d("Test Timer startTimer() tidSekunderIgjen", secondsRemaining.toString())
        progress.max = SharedPreferences.getTimerLengthSeconds(advanced.context).toInt()

        if (secondsRemaining == 0L){
            secondsRemaining = SharedPreferences.getTimerLengthSeconds(advanced.context)
            Log.d("Test Timer startTimer() tidSekunderIgjen", secondsRemaining.toString())
        }

        timeState = TimeState.Running

        SharedPreferences.setTimeState(timeState, advanced.context)
        progress.progress = secondsRemaining.toInt()
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                SharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, advanced.context)
                updateTimerUI()
            }

            override fun onFinish() {
                stopTimer()
                buttonStop.text = "Restart"
                buttonPause.isEnabled = false
                var minutt = date.minutes.toString()
                if(minutt.length == 1) {
                    minutt = "0" + date.minutes.toString()
                }
                tvTimerExpired.text = "Nedtellingen var ferdig "  + date.hours.toString() + ":" + minutt + ". Husk å smøre deg på nytt!"
                Notification.showTimerExpired(advanced.context)
            }
        }.start()
    }

    //Pauser timer lagrer TimeStates / tid igjen osv. i sharedpreferences
    private fun pauseTimer() {
        timeState = TimeState.Paused
        SharedPreferences.setTimerLengthSeconds(timerLengthInSeconds, advanced.context)
        SharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, advanced.context)
        SharedPreferences.setTimeState(timeState, advanced.context)
        Log.d("Test Timer pauseTimer() pause", "Paused timer")
        timer.cancel()
    }

    //stopper timer og setter en "tom/ny" timer opp
    fun stopTimer() {
        if (timeState != TimeState.Paused){
            timer.cancel()
            SharedPreferences.setTimerLengthSeconds(timerLengthInSeconds, advanced.context)
        }

        timeState = TimeState.Stopped
        setNewTimerLength()

        tvTimer.text = "0:00:00"
        progress.progress = 0

        SharedPreferences.setTimeState(timeState, advanced.context)
        SharedPreferences.setTimerLengthSecondsRemaining(timerLengthInSeconds, advanced.context)
        secondsRemaining = timerLengthInSeconds
        Log.d("Test Timer stopTimer() timerISekunder", timerLengthInSeconds.toString())

        updateButtons()
    }

    //Oppdaterer Timerens UI elementer / Progressbar / Tid TextView
    fun updateTimerUI(){
        var timeRemaining = secondsRemaining
        val hoursUntilFinished = timeRemaining/60/60
        timeRemaining -= (hoursUntilFinished * 60 * 60)
        val minutesUntilFinsished = timeRemaining /60
        val minutesStr = minutesUntilFinsished.toString()
        timeRemaining -= minutesUntilFinsished * 60
        val secondsInMinutesUntilFinished = timeRemaining
        val timeStr = secondsInMinutesUntilFinished.toString()
        progress.progress = timeRemaining.toInt()

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

    //Oppdaterer buttons under Timer Start/pause/stop
    private fun updateButtons(){
        when (timeState){
            TimeState.Running -> {
                buttonStart.isEnabled = false
                buttonPause.isEnabled = true
                buttonStop.isEnabled = true
            }
            TimeState.Paused -> {
                buttonStart.isEnabled = true
                buttonPause.isEnabled = false
                buttonStop.isEnabled = true
            }
            TimeState.Stopped -> {
                buttonStart.isEnabled = true
                buttonPause.isEnabled = false
                buttonStop.isEnabled = false
            }
        }
    }

    //Lagrer timestate og tid igjen når fragment pauses
    fun saveOnPause(){
        SharedPreferences.setTimerLengthSeconds(timerLengthInSeconds, advanced.context)
        SharedPreferences.setTimerLengthSecondsRemaining(secondsRemaining, advanced.context)
        SharedPreferences.setTimeState(timeState, advanced.context)
        timer.cancel()
    }
    //Starter backgroundtimer (når appen lukkes)
    fun onPauseStartBackgroundTimer() : Long {
        wakeUpTime = setAlarm(advanced.context, nowSeconds, secondsRemaining)
        Log.d("Test Timer onPauseStartBackgroundTimer() wakeUpTime", wakeUpTime.toString())
        return wakeUpTime
    }
}
//Sharedpreferencesclass
class SharedPreferences{
    companion object{
        private const val timerLengthSecondsId = "com.example.appen.timer.timerLength"

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

        private const val timerLengthSecondsRemainingId = "com.example.appen.timer.timerLengthSecondsRemaining"

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

        fun getAlarmSetTime(context: Context): Long {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            return pref.getLong(alarmSetTimeId, 0)
        }

        fun setAlarmSetTime(time:Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(alarmSetTimeId, time)
            editor.apply()
        }

    }
}
