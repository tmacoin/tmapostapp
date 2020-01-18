package org.tmacoin.post.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Transaction;
import org.tma.blockchain.TransactionData;
import org.tma.blockchain.TransactionOutput;
import org.tma.blockchain.Wallet;
import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.SendTransactionRequest;
import org.tma.peer.thin.GetInputsRequest;
import org.tma.util.Coin;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SendTransactionActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    String recipient;
    String amount;
    String fee;
    String data;
    String expire;
    String expiringData;

    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction);
        Button sendTransactionButton = findViewById(R.id.sendTransactionButton);
        sendTransactionButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        sendTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                load();
                if(!validate()) {
                    showAlert();
                    return;
                }
                setContentView(R.layout.activity_send_transaction_wait);
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
            public void finish() throws Exception {
                processSync();
            }
        }.run();
    }

    private void processAsync() {
        Network network = Network.getInstance();
        final String tmaAddress = network.getTmaAddress();
        final Coin total = Coin.ONE.multiply(Double.parseDouble(amount)).add(new Coin(Long.parseLong(fee)));
        final Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        final TransactionData expiringData = this.expiringData == null? null: new TransactionData(this.expiringData, Long.parseLong(expire));

        if(!network.isPeerSetComplete()) {
            new BootstrapRequest(network).start();
        }
        List<Coin> totals = new ArrayList<>();
        totals.add(total);
        List<Set<TransactionOutput>> inputList = new GetInputsRequest(network, tmaAddress, totals).getInputlist();
        int i = 0;

        if(inputList == null || inputList.size() != totals.size()) {
            result = "No inputs available for tma address " + tmaAddress + ". Please check your balance.";
            return;
        }

        Set<TransactionOutput> inputs = inputList.get(i++);
        logger.debug("number of inputs: {} for {}", inputs.size(), tmaAddress);
        Transaction transaction = new Transaction(wallet.getPublicKey(), recipient, Coin.ONE.multiply(Double.parseDouble(amount)),
                new Coin(Integer.parseInt(fee)), inputs, wallet.getPrivateKey(), data, expiringData, null);
        logger.debug("sent {}", transaction);
        new SendTransactionRequest(Network.getInstance(), transaction).start();
        result = "Successfully sent " + amount + " coins to " + recipient;
    }

    private void processSync() {
        setContentView(R.layout.activity_send_transaction_complete);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
    }

    private void showAlert() {
        Toast.makeText(this, getResources().getString(R.string.entered_info_not_correct), Toast.LENGTH_LONG).show();
    }

    private boolean validate() {
        if(!StringUtil.isTmaAddressValid(recipient)) {
            return false;
        }
        try {
            Double.parseDouble(amount);
            Long.parseLong(fee);
            if(expiringData != null) {
                Long.parseLong(expire);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void load() {
        EditText recipientTmaAddressEditText = findViewById(R.id.recipientTmaAddressEditText);
        recipient = StringUtil.trim(recipientTmaAddressEditText.getText().toString());
        EditText amountInCoinsEditText = findViewById(R.id.amountInCoinsEditText);
        amount = StringUtil.trim(amountInCoinsEditText.getText().toString());
        EditText feeInSatoshisEditText = findViewById(R.id.feeInSatoshisEditText);
        fee = StringUtil.trim(feeInSatoshisEditText.getText().toString());
        EditText expireAfterBlocksEditText = findViewById(R.id.expireAfterBlocksEditText);
        expire = StringUtil.trimToNull(expireAfterBlocksEditText.getText().toString());
        EditText dataEditText = findViewById(R.id.dataEditText);
        data = StringUtil.trimToNull(dataEditText.getText().toString());
        EditText expiringDataEditText = findViewById(R.id.expiringDataEditText);
        expiringData = StringUtil.trimToNull(expiringDataEditText.getText().toString());
    }
}
