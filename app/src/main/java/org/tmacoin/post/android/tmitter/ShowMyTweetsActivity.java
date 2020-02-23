package org.tmacoin.post.android.tmitter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Wallet;
import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.thin.GetMyTweetsRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.Tweet;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ShowMyTweetsActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private String tmaAddress;
    private String result = "";
    private Tweet title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Wallets wallets = Wallets.getInstance();
        Collection<String> names = wallets.getNames(Wallets.TWITTER);
        if(names.isEmpty()) {
            setContentView(R.layout.activity_show_my_tweets_complete);
            result = getResources().getString(R.string.tmitter_account_create);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            TextView resultTextView = findViewById(R.id.resultTextView);
            resultTextView.setText(result);
        } else {
            setContentView(R.layout.activity_show_my_tmeets_wait);
            String accountName = names.iterator().next();
            Wallet twitterWallet = wallets.getWallet(Wallets.TWITTER, accountName);
            tmaAddress = twitterWallet.getTmaAddress();
            Toast.makeText(this, getResources().getString(R.string.wait), Toast.LENGTH_LONG).show();
            updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
            process();
        }
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
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
        if(!network.isPeerSetComplete()) {
            BootstrapRequest.getInstance().start();
        }

        GetMyTweetsRequest request = new GetMyTweetsRequest(network, tmaAddress);
        request.start();
        @SuppressWarnings("unchecked")
        List<Tweet> list = (List<Tweet>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(list == null) {
            result = "Failed to retrieve transactions. Please try again";
            return;
        }

        for(Tweet tweet: list) {
            if(tweet.getKeywords() != null && tweet.getKeywords().getMap().get("create") != null) {
                title = tweet;
            }
        }

        Iterator<Tweet> i = list.iterator();

        while(i.hasNext()) {
            Tweet t = i.next();
            if (t.getKeywords() != null && (t.getKeywords().getMap().get("create") != null || t.getKeywords().getMap().get("transactionId") != null)) {
                i.remove();
            }
        }

        result = "Retrieved number of tmeets " + list.size();

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

        TextView textViewAccountName = findViewById(R.id.textViewAccountName);
        textViewAccountName.setText(title.getKeywords().getMap().get("create"));
        TextView textViewAccountDescription = findViewById(R.id.textViewAccountDescription);
        textViewAccountDescription.setText(title.getText());

        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }
}
