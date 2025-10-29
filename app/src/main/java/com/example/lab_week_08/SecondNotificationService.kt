package com.example.lab_week_08

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.os.Looper
import android.os.Handler

// Ini adalah Service kedua, disalin dari NotificationService.
class SecondNotificationService : Service() {
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler
    override fun onBind(intent: Intent): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        notificationBuilder = startForegroundService()
        // Menggunakan nama thread yang berbeda agar tidak bingung
        val handlerThread = HandlerThread("ThirdThread")
            .apply { start() }
        serviceHandler = Handler(handlerThread.looper)
    }
    private fun startForegroundService(): NotificationCompat.Builder {
        val pendingIntent = getPendingIntent()
        // Menggunakan Channel ID yang berbeda
        val channelId = createNotificationChannel("002", "002 Channel")
        val notificationBuilder = getNotificationBuilder(
            pendingIntent, channelId
        )
        // Menggunakan NOTIFICATION_ID yang BERBEDA
        startForeground(NOTIFICATION_ID_SECOND, notificationBuilder.build())
        return notificationBuilder
    }
    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(
            this, 0, Intent(
                this,
                MainActivity::class.java
            ), flag
        )
    }

    // Menjadikan fungsi ini lebih fleksibel dengan parameter
    private fun createNotificationChannel(channelId: String, channelName: String): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelPriority = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                channelId,
                channelName,
                channelPriority
            )
            val service = requireNotNull(
                ContextCompat.getSystemService(this,
                    NotificationManager::class.java)
            )
            service.createNotificationChannel(channel)
            channelId
        } else { "" }
    private fun getNotificationBuilder(pendingIntent: PendingIntent, channelId:
    String) =
        NotificationCompat.Builder(this, channelId)
            // Mengubah teks notifikasi
            .setContentTitle("Third worker process is done")
            .setContentText("This is the second service!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("Third worker process is done, check it out!")
            .setOngoing(true)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        val returnValue = super.onStartCommand(intent,
            flags, startId)
        val Id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")
        serviceHandler.post {
            // Mengubah timer hitung mundur (misal jadi 5 detik)
            // agar toast tidak bertabrakan.
            countDownFromFiveToZero(notificationBuilder)
            notifyCompletion(Id)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return returnValue
    }

    // Mengubah fungsi countdown
    private fun countDownFromFiveToZero(notificationBuilder:
                                        NotificationCompat.Builder) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as
                NotificationManager
        // Hitung mundur dari 5
        for (i in 5 downTo 0) {
            Thread.sleep(1000L)
            notificationBuilder.setContentText("$i seconds until final process")
                .setSilent(true)
            // Menggunakan NOTIFICATION_ID yang BERBEDA
            notificationManager.notify(
                NOTIFICATION_ID_SECOND,
                notificationBuilder.build()
            )
        }
    }
    private fun notifyCompletion(Id: String) {
        Handler(Looper.getMainLooper()).post {
            // Menggunakan LiveData yang BERBEDA
            mutableIDSecond.value = Id
        }
    }

    companion object {
        // ID Notifikasi yang BERBEDA
        const val NOTIFICATION_ID_SECOND = 0xCA8
        const val EXTRA_ID = "Id"

        // Objek LiveData yang BERBEDA untuk diobservasi
        private val mutableIDSecond = MutableLiveData<String>()
        val trackingCompletionSecond: LiveData<String> = mutableIDSecond
    }
}
