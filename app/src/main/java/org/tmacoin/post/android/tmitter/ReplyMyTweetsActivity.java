package org.tmacoin.post.android.tmitter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Keywords;
import org.tma.blockchain.Transaction;
import org.tma.blockchain.TransactionOutput;
import org.tma.blockchain.Wallet;
import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.SendTransactionRequest;
import org.tma.peer.thin.GetInputsRequest;
import org.tma.peer.thin.GetRepliesRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.Tweet;
import org.tma.peer.thin.TwitterAccount;
import org.tma.util.Applications;
import org.tma.util.Coin;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.TmaAndroidUtil;
import org.tmacoin.post.android.Wallets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ReplyMyTweetsActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private String result = "";
    private Tweet tweet;
    private List<Tweet> list;
    private View header;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respond_tmeet_wait);
        Intent intent = getIntent();
        tweet = (Tweet) getIntent().getSerializableExtra("tweet");
        displayData();
    }

    private void replyButtonClicked() {
        setContentView(R.layout.activity_respond_tmeet_wait);
        processReply();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }

    private void displayData() {
        processView();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }

    private void processView() {
        new AndroidExecutor() {

            @Override
            public void start() throws Exception {
                processAsync();
            }

            @Override
            public void finish() {
                processSync();
            }
        }.run();
    }

    private void processReply() {
        new AndroidExecutor() {

            @Override
            public void start() throws Exception {
                processAsyncReply();
            }

            @Override
            public void finish() {
                processSync();
            }
        }.run();
    }



    private void processAsync() throws Exception {
        Network network = Network.getInstance();
        if(!network.isPeerSetComplete()) {
            BootstrapRequest.getInstance().start();
        }
        GetRepliesRequest request = new GetRepliesRequest(network, tweet.getTransactionId(), tweet.getRecipient());
        request.start();
        list = (List<Tweet>) ResponseHolder.getInstance().getObject(request.getCorrelationId());
        Comparator<Tweet> compareByTimestamp = new Comparator<Tweet>() {
            @Override
            public int compare(Tweet o1, Tweet o2) {
                return Long.valueOf(o2.getTimeStamp()).compareTo( o1.getTimeStamp() );
            }
        };

        Collections.sort(list, compareByTimestamp);
    }

    private void processSync() {
        setContentView(R.layout.activity_respond_tmeet_complete);
        ListView listView = findViewById(R.id.simpleListView);
        TmeetAdapter arrayAdapter = new TmeetAdapter(this, list);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tweet tweet = (Tweet) parent.getItemAtPosition(position);
                showClicked(tweet);
            }
        });

        if(!StringUtil.isEmpty(result)) {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }

        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());

        header =  getLayoutInflater().inflate(R.layout.activity_respond_tmeet_header, null);
        listView.addHeaderView(header, null, false);

        TextView textViewAccountName = header.findViewById(R.id.textViewAccountName);
        textViewAccountName.setText(tweet.getKeywords().getMap().get("from"));
        TextView textViewAccountDescription = header.findViewById(R.id.textViewAccountDescription);
        textViewAccountDescription.setText(tweet.getText());
        TextView textViewDate = header.findViewById(R.id.dateTextView);
        textViewDate.setText(new Date(tweet.getTimeStamp()).toString());

        final Button buttonReply = header.findViewById(R.id.sendReplyButton);
        buttonReply.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        buttonReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                replyButtonClicked();

            }
        });

        TextView textViewEnterReply = header.findViewById(R.id.textViewEnterReply);
        textViewEnterReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText replyDataEditText = header.findViewById(R.id.replyDataEditText);
                replyDataEditText.setVisibility(View.VISIBLE);
                buttonReply.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showClicked(Tweet tweet) {
        setContentView(R.layout.activity_respond_tmeet_wait);
        this.tweet = tweet;
        displayData();
    }

    private void processAsyncReply() {

        Network network = Network.getInstance();
        String tmaAddress = network.getTmaAddress();
        TmaAndroidUtil.checkNetwork();
        Wallets wallets = Wallets.getInstance();
        Wallet wallet = wallets.getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        Coin amount = Coin.SATOSHI.multiply(2);
        List<Coin> totals = new ArrayList<Coin>();
        totals.add(amount);
        List<Set<TransactionOutput>> inputList = new GetInputsRequest(network, tmaAddress, totals).getInputlist();
        int i = 0;

        if(inputList.size() != totals.size()) {
            result = "No inputs available for tma address " + tmaAddress + ". Please check your balance.";
            return;
        }

        Set<TransactionOutput> inputs = inputList.get(i++);

        Keywords keywords = new Keywords();
        keywords.getMap().put("transactionId", tweet.getTransactionId());
        Collection<String> names = wallets.getNames(Wallets.TWITTER);
        if (!names.isEmpty()) {
            String accountName = names.iterator().next();
            keywords.getMap().put("from", accountName);
        } else {
            result = "You have not created your tmitter account yet.";
            return;
        }
        EditText replyDataEditText = header.findViewById(R.id.replyDataEditText);
        String replyData = StringUtil.trim(replyDataEditText.getText().toString());
        Transaction transaction = new Transaction(wallet.getPublicKey(), tweet.getRecipient(), Coin.SATOSHI, Coin.SATOSHI,
                inputs, wallet.getPrivateKey(), replyData, null, keywords);
        transaction.setApp(Applications.TWITTER);

        new SendTransactionRequest(network, transaction).start();
        logger.debug("sent {}", transaction);
        result = "Reply successfully sent!";
        replyDataEditText.getText().clear();

        Tweet tweet = new Tweet();
        tweet.setTransactionId(transaction.getTransactionId());
        tweet.setSender(transaction.getSenderAsString());
        tweet.setText(transaction.getData());
        tweet.setRecipient(transaction.getRecipient());
        tweet.setTimeStamp(System.currentTimeMillis());
        tweet.setKeywords(transaction.getKeywords());
        list.add(0, tweet);

    }




}
