package org.tmacoin.post.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.thin.GetBalanceRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaRunnable;

public class GetBalanceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_balance);
        Button getBalanceButton = findViewById(R.id.get_balance_button);
        final EditText addressEditText = findViewById(R.id.editText);

        addressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        addressEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideKeyboard(v);
                    buttonClicked();
                }
                return false;
            }
        });

        getBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked();
            }
        });
    }

    private void buttonClicked() {
        final EditText addressEditText = findViewById(R.id.editText);
        final String tmaAddress = addressEditText.getText().toString();
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, getResources().getString(R.string.get_balance_wait), Toast.LENGTH_LONG).show();

        new AndroidExecutor() {
            private String balance;
            @Override
            public void start() throws Exception {
                balance = getBalance(3, tmaAddress);
            }

            @Override
            public void finish() throws Exception {
                complete(balance, tmaAddress);
            }
        }.run();
    }

    private void complete(final String balance, final String tmaAddress) {
        setContentView(R.layout.activity_show_balance);
        final TextView addressTextView = findViewById(R.id.tma_address_textView);
        final TextView balanceTextView = findViewById(R.id.balance_textView);
        addressTextView.setText(tmaAddress);
        balanceTextView.setText(balance + getResources().getString(R.string.coins));
    }

    private String getBalance(int attemptNumber, String tmaAddress) {
        if(attemptNumber < 0) {
            return null;
        }
        Network network = Network.getInstance();
        if(!network.isPeerSetComplete()) {
            new BootstrapRequest(network).start();
        }

        GetBalanceRequest request = new GetBalanceRequest(network, tmaAddress);
        request.start();
        String balance = (String) ResponseHolder.getInstance().getObject(request.getCorrelationId());
        if(balance == null) {
            balance = getBalance(--attemptNumber, tmaAddress);
        }

        return balance;
    }
}
