package org.tmacoin.post.android.messaging;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.tma.blockchain.Wallet;
import org.tma.peer.BootstrapRequest;
import org.tma.peer.EmptyRequest;
import org.tma.peer.Network;
import org.tma.peer.Peer;
import org.tma.peer.thin.GetMessagesRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SecureMessage;
import org.tma.util.Constants;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;
import org.tmacoin.post.android.AndroidContants;
import org.tmacoin.post.android.PasswordUtil;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.TmaAndroidUtil;
import org.tmacoin.post.android.Wallets;

import java.net.UnknownHostException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.Iterator;
import java.util.List;

public class NewMessageNotifier extends Service {

    private static final TmaLogger logger = TmaLogger.getLogger();

    static {
        PasswordUtil.setupBouncyCastle();
    }

    private SecureMessage lastMessage = null;
    private Wallet wallet = null;
    private Context context;
    private String action;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.debug("onStartCommand intent.getAction()={}", intent.getAction());
        if (TmaAndroidUtil.STOP.equals(intent.getAction())) {
            action = TmaAndroidUtil.STOP;
            stopForeground(true);
            stopSelf();
        }
        context = getApplicationContext();
        Constants.FILES_DIRECTORY = getFilesDir().getAbsolutePath() + "/";
        run(intent);

        return Service.START_REDELIVER_INTENT;
    }

    private void setup(Intent intent) {
        wallet = (Wallet)intent.getSerializableExtra("wallet");
        String tmaAddress = wallet.getTmaAddress();
        try {
            Network network = Network.getInstance();
            if(network == null) {
                network = new Network(tmaAddress);
            }
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        logger.debug("onTaskRemoved");
        restart();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        logger.debug("onDestroy");
        super.onDestroy();
        restart();
    }

    private void restart() {
        if (TmaAndroidUtil.STOP.equals(action)) {
            return;
        }
        logger.debug("wallet={}", wallet);
        Intent restartServiceTask = new Intent(context,this.getClass());
        restartServiceTask.setAction(TmaAndroidUtil.START);
        restartServiceTask.setPackage(getPackageName());
        restartServiceTask.putExtra("wallet", wallet);
        PendingIntent restartPendingIntent =PendingIntent.getService(context, 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);
    }


    private void run(final Intent intent) {
        if(wallet != null) {
            return;
        }
        startForeground();
        ThreadExecutor.getInstance().execute(new TmaRunnable("NewMessageNotifier") {
            public void doRun() {
                setup(intent);
                if(wallet == null) {
                    return;
                }
                PowerManager.WakeLock wakeLock = null;
                try {
                    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TMAPost::MyWakelockTag");
                    wakeLock.acquire();

                    while (true && !TmaAndroidUtil.STOP.equals(action)) {
                        try {
                            process();
                            ThreadExecutor.sleep(Constants.TIMEOUT);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if(wakeLock != null) {
                        wakeLock.release();
                    }

                }
            }
        });
    }

    public void process() {
        Network network = Network.getInstance();
        TmaAndroidUtil.checkNetwork();
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForeground() {
        logger.debug("startForeground");
        createNotificationChannel();
        Intent notificationIntent = new Intent(context, ShowMessagesActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, AndroidContants.TMACOIN);
        Notification notification = notificationBuilder
                .setOngoing(true)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getResources().getString(R.string.new_messages_listener))
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build();
        startForeground(1, notification);
    }

}
