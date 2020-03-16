package org.tmacoin.post.android;

import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Wallet;
import org.tma.peer.Network;
import org.tma.peer.thin.GetBalanceRequest;
import org.tma.peer.thin.NewMessageEvent;
import org.tma.peer.thin.ResponseHolder;
import org.tma.util.Listeners;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.messaging.NewMessageEventListener;
import org.tmacoin.post.android.messaging.NewMessageNotifier;

public class ConnectedToNetworkActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final Listeners listeners = Listeners.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_to_network);
        final TextView address = findViewById(R.id.address);
        address.setText(Network.getInstance().getTmaAddress());
        listeners.addEventListener(NewMessageEvent.class, new NewMessageEventListener(getApplicationContext()));
        startNotifier();
    }

    private void startNotifier() {
        Intent service = new Intent(this, NewMessageNotifier.class);
        service.setAction(TmaAndroidUtil.START);
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        service.putExtra("wallet", wallet);
        ContextCompat.startForegroundService(this, service);
    }

}
