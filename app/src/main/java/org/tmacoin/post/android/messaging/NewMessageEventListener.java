package org.tmacoin.post.android.messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import org.tma.blockchain.Wallet;
import org.tma.peer.thin.NewMessageEvent;
import org.tma.peer.thin.SecureMessage;
import org.tma.util.Event;
import org.tma.util.EventListener;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidContants;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.util.Objects;

public class NewMessageEventListener implements EventListener {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private Context context;

    public NewMessageEventListener(Context context) {
        this.context = context;
    }

    @Override
    public void onEvent(Event event) {
        logger.debug("Received new message event");
        NewMessageEvent newMessageEvent = (NewMessageEvent)event;
        addNotification(newMessageEvent.getSecureMessage());
    }

    private void addNotification(SecureMessage secureMessage) {
        createNotificationChannel();
        String channelId = AndroidContants.TMACOIN;
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(context.getString(R.string.secure_message_received))
                        .setContentText(secureMessage.getSubject(wallet.getPrivateKey()))
                        .setAutoCancel(true)
                        .setVibrate(new long[] { 1000, 1000})
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        ;

        Intent notificationIntent = new Intent(context, ShowMessageActivity.class);
        notificationIntent.putExtra("secureMessage", secureMessage);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = AndroidContants.TMACOIN;
            CharSequence name = AndroidContants.TMACOIN;
            String description = AndroidContants.TMACOIN;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
