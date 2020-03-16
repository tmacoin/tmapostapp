package org.tmacoin.post.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.tma.blockchain.Wallet;
import org.tma.peer.Network;
import org.tma.peer.thin.GetBalanceRequest;
import org.tma.peer.thin.NewMessageEvent;
import org.tma.peer.thin.ResponseHolder;
import org.tma.util.Listeners;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.messaging.NewMessageEventListener;
import org.tmacoin.post.android.messaging.NewMessageNotifier;

public class GetMyBalanceActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final Listeners listeners = Listeners.getInstance();
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_balance);
        final TextView balance = findViewById(R.id.balance);
        balance.setText(getResources().getString(R.string.get_balance_wait));


        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.VISIBLE);
        toast = Toast.makeText(this, getResources().getString(R.string.get_balance_wait), Toast.LENGTH_LONG);
        toast.show();

        new AndroidExecutor() {
            private String balance;
            @Override
            public void start() throws Exception {
                balance = getBalance(10);
            }

            @Override
            public void finish() throws Exception {
                complete(balance);
            }
        }.run();
        listeners.addEventListener(NewMessageEvent.class, new NewMessageEventListener(getApplicationContext()));
    }



    private void complete(final String balance) {
        final TextView balanceTextView = findViewById(R.id.balance);
        if(balance == null) {
            balanceTextView.setText(getResources().getString(R.string.fail_retrieve_balance));
            updateStatus(getResources().getString(R.string.fail_retrieve_balance));
            Toast.makeText(this, getResources().getString(R.string.fail_retrieve_balance), Toast.LENGTH_LONG).show();
        } else {
            balanceTextView.setText(balance + " " + getResources().getString(R.string.coins));
        }
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.INVISIBLE);
        Intent service = new Intent(this, NewMessageNotifier.class);
        toast.cancel();
        service.setAction(TmaAndroidUtil.START);
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        service.putExtra("wallet", wallet);
        ContextCompat.startForegroundService(this, service);
    }

    private String getBalance(int attemptNumber) {
        String balance = null;
        int i = attemptNumber;
        Network network = Network.getInstance();
        while(balance == null && i > 0 ) {
            logger.debug("getBalance attempt {}", attemptNumber - i);
            updateStatus(getResources().getString(R.string.network_status) + ": " + network.getPeerCount().toString());
            TmaAndroidUtil.checkNetwork();
            updateStatus(getResources().getString(R.string.network_status) + ": " + network.getPeerCount().toString());
            GetBalanceRequest request = new GetBalanceRequest(network, network.getTmaAddress());
            request.start();
            balance = (String) ResponseHolder.getInstance().getObject(request.getCorrelationId());
            i--;
        }
        updateStatus(getResources().getString(R.string.network_status) + ": " + network.getPeerCount().toString());
        return balance;
    }

}
