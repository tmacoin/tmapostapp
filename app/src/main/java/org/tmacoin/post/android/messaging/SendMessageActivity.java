package org.tmacoin.post.android.messaging;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Transaction;
import org.tma.blockchain.TransactionData;
import org.tma.blockchain.TransactionOutput;
import org.tma.blockchain.Wallet;
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
import org.tmacoin.post.android.TmaAndroidUtil;
import org.tmacoin.post.android.Wallets;
import org.tmacoin.post.android.persistance.AddressStore;

import java.nio.charset.StandardCharsets;
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
    private AddressStore addressStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        addressStore = new AddressStore(getApplicationContext());

        AutoCompleteTextView recipientTmaAddressEditText = findViewById(R.id.recipientTmaAddressEditText);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, addressStore.getAllNames());
        recipientTmaAddressEditText.setAdapter(adapter);
        recipientTmaAddressEditText.setThreshold(1);

        SecureMessage secureMessage = (SecureMessage)getIntent().getSerializableExtra("secureMessage");
        if(secureMessage != null) {
            String recipientName = addressStore.findNameByTmaAddress(secureMessage.getSenderTmaAddress());
            if(recipientName == null) {
                recipientName = secureMessage.getSenderTmaAddress();
            }

            recipientTmaAddressEditText.setText(recipientName);
            EditText subjectEditText = findViewById(R.id.subjectEditText);
            Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
            subjectEditText.setText(getResources().getString(R.string.re) + " " + secureMessage.getSubject(wallet.getPrivateKey()));
        }
        String subject = (String)getIntent().getSerializableExtra("subject");
        if(subject != null) {
            String recipient = (String)getIntent().getSerializableExtra("recipient");
            String recipientName = addressStore.findNameByTmaAddress(recipient);
            if(recipientName == null) {
                recipientName = recipient;
            }

            recipientTmaAddressEditText.setText(recipientName);
            EditText subjectEditText = findViewById(R.id.subjectEditText);
            Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
            subjectEditText.setText(getResources().getString(R.string.re) + " " + subject);
        }



        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
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

        Spinner expireAfterBlocksSpinner = findViewById(R.id.expireAfterBlocksSpinner);
        expireAfterBlocksSpinner.setSelection(5);
        expireAfterBlocksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        expire = "10";
                        break;
                    case 1:
                        expire = "60";
                        break;
                    case 2:
                        expire = "1440";
                        break;
                    case 3:
                        expire = "10080";
                        break;
                    case 4:
                        expire = "43200";
                        break;
                    case 5:
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

        final EditText expiringDataEditText = findViewById(R.id.expiringDataEditText);
        final ScrollView scrollView = findViewById(R.id.scrollView);
        final TextView statusBar = findViewById(R.id.statusBar);
        expiringDataEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    scrollView.smoothScrollTo(0,statusBar.getBottom());
                }
            }
        });

        TmaAndroidUtil.enableScroll(expiringDataEditText);

    }

    private void process() {
        new AndroidExecutor() {

            @Override
            public void start() {
                processAsync();
            }

            @Override
            public void finish() {
                processSync();
            }
        }.run();
    }

    private void processAsync() {
        Network network = Network.getInstance();
        final String tmaAddress = network.getTmaAddress();
        final Coin total = Coin.SATOSHI.add(new Coin(Long.parseLong(fee)));
        final Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        updateStatus(getResources().getString(R.string.network_status) + ": " + network.getPeerCount().toString());
        TmaAndroidUtil.checkNetwork();
        updateStatus(getResources().getString(R.string.network_status) + ": " + network.getPeerCount().toString());
        List<Coin> totals = new ArrayList<>();
        totals.add(total);
        List<Set<TransactionOutput>> inputList = new GetInputsRequest(network, tmaAddress, totals).getInputlist();

        if(inputList == null || inputList.size() != totals.size()) {
            result = "No inputs available for tma address " + tmaAddress + ". Please check your balance.";
            return;
        }

        Set<TransactionOutput> inputs = inputList.get(0);
        logger.debug("number of inputs: {} for {}", inputs.size(), tmaAddress);

        GetPublicKeyRequest getPublicKeyRequest = new GetPublicKeyRequest(network, recipientTmaAddress);
        getPublicKeyRequest.start();
        PublicKey recipient = (PublicKey) ResponseHolder.getInstance().getObject(getPublicKeyRequest.getCorrelationId());

        if(recipient == null) {
            logger.debug("Recipient public key is not found for tma address {}", recipientTmaAddress);
            result = "Recipient public key is not found for tma address " + recipientTmaAddress;
            return;
        }

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
        updateStatus(getResources().getString(R.string.network_status) + ": " + network.getPeerCount().toString());

        String recipientName = addressStore.findNameByTmaAddress(recipientTmaAddress);
        if(recipientName == null) {
            recipientName = recipientTmaAddress;
        }

        result = getResources().getString(R.string.successfully_sent_message) + " \"" + subject + "\" " +
                getResources().getString(R.string.to) + " " + recipientName;
    }

    private void processSync() {
        setContentView(R.layout.activity_send_message_complete);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
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

        String str = addressStore.findTmaAddressByName(recipientTmaAddress);
        if(str != null) {
            recipientTmaAddress = str;
        }

        EditText feeInSatoshisEditText = findViewById(R.id.feeInSatoshisEditText);
        fee = StringUtil.trim(feeInSatoshisEditText.getText().toString());
        EditText subjectEditText = findViewById(R.id.subjectEditText);
        subject = StringUtil.trim(subjectEditText.getText().toString());
        EditText expiringDataEditText = findViewById(R.id.expiringDataEditText);
        expiringData = StringUtil.trim(expiringDataEditText.getText().toString());
    }

    @Override
    protected void onDestroy() {
        if(addressStore != null) {
            addressStore.onDestroy();
        }
        super.onDestroy();
    }
}
