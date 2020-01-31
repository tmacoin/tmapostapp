package org.tmacoin.post.android;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.tma.util.Constants;
import org.tma.util.StringUtil;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class LogViewerActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private long pointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);
        final TextView logContent = findViewById(R.id.log_content);
        logContent.setMovementMethod(new ScrollingMovementMethod());
        final File log = new File(getFilesDir(), "config/tmapost.log");
        if(!log.exists()) {
            logContent.setText("File does not exists: " + log.getAbsolutePath());
            return;
        }



        ThreadExecutor.getInstance().execute(new TmaRunnable("LogViewerActivity") {
            public void doRun() {
                while(true) {
                    ThreadExecutor.sleep(Constants.ONE_SECOND);
                    readLog(log);
                    if(!logContent.isShown()) {
                        logger.debug("Exiting LogViewerActivity Loop");
                        break;
                    }
                }
            }
        });

    }

    private void readLog(File file) {
        final StringBuilder builder = new StringBuilder();
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long fileLength = file.length() - 1;
            if(pointer == 0) {
                pointer = Math.max(0, fileLength - 10000);
            }
            while(pointer < fileLength) {
                pointer++;
                randomAccessFile.seek(pointer);
                char c = (char) randomAccessFile.read();
                builder.append(c);
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        final String text = builder.toString();
        if(StringUtil.isEmpty(text)) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                try {
                    final TextView logContent = findViewById(R.id.log_content);
                    logContent.append(text);
                    logContent.scrollTo(0, Integer.MAX_VALUE);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

}
