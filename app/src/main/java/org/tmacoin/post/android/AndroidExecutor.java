package org.tmacoin.post.android;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;

public abstract class AndroidExecutor {

    private static final TmaLogger logger = TmaLogger.getLogger();

    public abstract void start() throws Exception;
    public abstract void finish() throws Exception;
    public void run() {
        ThreadExecutor.getInstance().execute(new TmaRunnable("Android Executor") {
            public void doRun() {
                try {
                    start();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                new Handler(Looper.getMainLooper()).post(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            finish();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                });
            }
        });
    }

}
