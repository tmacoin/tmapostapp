package org.tmacoin.post.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Wallet;
import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.thin.GetBalanceRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaRunnable;
import org.tmacoin.post.android.messaging.NewMessageNotifier;
import org.tmacoin.post.android.messaging.SendMessageActivity;

public class ConnectedToNetworkActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_to_network);
        final TextView address = findViewById(R.id.address);
        address.setText(Network.getInstance().getTmaAddress());
        final TextView balance = findViewById(R.id.balance);
        balance.setText(getResources().getString(R.string.get_balance_wait));
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, getResources().getString(R.string.get_balance_wait), Toast.LENGTH_LONG).show();

        new AndroidExecutor() {
            private String balance;
            @Override
            public void start() throws Exception {
                balance = getBalance(3);
            }

            @Override
            public void finish() throws Exception {
                complete(balance);
            }
        }.run();

    }



    private void complete(final String balance) {
        final TextView balanceTextView = findViewById(R.id.balance);
        balanceTextView.setText(balance + getResources().getString(R.string.coins));
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.INVISIBLE);
        Intent service = new Intent(this, NewMessageNotifier.class);
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        service.putExtra("wallet", wallet);
        startService(service);
    }

    private String getBalance(int attemptNumber) {
        if(attemptNumber < 0) {
            return null;
        }
        Network network = Network.getInstance();
        updateStatus("Network status: " + network.getPeerCount().toString());
        if(!network.isPeerSetComplete()) {
            new BootstrapRequest(network).start();
        }
        updateStatus("Network status: " + network.getPeerCount().toString());
        GetBalanceRequest request = new GetBalanceRequest(network, network.getTmaAddress());
        request.start();
        String balance = (String) ResponseHolder.getInstance().getObject(request.getCorrelationId());
        if(balance == null) {
            balance = getBalance(--attemptNumber);
        }
        updateStatus("Network status: " + network.getPeerCount().toString());
        return balance;
    }

}
