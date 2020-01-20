package org.tmacoin.post.android.messaging;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Wallet;
import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.thin.GetMessagesRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SecureMessage;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.security.PublicKey;
import java.util.Iterator;
import java.util.List;

public class ShowMessagesActivity extends BaseActivity {

    private List<SecureMessage> list = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_messages);
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.VISIBLE);
        TextView myMessagesTextView = findViewById(R.id.myMessagesTextView);
        myMessagesTextView.setText("Secure messages for " + Network.getInstance().getTmaAddress());
        updateStatus(getResources().getString(R.string.retrieving_messages_wait));
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
        Network network = Network.getInstance();
        updateStatus("Network status: " + network.getPeerCount().toString());
        if(!network.isPeerSetComplete()) {
            new BootstrapRequest(network).start();
        }
        updateStatus("Network status: " + network.getPeerCount().toString());
        final Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        final PublicKey publicKey = wallet.getPublicKey();
        GetMessagesRequest request = new GetMessagesRequest(network, publicKey);
        request.start();
        list = (List<SecureMessage>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        Iterator<SecureMessage> iterator = list.iterator();

        while(iterator.hasNext()) {
            SecureMessage secureMessage = iterator.next();
            if (!secureMessage.getRecipient().equals(wallet.getTmaAddress())) {
                iterator.remove();
            }
        }

        updateStatus("Network status: " + network.getPeerCount().toString());
    }

    private void processSync() {
        if(list == null) {
            updateStatus(getResources().getString(R.string.fail_retrieve_messages));
            return;
        } else {
            updateStatus("Retrieved " + list.size() + " messages");
        }
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.INVISIBLE);
        ListView simpleList = (ListView)findViewById(R.id.simpleListView);
        MessageAdapter arrayAdapter = new MessageAdapter(this, list);
        simpleList.setAdapter(arrayAdapter);
    }
}
