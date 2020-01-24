package org.tmacoin.post.android.messaging;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.tma.blockchain.Wallet;
import org.tma.util.Constants;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;

public class RestarterService extends Service {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private Context context;
    private Wallet wallet;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(context == null) {
            context = getApplicationContext();
            wallet = (Wallet)intent.getSerializableExtra("wallet");
            run();
        }
        return Service.START_REDELIVER_INTENT;
    }

    private void run() {

        ThreadExecutor.getInstance().execute(new TmaRunnable("NewMessageNotifier") {
            public void doRun() {
                while(true) {
                    try {
                        process();
                        ThreadExecutor.sleep(Constants.ONE_MINUTE);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });

    }

    private void process() {
        if(isMyServiceRunning(NewMessageNotifier.class)) {
            return;
        }
        Intent service = new Intent(this, NewMessageNotifier.class);
        service.putExtra("wallet", wallet);
        startService(service);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
