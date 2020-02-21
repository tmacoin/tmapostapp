package org.tmacoin.post.android.tmitter;

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
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SendTmeetActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private String tmeet;
    private String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_tmeet);
        Button buttonSendTmeet = findViewById(R.id.buttonSendTmeet);
        buttonSendTmeet.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        buttonSendTmeet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                load();
                if(!validate()) {
                    return;
                }
                Toast.makeText(SendTmeetActivity.this, getResources().getString(R.string.wait), Toast.LENGTH_LONG).show();
                setContentView(R.layout.activity_send_tmeet_wait);
                updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
                process();
            }
        });
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
        if(sendTweet()) {
            result = getResources().getString(R.string.tweet_was_sent_successfully);
        }
    }

    private boolean sendTweet() {

        Wallets wallets = Wallets.getInstance();
        Collection<String> names = wallets.getNames(Wallets.TWITTER);
        if(names.isEmpty()) {
            result = ("Please create your tmitter account first.");
            return false;
        }
        String accountName = names.iterator().next();
        Wallet twitterWallet = wallets.getWallet(Wallets.TWITTER, accountName);

        return sendTweetTransaction(twitterWallet.getTmaAddress());
    }

    private boolean sendTweetTransaction(String twitterTmaAddress) {
        Network network = Network.getInstance();
        if(!network.isPeerSetComplete()) {
            BootstrapRequest.getInstance().start();
        }
        String tmaAddress = network.getTmaAddress();
        Wallets wallets = Wallets.getInstance();
        Wallet wallet = wallets.getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        Coin amount = Coin.SATOSHI.multiply(2);
        List<Coin> totals = new ArrayList<Coin>();
        totals.add(amount);
        List<Set<TransactionOutput>> inputList = new GetInputsRequest(network, tmaAddress, totals).getInputlist();
        int i = 0;

        if(inputList.size() != totals.size()) {
            result = ("No inputs available for tma address " + tmaAddress + ". Please check your balance.");
            return false;
        }

        Set<TransactionOutput> inputs = inputList.get(i++);

        Keywords keywords = null;
        Collection<String> names = wallets.getNames(Wallets.TWITTER);
        if(!names.isEmpty()) {
            String accountName = names.iterator().next();
            keywords = new Keywords();
            keywords.getMap().put("from", accountName);
        } else {
            logger.error("Tmitter account is not created yet");
            return false;
        }

        Transaction transaction = new Transaction(wallet.getPublicKey(), twitterTmaAddress, Coin.SATOSHI, Coin.SATOSHI,
                inputs, wallet.getPrivateKey(), tmeet, null, keywords);
        transaction.setApp(Applications.TWITTER);
        new SendTransactionRequest(network, transaction).start();
        logger.debug("sent {}", transaction);

        return true;
    }

    private void processSync() {
        setContentView(R.layout.activity_send_tmeet_complete);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }





    private void load() {
        EditText editTextTmeet = findViewById(R.id.editTextTmeet);
        tmeet = StringUtil.trim(editTextTmeet.getText().toString());
    }

    private boolean validate() {
        if (StringUtil.isEmpty(tmeet)) {
            Toast.makeText(this, getResources().getString(R.string.enter_tmeet), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
