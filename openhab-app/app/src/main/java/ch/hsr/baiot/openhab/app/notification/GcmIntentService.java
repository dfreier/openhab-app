
package ch.hsr.baiot.openhab.app.notification;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.activity.MainActivity;


public class GcmIntentService extends IntentService {

    private static final String TAG = "GcmIntentService";
    private NotificationManager mNotificationManager;
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null)
            return;

        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendNotification(intent.getExtras().getString("message"), 1);
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg, int notificationId) {

        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("notificationId", notificationId);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.house)
                        .setContentTitle("openHAB")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setSound(alarmSound)
                        .setContentText(msg);
        mBuilder.setContentIntent(pendingNotificationIntent);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}
