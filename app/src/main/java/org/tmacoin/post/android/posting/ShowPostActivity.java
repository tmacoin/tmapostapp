package org.tmacoin.post.android.posting;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Keywords;
import org.tma.blockchain.Transaction;
import org.tma.blockchain.TransactionOutput;
import org.tma.blockchain.Wallet;
import org.tma.peer.Network;
import org.tma.peer.SendTransactionRequest;
import org.tma.peer.thin.GetInputsRequest;
import org.tma.peer.thin.GetKeywordsRequest;
import org.tma.peer.thin.Ratee;
import org.tma.peer.thin.Rating;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SearchRatingForRaterRequest;
import org.tma.peer.thin.SearchRatingRequest;
import org.tma.util.Applications;
import org.tma.util.Coin;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.R;

import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.TmaAndroidUtil;
import org.tmacoin.post.android.Wallets;
import org.tmacoin.post.android.messaging.SendMessageActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShowPostActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private String result = "";
    private List<Rating> list;
    private Ratee ratee;
    private View header;
    private String comment;
    private String rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post_wait);
        ratee = (Ratee) getIntent().getSerializableExtra("ratee");
        process();
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
        findRatings();
    }

    private void findRatings() {
        Network network = Network.getInstance();
        TmaAndroidUtil.checkNetwork();

        SearchRatingRequest request = new SearchRatingRequest(network, ratee.getName(), ratee.getTransactionId());
        request.start();

        list = (List<Rating>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(list == null) {
            result  = ("Failed to retrieve ratings. Please try again");
            return;
        }

        result = ("Total number of comments found for " + ratee.getName() + ": " + list.size());

    }

    private void processSync() {
        setContentView(R.layout.activity_show_post);

        if(!StringUtil.isEmpty(result)) {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
        if(list == null) {
            list = new ArrayList<>();
        }
        ListView listView = findViewById(R.id.simpleListView);
        RatingAdapter arrayAdapter = new RatingAdapter(this, list);
        listView.setAdapter(arrayAdapter);

        header =  getLayoutInflater().inflate(R.layout.activity_show_post_header, null);
        listView.addHeaderView(header, null, false);

        TextView textViewPost = header.findViewById(R.id.textViewPost);
        textViewPost.setText(ratee.getName());
        textViewPost.setPaintFlags(textViewPost.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        textViewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reply();
            }
        });


        TextView textViewDescription = header.findViewById(R.id.textViewDescription);
        textViewDescription.setText(ratee.getDescription());
        TextView textViewDate = header.findViewById(R.id.textViewDate);
        textViewDate.setText(new Date(ratee.getTimeStamp()).toString());
        TextView textViewIdentifier = header.findViewById(R.id.textViewIdentifier);
        textViewIdentifier.setText(ratee.getTransactionId());

        TextView totalRating = header.findViewById(R.id.totalRating);
        totalRating.setText("Total rating is " + ratee.getTotalRating());

        Button buttonAddRating = header.findViewById(R.id.buttonAddRating);
        buttonAddRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_show_post_wait);
                addRating();
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(
            new RadioGroup.OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(RadioGroup group, int checkedId) {
                  RadioButton radioButton = (RadioButton) findViewById(checkedId);
                  rating = radioButton.getText().toString();
              }
            }
        );

    }

    private void addRating() {
        new AndroidExecutor() {

            @Override
            public void start() throws Exception {
                load();
                addRatingAsync();
            }

            @Override
            public void finish() {
                processSync();
            }
        }.run();
    }

    private void load() {
        TextView editTextComment = header.findViewById(R.id.editTextComment);
        comment = StringUtil.trim(editTextComment.getText().toString());
    }

    private boolean addRatingAsync() {
        Network network = Network.getInstance();
        TmaAndroidUtil.checkNetwork();

        GetKeywordsRequest getKeywordsRequest = new GetKeywordsRequest(network, StringUtil.getTmaAddressFromString(ratee.getName()), ratee.getTransactionId());
        getKeywordsRequest.start();

        Keywords accountKeywords = (Keywords)ResponseHolder.getInstance().getObject(getKeywordsRequest.getCorrelationId());

        if(accountKeywords == null || accountKeywords.getMap().isEmpty()) {
            result = ("Could not retrieve any keywords for " + ratee.getName());
            return false;
        }


        String tmaAddress = network.getTmaAddress();
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        Coin amount = Coin.SATOSHI.multiply(2);
        List<Coin> totals = new ArrayList<Coin>();
        totals.add(amount);
        for(String word: accountKeywords.getMap().keySet()) {
            if(word.equals(accountKeywords.getMap().get(word))) {
                totals.add(amount);
            }
        }
        List<Set<TransactionOutput>> inputList = new GetInputsRequest(network, tmaAddress, totals).getInputlist();
        int i = 0;

        if(inputList.size() != totals.size()) {
            result = ("No inputs available for tma address " + tmaAddress + ". Please check your balance.");
            return false;
        }

        Keywords keywords = new Keywords();
        keywords.getMap().put("rater", wallet.getTmaAddress());
        keywords.getMap().put("ratee", ratee.getName());
        keywords.getMap().put("transactionId", ratee.getTransactionId());
        keywords.getMap().put("rating", rating);

        Transaction transaction = new Transaction(wallet.getPublicKey(), StringUtil.getTmaAddressFromString(ratee.getName()), Coin.SATOSHI, Coin.SATOSHI,
                inputList.get(i++), wallet.getPrivateKey(), comment, null, keywords);
        transaction.setApp(Applications.RATING);
        new SendTransactionRequest(network, transaction).start();
        logger.debug("sent {}", transaction);
        addRating(transaction);

        Map<String, String> map = new HashMap<>(keywords.getMap());
        map.remove("rater");
        for(String word: accountKeywords.getMap().keySet()) {
            if(word.equals(accountKeywords.getMap().get(word))) {
                Keywords words = new Keywords();
                words.getMap().putAll(map);
                transaction = new Transaction(wallet.getPublicKey(), StringUtil.getTmaAddressFromString(word), Coin.SATOSHI, Coin.SATOSHI,
                        inputList.get(i++), wallet.getPrivateKey(), comment, null, words);
                transaction.setApp(Applications.RATING);
                new SendTransactionRequest(network, transaction).start();
                logger.debug("sent {}", transaction);
            }
        }
        return true;
    }

    private void addRating(Transaction transaction) {
        Keywords keywords  = transaction.getKeywords();
        String rate = keywords.getMap().get("rating");
        Rating rating = new Rating();
        rating.setRater(Network.getInstance().getTmaAddress());
        rating.setComment(transaction.getData());
        rating.setRate(rate);
        rating.setRatee(keywords.getMap().get("ratee"));
        rating.setTimeStamp(System.currentTimeMillis());

        String transactionId = keywords.getMap().get("transactionId") ;
        rating.setTransactionId(transactionId);
        list.add(0, rating);
    }

    private void reply() {
        Intent intent = new Intent(this, SendMessageActivity.class);
        intent.putExtra("subject", ratee.getName());
        intent.putExtra("recipient", ratee.getCreatorTmaAddress());
        startActivity(intent);
    }


}
