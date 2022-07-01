package com.hebaelsaid.android.driverapp.ui.notification

import android.app.*
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hebaelsaid.android.driverapp.R
import com.hebaelsaid.android.driverapp.common.Constants
import com.hebaelsaid.android.driverapp.ui.MainActivity

class Notification(private val context: Context){
    private lateinit var notify: Notification
    private lateinit var notifyManger: NotificationManagerCompat
    init {
        createNotifyChannel(context)
    }

    private fun createNotifyChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                Constants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.BLUE
                enableLights(true)
            }
            val manager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

     fun createAcceptedNotification(message:String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        notify = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            .setContentTitle("Order Status Notify")
            .setContentText(message)
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()


        notifyManger = NotificationManagerCompat.from(context)
        notifyManger.notify(Constants.NOTIFY_ID, notify)
    }

}
