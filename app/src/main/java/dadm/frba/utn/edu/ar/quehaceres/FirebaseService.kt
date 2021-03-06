package dadm.frba.utn.edu.ar.quehaceres

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dadm.frba.utn.edu.ar.quehaceres.services.Services
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import android.net.Uri
import android.support.v4.app.NotificationCompat

class FirebaseService: FirebaseMessagingService() {

    val service by lazy { Services(baseContext) }

    override fun onNewToken(token: String?) {
        Log.d("FIREBASE", "Refreshed token: $token")

        service.postTokenIfPossible(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.d("FIREBASE", "onMessageReceived")

        val data = remoteMessage!!.data

        val title = data["title"]
        val message = data["message"]
        val deepLink = data["deeplink"]

        sendNotification(this, title, message, deepLink)
    }

    fun sendNotification(context: Context, title: String?, message: String?, deepLink: String?) {
        Log.d("FIREBASE", "Creating push notification title: $title, message: $message, deepLink: $deepLink")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                    "quehaceres_default_id",
                    "quehaceres_channel_name",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "Any description can be given!"
            notificationManager.createNotificationChannel(notificationChannel)
            NotificationCompat.Builder(context, "quehaceres_default_id")
        } else {
            NotificationCompat.Builder(context)
        }

        val notificationBuilder = builder
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.logo)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo))

        val intent = Intent()

        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(deepLink)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        notificationBuilder
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)

        notificationManager.notify(2468, notificationBuilder.build())
    }
}