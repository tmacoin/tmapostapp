package org.tmacoin.post.android.messaging;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import org.tma.blockchain.Wallet;
import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.thin.GetMessagesRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SecureMessage;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.security.PublicKey;
import java.util.Iterator;
import java.util.List;

public class NewMessageNotifier {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static NewMessageNotifier instance;

    private SecureMessage lastMessage = null;
    private Activity activity;

    public NewMessageNotifier(Activity activity) {
        instance = this;
        this.activity = activity;
        run();
    }

    public static NewMessageNotifier getInstance() {
        return instance;
    }

    private void run() {
        ThreadExecutor.getInstance().execute(new TmaRunnable("NewMessageNotifier") {
            public void doRun() {
                while(true) {
                    try {
                        process();
                        ThreadExecutor.sleep(60000);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void process() {
        Network network = Network.getInstance();
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        int attempt = 0;
        List<SecureMessage> list = null;
        while(list == null && attempt++ < 5) {
            if(!network.isPeerSetComplete()) {
                new BootstrapRequest(network).start();
            }
            PublicKey publicKey = wallet.getPublicKey();
            GetMessagesRequest request = new GetMessagesRequest(network, publicKey);
            request.start();
            list = (List<SecureMessage>) ResponseHolder.getInstance().getObject(request.getCorrelationId());
        }

        if(list == null || list.isEmpty()) {
            return;
        }

        Iterator<SecureMessage> iterator = list.iterator();

        while(iterator.hasNext()) {
            SecureMessage secureMessage = iterator.next();
            if (!secureMessage.getRecipient().equals(wallet.getTmaAddress())) {
                iterator.remove();
            }
        }

        if(list.isEmpty()) {
            return;
        }

        SecureMessage message = list.get(0);
        if(lastMessage == null) {
            lastMessage = message;
            return;
        }



        if(!lastMessage.getText().equals(message.getText())) {
            lastMessage = message;
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run() {
                    try {
                        addNotification();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });

        }


    }

    private void addNotification() {
        createNotificationChannel();
        String channelId = activity.getString(R.string.channel_id);
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(activity.getApplicationContext(), channelId)
                        .setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Secure Message Received")
                        .setContentText(lastMessage.getSubject(wallet.getPrivateKey()))
                        .setAutoCancel(true)
                        .setVibrate(new long[] { 1000, 1000})
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setPriority(NotificationCompat.PRIORITY_MAX);
        ;

        Intent notificationIntent = new Intent(activity, ShowMessagesActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(activity, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = activity.getString(R.string.channel_id);
            CharSequence name = activity.getString(R.string.channel_name);
            String description = activity.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
