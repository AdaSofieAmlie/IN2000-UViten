package com.example.appen.ui.Home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.app.NotificationCompat
import com.example.appen.R
import io.ktor.http.*
import java.util.*


class Notification {

    companion object{
        private const val channelIdTimer = "MenuTimer"
        private const val channelNameTimer = "TimerAppTimer"
        private const val timerId = 0

        fun showTimerExpired(context: Context){
            val startIntent = Intent(context, TimeNotificationReciver::class.java)
            startIntent.action = "start"
            val startPendingIntent = PendingIntent.getBroadcast(context, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val date = Date()
            var timerExpiredAt: String = date.hours.toString() + ":"
            if (date.minutes.toString().length == 1 ) {
                timerExpiredAt += "0"
            }
            timerExpiredAt += date.minutes.toString()

            val notificationBuilder = getBasicNotification(context, channelIdTimer, true)
            val pendingIntent = PendingIntent.getActivity(context, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val icon = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.ic_round_wb_sun_24
            )

            notificationBuilder.setContentTitle("På tide å smøre seg på nytt!")
                .setContentText("Nedtellingen var ferdig $timerExpiredAt")
                .setContentIntent(pendingIntent)
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.ic_round_wb_sun_24)
            notificationBuilder.color = 0xFF6329

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelIdTimer, channelNameTimer, true)
            notificationManager.notify(timerId, notificationBuilder.build())
            Log.d("Not1", "1")
        }

        public fun hideTimerNotification(context: Context){
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(timerId)
        }


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

        private fun <T> getPendingIntentWithStack( context: Context, javaClass: Class<T>) : PendingIntent{
            val resultIntet = Intent(context, javaClass)
            resultIntet.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultIntet)

            return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}