package org.tmacoin.post.android.tmitter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Wallet;
import org.tma.peer.Network;
import org.tma.peer.thin.GetMyTweetsRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.Tweet;
import org.tma.peer.thin.TwitterAccount;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.TmaAndroidUtil;
import org.tmacoin.post.android.Wallets;
import org.tmacoin.post.android.persistance.SubscriptionStore;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ShowMyTweetsActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private TwitterAccount twitterAccount;
    private String tmaAddress;
    private String result = "";
    private Tweet title;
    private List<Tweet> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        twitterAccount = (TwitterAccount) getIntent().getSerializableExtra("twitterAccount");
        if(twitterAccount == null) {
            showMyTmeets();
            return;
        }
        tmaAddress = twitterAccount.getTmaAddress();
        process();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }

    private void showMyTmeets() {
        Wallets wallets = Wallets.getInstance();
        Collection<String> names = wallets.getNames(Wallets.TWITTER);
        if(names.isEmpty()) {
            setContentView(R.layout.activity_show_my_tweets_complete);
            result = getResources().getString(R.string.tmitter_account_create);
            showError(result);
        } else {
            setContentView(R.layout.activity_show_my_tmeets_wait);
            String accountName = names.iterator().next();
            Wallet twitterWallet = wallets.getWallet(Wallets.TWITTER, accountName);
            tmaAddress = twitterWallet.getTmaAddress();
            Toast.makeText(this, getResources().getString(R.string.wait), Toast.LENGTH_LONG).show();
            updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
            process();
        }
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(error);
        TextView textViewAccountName = findViewById(R.id.textViewAccountName);
        TextView textViewAccountDescription = findViewById(R.id.textViewAccountDescription);
        textViewAccountName.setVisibility(View.GONE);
        textViewAccountDescription.setVisibility(View.GONE);
        Button buttonSubscribe = findViewById(R.id.buttonSubscribe);
        buttonSubscribe.setVisibility(View.GONE);
        Button buttonUnsubscribe = findViewById(R.id.buttonUnsubscribe);
        buttonUnsubscribe.setVisibility(View.GONE);
    }

    private void process() {
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

    private void processAsync() throws Exception {
        Network network = Network.getInstance();
        TmaAndroidUtil.checkNetwork();

        GetMyTweetsRequest request = new GetMyTweetsRequest(network, tmaAddress);
        request.start();

        list = (List<Tweet>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(list == null) {
            result = "Failed to retrieve tmeets. Please try again";
            return;
        }

        for(Tweet tweet: list) {
            if(tweet.getKeywords() != null && tweet.getKeywords().get("create") != null) {
                title = tweet;
            }
        }

        Iterator<Tweet> i = list.iterator();

        while(i.hasNext()) {
            Tweet t = i.next();
            if (t.getKeywords() != null && (t.getKeywords().get("create") != null || t.getKeywords().get("transactionId") != null)) {
                i.remove();
            }
        }

        Comparator<Tweet> compareByTimestamp = new Comparator<Tweet>() {
            @Override
            public int compare(Tweet o1, Tweet o2) {
                return Long.valueOf(o2.getTimeStamp()).compareTo( o1.getTimeStamp() );
            }
        };

        Collections.sort(list, compareByTimestamp);

    }

    private void processSync() {
        setContentView(R.layout.activity_show_my_tweets_complete);

        if(title == null) {
            result = "Failed to retrieve tmeets. Please try again";
            showError(result);
            return;
        }

        TextView textViewAccountName = findViewById(R.id.textViewAccountName);
        TextView textViewAccountDescription = findViewById(R.id.textViewAccountDescription);
        TextView resultTextView = findViewById(R.id.resultTextView);

        textViewAccountName.setText(title.getKeywords().get("create"));
        textViewAccountDescription.setText(title.getText());
        resultTextView.setText(result);
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());

        ListView listView = findViewById(R.id.simpleListView);
        TmeetAdapter arrayAdapter = new TmeetAdapter(this, list);
        listView.setAdapter(arrayAdapter);

        doSubscriptionButtons();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tweet tweet = (Tweet) parent.getItemAtPosition(position);
                showClicked(tweet);
            }
        });
    }

    private void showClicked(Tweet tweet) {
        Intent intent = new Intent(this, ReplyMyTweetsActivity.class);
        intent.putExtra("tweet", tweet);
        startActivity(intent);
    }

    private void doSubscriptionButtons() {

        Button buttonSubscribe = findViewById(R.id.buttonSubscribe);
        buttonSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSubscribeClick();
            }
        });

        Button buttonUnsubscribe = findViewById(R.id.buttonUnsubscribe);
        buttonUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonUnsubscribeClick();
            }
        });


        if(twitterAccount == null) {
            buttonSubscribe.setVisibility(View.INVISIBLE);
            buttonUnsubscribe.setVisibility(View.INVISIBLE);
            return;
        }

        List<TwitterAccount> subscribedAccounts = SubscriptionStore.getInstance().getSubscriptions();
        if(subscribedAccounts.contains(twitterAccount)) {
            buttonSubscribe.setVisibility(View.INVISIBLE);
        } else {
            buttonUnsubscribe.setVisibility(View.INVISIBLE);
        }




    }

    private void buttonSubscribeClick() {
        SubscriptionStore.getInstance().save(twitterAccount);
        Button buttonSubscribe = findViewById(R.id.buttonSubscribe);
        buttonSubscribe.setVisibility(View.INVISIBLE);
        Button buttonUnsubscribe = findViewById(R.id.buttonUnsubscribe);
        buttonUnsubscribe.setVisibility(View.VISIBLE);
    }

    private void buttonUnsubscribeClick() {
        SubscriptionStore.getInstance().delete(twitterAccount);
        Button buttonUnsubscribe = findViewById(R.id.buttonUnsubscribe);
        buttonUnsubscribe.setVisibility(View.INVISIBLE);
        Button buttonSubscribe = findViewById(R.id.buttonSubscribe);
        buttonSubscribe.setVisibility(View.VISIBLE);
    }

}
