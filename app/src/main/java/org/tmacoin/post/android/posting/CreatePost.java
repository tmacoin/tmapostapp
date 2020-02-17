package org.tmacoin.post.android.posting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import org.tma.util.Applications;
import org.tma.util.Coin;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreatePost extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final int MAX_NUMBER_OF_KEYWORDS = 10;

    private String post;
    private String description;
    private Set<String> words;
    private String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        Button createpostAccount = findViewById(R.id.createPost);
        createpostAccount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        createpostAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                load();
                if(!validate()) {
                    return;
                }
                Toast.makeText(CreatePost.this, getResources().getString(R.string.wait), Toast.LENGTH_LONG).show();
                setContentView(R.layout.activity_create_post_wait);
                process();
            }
        });

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
        if(sendCreateRateeTransaction() != null) {
            result = "Post " + post + " was created successfully with keywords: " + words;
        }
    }

    private void processSync() {
        setContentView(R.layout.activity_create_post_complete);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }

    private boolean validate() {
        if (StringUtil.isEmpty(post)) {
            Toast.makeText(this, getResources().getString(R.string.post_cannot_be_empty), Toast.LENGTH_LONG).show();
            return false;
        }

        if (StringUtil.isEmpty(description)) {
            Toast.makeText(this, getResources().getString(R.string.description_cannot_be_empty), Toast.LENGTH_LONG).show();
            return false;
        }

        if (words.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.keywords_cannot_be_empty), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void load() {
        EditText editTextPost = findViewById(R.id.editTextPost);
        post = editTextPost.getText().toString();
        EditText editTextDescription = findViewById(R.id.editTextDescription);
        description = editTextDescription.getText().toString();
        EditText editTextKeywords = findViewById(R.id.editTextKeywords);
        String keywords = editTextKeywords.getText().toString();
        words = getKeywords(keywords);
    }

    public Set<String> getKeywords(String keywords) {
        Set<String> set = new HashSet<>();
        String[] strings = keywords.split(" ");
        for(String str: strings) {
            if(set.size() > MAX_NUMBER_OF_KEYWORDS) {
                break;
            }
            if(!"".equals(str)) {
                set.add(str.toLowerCase());
            }
        }
        return set;
    }

    private Transaction sendCreateRateeTransaction() {
        String ratee = StringUtil.getTmaAddressFromString(post);
        Network network = Network.getInstance();
        if(!network.isPeerSetComplete()) {
            BootstrapRequest.getInstance().start();
        }
        String tmaAddress = network.getTmaAddress();
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        Coin amount = Coin.SATOSHI.multiply(2);
        List<Coin> totals = new ArrayList<Coin>();
        totals.add(amount);
        for(@SuppressWarnings("unused") String word: words) {
            totals.add(amount);
        }
        List<Set<TransactionOutput>> inputList = new GetInputsRequest(network, tmaAddress, totals).getInputlist();
        int i = 0;

        if(inputList.size() != totals.size()) {
            result = "No inputs available for tma address " + tmaAddress + ". Please check your balance.";
            return null;
        }

        Set<TransactionOutput> inputs = inputList.get(i++);
        Keywords keywords = new Keywords();
        keywords.getMap().put("create", post);
        keywords.getMap().put("first", post);
        for(String word: words) {
            keywords.getMap().put(word, word);
        }

        Transaction transaction = new Transaction(wallet.getPublicKey(), ratee, Coin.SATOSHI, Coin.SATOSHI,
                inputs, wallet.getPrivateKey(), description, null, keywords);
        transaction.setApp(Applications.RATING);
        new SendTransactionRequest(network, transaction).start();
        logger.debug("sent {}", transaction);

        Map<String, String> map = keywords.getMap();

        for(String word: words) {
            keywords = new Keywords();
            keywords.getMap().putAll(map);
            keywords.getMap().put("transactionId", transaction.getTransactionId());
            keywords.getMap().put("first", word);
            inputs = inputList.get(i++);
            String recipient = StringUtil.getTmaAddressFromString(word);
            Transaction keyWordTransaction = new Transaction(wallet.getPublicKey(), recipient, Coin.SATOSHI, Coin.SATOSHI,
                    inputs, wallet.getPrivateKey(), description, null, keywords);
            keyWordTransaction.setApp(Applications.RATING);
            new SendTransactionRequest(network, keyWordTransaction).start();
            logger.debug("sent {}", keyWordTransaction);

        }

        return transaction;
    }
}
