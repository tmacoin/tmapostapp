package org.tmacoin.post.android.messaging;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.tma.blockchain.Wallet;
import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.thin.GetMessagesRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SecureMessage;
import org.tma.util.Base58;
import org.tma.util.Encryptor;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ShowMessagesActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final Encryptor encryptor = new Encryptor();

    private List<SecureMessage> list = null;
    private String activeView = "messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_messages);
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.VISIBLE);
        updateStatus(getResources().getString(R.string.retrieving_messages_wait));
        process();

    }

    private void process() {
        new AndroidExecutor() {

            @Override
            public void start() {
                processAsync();
            }

            @Override
            public void finish() {
                processSync();
            }
        }.run();
    }

    @SuppressWarnings("unchecked")
    private void processAsync() {
        Network network = Network.getInstance();
        updateStatus("Network status: " + network.getPeerCount().toString());
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        int attempt = 0;
        while(list == null && attempt++ < 5) {
            if(!network.isPeerSetComplete()) {
                new BootstrapRequest(network).start();
            }
            updateStatus("Network status: " + network.getPeerCount().toString());
            PublicKey publicKey = wallet.getPublicKey();
            GetMessagesRequest request = new GetMessagesRequest(network, publicKey);
            request.start();
            list = (List<SecureMessage>) ResponseHolder.getInstance().getObject(request.getCorrelationId());
        }

        if(list == null) {
            updateStatus("Could not retrieve messages. Please try again.");
            return;
        }

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
            Toast.makeText(this, getResources().getString(R.string.fail_retrieve_messages), Toast.LENGTH_LONG).show();
            return;
        } else {
            updateStatus("Retrieved " + list.size() + " messages");
        }
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.GONE);
        ListView listView = findViewById(R.id.simpleListView);
        MessageAdapter arrayAdapter = new MessageAdapter(this, list);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SecureMessage secureMessage = (SecureMessage) parent.getItemAtPosition(position);
                showMessage(secureMessage);
            }
        });
        TextView statusBar = findViewById(R.id.statusBar);
        statusBar.setVisibility(View.GONE);
    }

    private void showMessage(SecureMessage secureMessage) {
        String date = new Date(secureMessage.getTimeStamp()).toString();
        String expire = new Date(secureMessage.getTimeStamp() + secureMessage.getExpire() * 60000).toString();
        String subject = getSubject(secureMessage);
        logger.debug("date={}, expire={}, subject={}", date, expire, subject);
        TextView sender = findViewById(R.id.sender);
        sender.setText(StringUtil.getStringFromKey(secureMessage.getSender()));
        TextView recipient = findViewById(R.id.recipient);
        recipient.setText(secureMessage.getRecipient());
        TextView value = findViewById(R.id.value);
        value.setText(secureMessage.getValue().toNumberOfCoins());
        TextView fee = findViewById(R.id.fee);
        fee.setText(secureMessage.getFee().toNumberOfCoins());

        TextView dateTextView = findViewById(R.id.singleDateTextView);
        dateTextView.setText(date);

        TextView expireTextView = findViewById(R.id.expire);
        expireTextView.setText(expire);

        TextView subjectTextView = findViewById(R.id.singleSubjectTextView);
        subjectTextView.setText(subject);

        TextView body = findViewById(R.id.body);
        body.setText(getBody(secureMessage));
        ConstraintLayout messages = findViewById(R.id.messages);
        messages.setVisibility(View.GONE);
        ConstraintLayout message = findViewById(R.id.message);
        message.setVisibility(View.VISIBLE);
        activeView = "message";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case android.R.id.home:
                if("messages".equals(activeView)) {
                    onBackPressed();
                    return true;
                }
                activeView = "messages";
                ConstraintLayout messages = findViewById(R.id.messages);
                messages.setVisibility(View.VISIBLE);
                ConstraintLayout message = findViewById(R.id.message);
                message.setVisibility(View.GONE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getSubject(SecureMessage secureMessage) {
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        if(!secureMessage.getRecipient().equals(wallet.getTmaAddress())) {
            return "";
        }
        try {
            String str = StringUtil.trimToNull(secureMessage.getText());
            if(str != null) {
                str = new String(encryptor.decryptAsymm(Base58.decode(str), wallet.getPrivateKey()), StandardCharsets.UTF_8);
                int index = str.indexOf("\n");
                index = index == -1? str.length(): index;
                return str.substring(0, index);
            }
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    private String getBody(SecureMessage secureMessage) {
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        if(!secureMessage.getRecipient().equals(wallet.getTmaAddress())) {
            return "";
        }
        try {
            String str = StringUtil.trimToNull(secureMessage.getText());
            if(str != null) {
                str = new String(encryptor.decryptAsymm(Base58.decode(str), wallet.getPrivateKey()), StandardCharsets.UTF_8);
                int index = str.indexOf("\n");
                index = index == -1? str.length(): index;
                return  str.substring(index + 1);
            }
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }
}
