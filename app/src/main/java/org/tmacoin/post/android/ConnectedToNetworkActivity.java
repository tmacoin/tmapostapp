package org.tmacoin.post.android;

import androidx.appcompat.app.AppCompatActivity;

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

import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.thin.GetBalanceRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaRunnable;

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
    }

    private String getBalance(int attempNumber) {
        if(attempNumber < 0) {
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
            balance = getBalance(--attempNumber);
        }
        updateStatus("Network status: " + network.getPeerCount().toString());
        return balance;
    }

}
