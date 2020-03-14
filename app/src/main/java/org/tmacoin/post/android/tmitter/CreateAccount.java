package org.tmacoin.post.android.tmitter;

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
import org.tmacoin.post.android.TmaAndroidUtil;
import org.tmacoin.post.android.Wallets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CreateAccount extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final int POWER = 15;
    private static boolean active = false;
    private static Activity activity;

    private String account;
    private String description;
    private String passphrase;
    private String result = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        Wallets wallets = Wallets.getInstance();
        Collection<String> names = wallets.getNames(Wallets.TWITTER);
        if (!names.isEmpty()) {
            String accountName = names.iterator().next();
            String str = getResources().getString(R.string.tmitter_account_created_account_name_is) + " " + accountName
                + " " + getResources().getString(R.string.with_tma_address) + " " +
                wallets.getWallet(Wallets.TWITTER, accountName).getTmaAddress();
            setContentView(R.layout.activity_create_tmitter_complete);
            TextView resultTextView = findViewById(R.id.resultTextView);
            resultTextView.setText(str);
            return;
        }
        if(active) {
            setContentView(R.layout.activity_create_tmitter_wait);
            return;
        }
        setContentView(R.layout.activity_create_tmitter);
        Button createTmitterAccount = findViewById(R.id.createTmitterAccount);
        createTmitterAccount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        createTmitterAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                load();
                if(!validate()) {
                    return;
                }
                Toast.makeText(CreateAccount.this, getResources().getString(R.string.tmitter_wait), Toast.LENGTH_LONG).show();
                setContentView(R.layout.activity_create_tmitter_wait);
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
        active = true;
        try {
            if (generateKeyPair()) {
                result = (getResources().getString(R.string.tmitter_account_created) + ": \"" + account + "\"");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

        } finally {
            active = false;
        }
    }

    private boolean generateKeyPair() throws Exception {

        Network network = Network.getInstance();

        GetBalanceRequest request = new GetBalanceRequest(network, network.getTmaAddress());
        String balance = request.start();
        if("0".equals(balance)) {
            result = ("Your balance is zero. You cannot create Tmitter account.");
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
        wallets.putWallet(Wallets.TWITTER, account, wallet);
        passwordUtil.saveKeys(passphrase);
        return sendCreateTwitterTransaction(wallet.getTmaAddress());
    }

    private boolean sendCreateTwitterTransaction(String twitterTmaAddress) {
        Network network = Network.getInstance();
        TmaAndroidUtil.checkNetwork();
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
        transaction.setApp(Applications.TWITTER);
        new SendTransactionRequest(network, transaction).start();
        logger.debug("sent {}", transaction);
        return true;
    }

    private void processSync() {
        activity.setContentView(R.layout.activity_create_tmitter_complete);
        TextView resultTextView = activity.findViewById(R.id.resultTextView);
        resultTextView.setText(result);
        Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }

    private boolean validate() {
        if (StringUtil.isEmpty(account)) {
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
        }
        return true;
    }

    private void load() {
        EditText accountEditText = findViewById(R.id.accountEditText);
        account = StringUtil.trim(accountEditText.getText().toString());
        EditText descriptionEditText = findViewById(R.id.descriptionEditText);
        description = StringUtil.trim(descriptionEditText.getText().toString());
        EditText passphraseEditText = findViewById(R.id.passphraseEditText);
        passphrase = StringUtil.trim(passphraseEditText.getText().toString());

    }
}
