package org.tmacoin.post.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.peer.Network;
import org.tma.peer.Peer;
import org.tma.util.Coin;
import org.tma.util.Constants;
import org.tma.util.StringUtil;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;

import java.util.List;

public class ShowTransactionsActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private String address;
    private Coin minimum;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_transactions);
        Button showTransactionsButton = findViewById(R.id.showTransactionsButton);
        showTransactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked();
            }
        });
    }

    private void buttonClicked() {
        load();
        if(!validate()) {
            showAlert();
            return;
        }
        setContentView(R.layout.activity_show_transactions_wait);
        process();
    }

    private void process() {
        new AndroidExecutor() {

            @Override
            public void start() throws Exception {
                processAsync();
            }

            @Override
            public void finish() throws Exception {
                processSync();
            }
        }.run();
    }

    private void processAsync() {
    }

    private void processSync() {
        setContentView(R.layout.activity_show_transactions_complete);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
    }

    private boolean validate() {
        if(!StringUtil.isTmaAddressValid(address)) {
            return false;
        }
        if(minimum == null) {
            return false;
        }
        return true;
    }

    private void showAlert() {
        Toast.makeText(this, getResources().getString(R.string.entered_info_not_correct), Toast.LENGTH_LONG).show();
    }

    private void load() {
        EditText addressEditText = findViewById(R.id.address);
        address = addressEditText.getText().toString().trim();
        EditText minimumEditText = findViewById(R.id.minimum);
        try {
            minimum = Coin.ONE.multiply(Double.parseDouble(minimumEditText.getText().toString().trim()));
        } catch (Exception e) {

        }
    }


}
