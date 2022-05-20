package com.example.appen.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.appen.R
import java.util.*

//Notifikasjon om at timer er gått ut
class Notification {

    companion object{
        private const val channelIdTimer = "MenuTimer"
        private const val channelNameTimer = "TimerAppTimer"
        private const val timerId = 0

        //Viser notifikasjon når timer har gått ut
        fun showTimerExpired(context: Context){
            val startIntent = Intent(context, TimeNotificationReceiver::class.java)
            startIntent.action = "start"

            val date = Date()
            var timerExpiredAt: String = date.hours.toString() + ":"
            if (date.minutes.toString().length == 1 ) {
                timerExpiredAt += "0"
            }
            timerExpiredAt += date.minutes.toString()

            val notificationBuilder = getBasicNotification(context, channelIdTimer, true)
            val pendingIntent = PendingIntent.getActivity(context, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            //ikon for notifikasjon
            val icon = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.ic_round_wb_sun_24
            )

            //bygger selve notifikasjonen for appen
            notificationBuilder.setContentTitle("På tide å smøre seg på nytt!")
                .setContentText("Nedtellingen var ferdig $timerExpiredAt")
                .setContentIntent(pendingIntent)
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.ic_round_wb_sun_24)
            notificationBuilder.color = 0xFF6329
            //snakker med notification manager og sender ut notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelIdTimer, channelNameTimer, true)
            notificationManager.notify(timerId, notificationBuilder.build())
            Log.d("Test Notifikasjon showTimerExpired()", "Sendt og bygget notifikasjon")
        }

        fun hideTimerNotification(context: Context){
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(timerId)
        }

        //NotificationManager android
        private fun NotificationManager.createNotificationChannel(channelId : String, channelName : String, playSound: Boolean){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW

                val notificationChannel = NotificationChannel(channelId, channelName, channelImportance)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor= Color.BLUE
                this.createNotificationChannel(notificationChannel)
            }

        }

        private fun getBasicNotification(context: Context, channelId: String, playSound: Boolean): NotificationCompat.Builder {
            val notificationSound : Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
                .setAutoCancel(true)
                .setDefaults(0)
            if (playSound){
                notificationBuilder.setSound(notificationSound)
            }
            return notificationBuilder
        }
    }
}