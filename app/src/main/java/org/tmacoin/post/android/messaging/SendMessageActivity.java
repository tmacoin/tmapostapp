package org.tmacoin.post.android.messaging;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import org.tma.peer.thin.GetPublicKeyRequest;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SecureMessage;
import org.tma.util.Applications;
import org.tma.util.Base58;
import org.tma.util.Coin;
import org.tma.util.Encryptor;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SendMessageActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final Encryptor encryptor = new Encryptor();

    private String recipientTmaAddress;
    private String fee;
    private String expire;
    private String subject;
    private String expiringData;
    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        SecureMessage secureMessage = (SecureMessage)getIntent().getSerializableExtra("secureMessage");
        if(secureMessage != null) {
            EditText recipientTmaAddressEditText = findViewById(R.id.recipientTmaAddressEditText);
            recipientTmaAddressEditText.setText(StringUtil.getStringFromKey(secureMessage.getSender()));
            EditText subjectEditText = findViewById(R.id.subjectEditText);
            Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
            subjectEditText.setText("Re: " + secureMessage.getSubject(wallet.getPrivateKey()));
        }
        updateStatus("Network status: " + Network.getInstance().getPeerCount().toString());
        Button sendMessageButton = findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    hideKeyboard(v);
                    load();
                    if (!validate()) {
                        showAlert();
                        return;
                    }
                    setContentView(R.layout.activity_send_message_wait);
                    process();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });

        Spinner expireAfterBlocksSpinner = (Spinner) findViewById(R.id.expireAfterBlocksSpinner);
        expireAfterBlocksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String label = parent.getItemAtPosition(position).toString();
                switch(label) {
                    case "10 minutes":
                        expire = "10";
                        break;
                    case "1 hour":
                        expire = "60";
                        break;
                    case "24 hours":
                        expire = "1440";
                        break;
                    case "1 week":
                        expire = "10080";
                        break;
                    case "1 month":
                        expire = "43200";
                        break;
                    case "1 year":
                        expire = "525600";
                        break;

                    default:
                        expire = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                expire = null;
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
        final Coin total = Coin.SATOSHI.add(new Coin(Long.parseLong(fee)));
        final Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        updateStatus("Network status: " + network.getPeerCount().toString());
        if(!network.isPeerSetComplete()) {
            new BootstrapRequest(network).start();
        }
        updateStatus("Network status: " + network.getPeerCount().toString());
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

        GetPublicKeyRequest getPublicKeyRequest = new GetPublicKeyRequest(network, recipientTmaAddress);
        getPublicKeyRequest.start();
        PublicKey recipient = (PublicKey) ResponseHolder.getInstance().getObject(getPublicKeyRequest.getCorrelationId());

        if(recipient == null) {
            logger.debug("Recipient public key is not found for tma address {}", recipientTmaAddress);
            result = "Recipient public key is not found for tma address " + recipientTmaAddress;
            return;
        }
        logger.debug("expire={}, subjectTextView={}, expiringData={}", expire, subject, expiringData);

        TransactionData transactionData = null;
        if(subject != null) {
            try {
                String str = subject + "\n" + expiringData;
                byte[] encrypted = encryptor.encryptAsymm(str.getBytes(StandardCharsets.UTF_8), recipient);
                str = Base58.encode(encrypted);
                transactionData = new TransactionData(str, Long.parseLong(expire));
                if(!str.equals(transactionData.getData())) {
                    logger.debug("Encrypted string length is {}", str.length());
                    result = "Message is too long, cannot send.";
                    return;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result  = e.getMessage();
                return;
            }
        }

        Transaction transaction = new Transaction(wallet.getPublicKey(), StringUtil.getStringFromKey(recipient), Coin.SATOSHI,
                new Coin(Integer.parseInt(fee)), inputs, wallet.getPrivateKey(), null, transactionData, null);
        transaction.setApp(Applications.MESSAGING);
        logger.debug("sent {}", transaction);

        new SendTransactionRequest(network, transaction).start();
        updateStatus("Network status: " + network.getPeerCount().toString());
        result = "Successfully sent message \"" + subject + "\" to " + recipientTmaAddress;
    }

    private void processSync() {
        setContentView(R.layout.activity_send_message_complete);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
        updateStatus("Network status: " + Network.getInstance().getPeerCount().toString());
    }

    private void showAlert() {
        Toast.makeText(this, getResources().getString(R.string.entered_info_not_correct), Toast.LENGTH_LONG).show();
    }

    private boolean validate() {
        if(!StringUtil.isTmaAddressValid(recipientTmaAddress)) {
            return false;
        }
        try {
            Long.parseLong(fee);
            if(subject != null) {
                Long.parseLong(expire);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void load() {
        EditText recipientTmaAddressEditText = findViewById(R.id.recipientTmaAddressEditText);
        recipientTmaAddress = StringUtil.trim(recipientTmaAddressEditText.getText().toString());
        EditText feeInSatoshisEditText = findViewById(R.id.feeInSatoshisEditText);
        fee = StringUtil.trim(feeInSatoshisEditText.getText().toString());
        EditText subjectEditText = findViewById(R.id.subjectEditText);
        subject = StringUtil.trimToNull(subjectEditText.getText().toString());
        EditText expiringDataEditText = findViewById(R.id.expiringDataEditText);
        expiringData = StringUtil.trimToNull(expiringDataEditText.getText().toString());
        logger.debug("expiringData={}, expiringDataEditText.getLineCount()={}", expiringData, expiringDataEditText.getLineCount());
    }
}
