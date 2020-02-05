package org.tmacoin.post.android;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.tma.util.Constants;
import org.tma.util.StringUtil;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class LogViewerActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private long pointer;
    private FileObserver observer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);
        final TextView logContent = findViewById(R.id.log_content);
        logContent.setMovementMethod(new ScrollingMovementMethod());
        final File log = new File(getFilesDir(), "config/tmapost.log");
        if(!log.exists()) {
            logContent.setText(getResources().getString(R.string.file_does_not_exist) + log.getAbsolutePath());
            return;
        }

        readLog(log);

        observer = new FileObserver(log.getAbsolutePath(), FileObserver.MODIFY) {

            @Override
            public void onEvent(int event, @Nullable String path) {
                readLog(log);
            }
        };

        observer.startWatching();

    }

    private void readLog(File file) {
        final StringBuilder builder = new StringBuilder();
        RandomAccessFile randomAccessFile = null;
        FileInputStream fis = null;
        ByteArrayOutputStream result = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long fileLength = file.length() - 1;
            if(pointer == 0) {
                pointer = Math.max(0, fileLength - 10000);
            }
            randomAccessFile.seek(pointer);

            fis = new FileInputStream(randomAccessFile.getFD());

            result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                result.write(buffer, 0, length);
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
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        final String text;
        try {
            text = result.toString(StandardCharsets.UTF_8.name());
            if(StringUtil.isEmpty(text)) {
                return;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
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
