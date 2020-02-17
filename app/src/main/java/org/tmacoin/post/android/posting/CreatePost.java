package org.tmacoin.post.android.posting;

import android.app.Activity;
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
import org.tma.peer.thin.GetBalanceRequest;
import org.tma.peer.thin.GetInputsRequest;
import org.tma.util.Applications;
import org.tma.util.Coin;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.PasswordUtil;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CreatePost extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final int POWER = 15;
    private static boolean active = false;
    private static Activity activity;

    private String account;
    private String description;
    private String passphrase;
    String result = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        Wallets wallets = Wallets.getInstance();
        Collection<String> names = wallets.getNames(Wallets.WALLET_NAME);

        if(active) {
            setContentView(R.layout.activity_create_post_wait);
            return;
        }
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
        
    }

    private boolean generateKeyPair() throws Exception {

        Network network = Network.getInstance();

        GetBalanceRequest request = new GetBalanceRequest(network, network.getTmaAddress());
        String balance = request.start();
        if("0".equals(balance)) {
            result = ("Your balance is zero. You cannot create post account.");
            return false;
        }

        PasswordUtil passwordUtil = new PasswordUtil();

        if (!passwordUtil.loadKeys(passphrase)) {
            return false;
        }

        Wallet wallet = new Wallet();
        int shardId = StringUtil.getShardForNonTmaAddress(account, POWER);
        logger.debug("shardId: {} for power {}", shardId, POWER);
        long i = 0;
        while (true) {
            i++;
            if(i % 10000 == 0) {
                logger.debug("i={}", i);
            }
            wallet.generateKeyPair();
            if (StringUtil.getShard(wallet.getTmaAddress(), POWER) == shardId) {
                break;
            }
        }
        shardId = StringUtil.getShard(wallet.getTmaAddress(), network.getBootstrapShardingPower());
        logger.debug("generated address {} for shard {} for power {}", wallet.getTmaAddress(), shardId, network.getBootstrapShardingPower());
        Wallets wallets = Wallets.getInstance();
        wallets.putWallet(Wallets.WALLET_NAME, account, wallet);
        passwordUtil.saveKeys(passphrase);
        return sendCreateTwitterTransaction(wallet.getTmaAddress());
    }

    private boolean sendCreateTwitterTransaction(String twitterTmaAddress) {
        Network network = Network.getInstance();
        if(!network.isPeerSetComplete()) {
            BootstrapRequest.getInstance().start();
        }
        String tmaAddress = network.getTmaAddress();
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        Coin amount = Coin.SATOSHI.multiply(2);
        List<Coin> totals = new ArrayList<Coin>();
        totals.add(amount);
        List<Set<TransactionOutput>> inputList = new GetInputsRequest(network, tmaAddress, totals).getInputlist();

        if(inputList.size() != totals.size()) {
            result = ("No inputs available for tma address " + tmaAddress + ". Please check your balance.");
            return false;
        }

        int i = 0;
        Set<TransactionOutput> inputs = inputList.get(i++);

        Keywords keywords = new Keywords();
        keywords.getMap().put("create", account);

        Transaction transaction = new Transaction(wallet.getPublicKey(), twitterTmaAddress, Coin.SATOSHI, Coin.SATOSHI,
                inputs, wallet.getPrivateKey(), description, null, keywords);
        transaction.setApp(Applications.POST);
        new SendTransactionRequest(network, transaction).start();
        logger.debug("sent {}", transaction);
        return true;
    }

    private void processSync() {
        activity.setContentView(R.layout.activity_create_post_complete);
        TextView resultTextView = activity.findViewById(R.id.resultTextView);
        resultTextView.setText(result);
        Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }

    private boolean validate() {
        /* if (StringUtil.isEmpty(account)) {
            Toast.makeText(this, getResources().getString(R.string.account_cannot_be_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        if (StringUtil.isEmpty(description)) {
            Toast.makeText(this, getResources().getString(R.string.description_cannot_be_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        PasswordUtil passwordUtil = new PasswordUtil();
        try {
            if (!passwordUtil.loadKeys(passphrase)) {
                Toast.makeText(this, getResources().getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }*/
        return true;
    }

    private void load() {
        EditText accountEditText = findViewById(R.id.accountEditText);


    }
}
