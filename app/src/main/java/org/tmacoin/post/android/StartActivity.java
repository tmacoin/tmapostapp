package org.tmacoin.post.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.tma.util.TmaLogger;

public class StartActivity extends AppCompatActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.start_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuHandler menuHandler = new MenuHandler();
        Intent intent;
        switch (item.getItemId()) {

            case R.id.view_log:
                intent = new Intent(this, StartLogViewerActivity.class);
                startActivity(intent);
                return true;

            case R.id.show_peers:
                intent = new Intent(this, StartShowPeersActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateStatus(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                try {
                    TextView statusBar = findViewById(R.id.statusBar);
                    if(statusBar == null) {
                        return;
                    }
                    statusBar.setText(message);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }


}
