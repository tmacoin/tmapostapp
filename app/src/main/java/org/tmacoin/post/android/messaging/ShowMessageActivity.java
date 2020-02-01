package org.tmacoin.post.android.messaging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.tma.blockchain.Wallet;
import org.tma.peer.Network;
import org.tma.peer.thin.SecureMessage;
import org.tma.util.Encryptor;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.util.Date;

public class ShowMessageActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final Encryptor encryptor = new Encryptor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecureMessage secureMessage = (SecureMessage) getIntent().getSerializableExtra("secureMessage");
        setContentView(R.layout.activity_show_message);
        showMessage(secureMessage);
        updateStatus("Network status: " + Network.getInstance().getPeerCount().toString());
    }

    private void showMessage(final SecureMessage secureMessage) {
        String date = new Date(secureMessage.getTimeStamp()).toString();
        String expire = new Date(secureMessage.getTimeStamp() + secureMessage.getExpire() * 60000).toString();
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        String subject = secureMessage.getSubject(wallet.getPrivateKey());
        logger.debug("date={}, expire={}, subject={}", date, expire, subject);
        TextView sender = findViewById(R.id.sender);
        sender.setText(StringUtil.getStringFromKey(secureMessage.getSender()));
        TextView recipient = findViewById(R.id.recipient);
        recipient.setText(secureMessage.getRecipient());
        TextView value = findViewById(R.id.value);
        value.setText(secureMessage.getValue().toNumberOfCoins());
        TextView fee = findViewById(R.id.fee);
        fee.setText(secureMessage.getFee().toNumberOfCoins());

        TextView dateTextView = findViewById(R.id.singleDateTextView);
        dateTextView.setText(date);

        TextView expireTextView = findViewById(R.id.expire);
        expireTextView.setText(expire);

        TextView subjectTextView = findViewById(R.id.singleSubjectTextView);
        subjectTextView.setText(subject);

        TextView body = findViewById(R.id.body);
        String bodyText = secureMessage.getBody(wallet.getPrivateKey());
        bodyText = bodyText == null? "": bodyText;
        body.setText(bodyText);

        Button reply = findViewById(R.id.reply);
        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(secureMessage);
            }
        });
    }

    private void buttonClicked(SecureMessage secureMessage) {
        Intent intent = new Intent(this, SendMessageActivity.class);
        intent.putExtra("secureMessage", secureMessage);
        startActivity(intent);
    }
}
