package org.tmacoin.post.android.messaging;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.tma.blockchain.Wallet;
import org.tma.peer.Network;
import org.tma.peer.thin.GetMessagesRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SecureMessage;
import org.tma.util.Encryptor;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.MainActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.TmaAndroidUtil;
import org.tmacoin.post.android.Wallets;
import org.tmacoin.post.android.persistance.AddressStore;
import org.tmacoin.post.android.persistance.MessageStore;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ShowMessagesActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final Encryptor encryptor = new Encryptor();

    private List<SecureMessage> list = null;
    private String activeView = "messages";
    private AddressStore addressStore;
    private MessageStore messageStore;
    private MessageAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Network.getInstance() == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }
        setContentView(R.layout.activity_show_messages);
        addressStore = new AddressStore(getApplicationContext());
        messageStore = new MessageStore(getApplicationContext());
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.VISIBLE);
        updateStatus(getResources().getString(R.string.retrieving_messages_wait));
        final SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        list = null;
                        process();

                    }
                }
        );
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
        updateStatus(getResources().getString(R.string.network_status) + ": " + network.getPeerCount().toString());
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        int attempt = 0;
        while(list == null && attempt++ < 10) {
            TmaAndroidUtil.checkNetwork();
            updateStatus(getResources().getString(R.string.network_status) + ": " + network.getPeerCount().toString());
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
        cleanIds();
        updateStatus(getResources().getString(R.string.network_status) + ": " + network.getPeerCount().toString());
    }

    private void cleanIds() {
        List<String> messageIds = new ArrayList<>();
        for(SecureMessage secureMessage: list) {
            messageIds.add(secureMessage.getTransactionId());
        }

        List<String> transactionIds = messageStore.getAll();
        transactionIds.removeAll(messageIds);
        for(String transactionId: transactionIds) {
            messageStore.deleteByTransactionId(transactionId);
        }
    }

    private void hideMessages() {
        List<String> transactionIds = messageStore.getAll();
        Iterator<SecureMessage> iterator = list.iterator();
        while(iterator.hasNext()) {
            SecureMessage secureMessage = iterator.next();
            if(transactionIds.contains(secureMessage.getTransactionId())) {
                iterator.remove();
            }
        }
    }

    private void processSync() {
        final SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        pgsBar.setVisibility(View.GONE);
        if(list == null) {
            updateStatus(getResources().getString(R.string.fail_retrieve_messages));
            Toast.makeText(this, getResources().getString(R.string.fail_retrieve_messages), Toast.LENGTH_LONG).show();
            swipeRefresh.setRefreshing(false);
            return;
        }
        hideMessages();
        ListView listView = findViewById(R.id.simpleListView);
        arrayAdapter = new MessageAdapter(this, list, addressStore);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SecureMessage secureMessage = (SecureMessage) parent.getItemAtPosition(position);
                showMessage(secureMessage);
            }
        });
        swipeRefresh.setRefreshing(false);
    }

    private void showMessage(final SecureMessage secureMessage) {
        String date = new Date(secureMessage.getTimeStamp()).toString();
        String expire = new Date(secureMessage.getTimeStamp() + secureMessage.getExpire() * 60000).toString();
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        String subject = secureMessage.getSubject(wallet.getPrivateKey());
        TextView sender = findViewById(R.id.sender);

        String senderTmaAddress = secureMessage.getSenderTmaAddress();
        String name = addressStore.findNameByTmaAddress(senderTmaAddress);
        if(name == null) {
            sender.setText(senderTmaAddress);
        } else {
            sender.setText(name);
        }

        sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                senderClicked(secureMessage);
            }
        });

        sender.setPaintFlags(sender.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        TextView recipient = findViewById(R.id.recipient);

        String recipientName = addressStore.findNameByTmaAddress(secureMessage.getRecipient());
        if(recipientName == null) {
            recipientName = secureMessage.getRecipient();
        }

        recipient.setText(recipientName);
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
        String bodyText = secureMessage.getBody(wallet.getPrivateKey());
        bodyText = bodyText == null? "": bodyText;
        body.setText(bodyText);
        ConstraintLayout messages = findViewById(R.id.messages);
        messages.setVisibility(View.GONE);
        ScrollView message = findViewById(R.id.message);
        message.setVisibility(View.VISIBLE);
        activeView = "message";

        Button reply = findViewById(R.id.reply);
        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(secureMessage);
            }
        });

        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(secureMessage);
            }
        });
    }

    private void delete(SecureMessage secureMessage) {
        messageStore.save(secureMessage.getTransactionId());
        list.remove(secureMessage);
        arrayAdapter.notifyDataSetChanged();
        showMessages();
    }

    private void senderClicked(SecureMessage secureMessage) {
        Intent intent = new Intent(this, AddAddressActivity.class);
        intent.putExtra("secureMessage", secureMessage);
        startActivity(intent);
    }

    private void buttonClicked(SecureMessage secureMessage) {
        Intent intent = new Intent(this, SendMessageActivity.class);
        intent.putExtra("secureMessage", secureMessage);
        startActivity(intent);
    }

    private void showMessages() {
        activeView = "messages";
        ConstraintLayout messages = findViewById(R.id.messages);
        messages.setVisibility(View.VISIBLE);
        ScrollView message = findViewById(R.id.message);
        message.setVisibility(View.GONE);
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
                showMessages();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        if(addressStore != null) {
            addressStore.onDestroy();
        }
        if(messageStore != null) {
            messageStore.onDestroy();
        }
        super.onDestroy();
    }

}
