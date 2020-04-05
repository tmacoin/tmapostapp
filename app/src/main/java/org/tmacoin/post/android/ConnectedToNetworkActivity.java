package org.tmacoin.post.android;

import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.tma.blockchain.Transaction;
import org.tma.blockchain.TransactionData;
import org.tma.blockchain.TransactionOutput;
import org.tma.blockchain.Wallet;
import org.tma.peer.Network;
import org.tma.peer.SendTransactionRequest;
import org.tma.peer.thin.GetBalanceRequest;
import org.tma.peer.thin.GetFaucetRequest;
import org.tma.peer.thin.GetInputsRequest;
import org.tma.peer.thin.NewMessageEvent;
import org.tma.peer.thin.ResponseHolder;
import org.tma.util.Coin;
import org.tma.util.Constants;
import org.tma.util.Listeners;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;
import org.tmacoin.post.android.messaging.NewMessageEventListener;
import org.tmacoin.post.android.messaging.NewMessageNotifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConnectedToNetworkActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final Listeners listeners = Listeners.getInstance();
    private static final String faucetAddress = "5KCJSsDpXkLiKv5juGoHuoaDbnvr2FASerm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_to_network);
        final TextView address = findViewById(R.id.address);
        address.setText(Network.getInstance().getTmaAddress());
        listeners.addEventListener(NewMessageEvent.class, new NewMessageEventListener(getApplicationContext()));
        startNotifier();
        tryFaucet();
    }

    private void tryFaucet() {
        ThreadExecutor.getInstance().execute(new TmaRunnable("Faucet") {
            public void doRun() {
                Network network = Network.getInstance();
                TmaAndroidUtil.checkNetwork();
                GetBalanceRequest request = new GetBalanceRequest(network, network.getTmaAddress());
                request.start();
                String balance = (String) ResponseHolder.getInstance().getObject(request.getCorrelationId());
                if(balance == null) {
                    return;
                }
                BigDecimal amount = new BigDecimal(balance);
                if(!BigDecimal.ZERO.equals(amount)) {
                    return;
                }
                GetFaucetRequest getFaucetRequest = new GetFaucetRequest(network, network.getTmaAddress(), faucetAddress);
                getFaucetRequest.start();
                ThreadExecutor.sleep(Constants.TIMEOUT);
                while(!sendTransaction()) {

                }
            }
        });
    }

    private boolean sendTransaction() {
        Network network = Network.getInstance();
        final String tmaAddress = network.getTmaAddress();
        final Coin total = Coin.SATOSHI.multiply(2);
        final Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        TmaAndroidUtil.checkNetwork();
        List<Coin> totals = new ArrayList<>();
        totals.add(total);
        List<Set<TransactionOutput>> inputList = new GetInputsRequest(network, tmaAddress, totals).getInputlist();
        int i = 0;

        if(inputList == null || inputList.size() != totals.size()) {
            return false;
        }

        Set<TransactionOutput> inputs = inputList.get(i++);
        logger.debug("number of inputs: {} for {}", inputs.size(), tmaAddress);
        Transaction transaction = new Transaction(wallet.getPublicKey(), faucetAddress, Coin.SATOSHI,
                Coin.SATOSHI, inputs, wallet.getPrivateKey(), null, null, null);
        logger.debug("sent {}", transaction);
        new SendTransactionRequest(Network.getInstance(), transaction).start();
        return true;
    }

    private void startNotifier() {
        Intent service = new Intent(this, NewMessageNotifier.class);
        service.setAction(TmaAndroidUtil.START);
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        service.putExtra("wallet", wallet);
        ContextCompat.startForegroundService(this, service);
    }

}
