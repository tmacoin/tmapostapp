package org.tmacoin.post.android;

import android.os.Bundle;
import android.widget.TextView;

import org.tma.blockchain.Transaction;

import java.util.Date;

public class ShowTransactionActivity extends BaseActivity {

    private Transaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_transaction);
        transaction = (Transaction) getIntent().getSerializableExtra("transaction");
        process();
    }

    private void process() {
        TextView sender = findViewById(R.id.sender);
        sender.setText(transaction.getSenderAddress());
        TextView recipient = findViewById(R.id.recipient);
        recipient.setText(transaction.getRecipient());
        TextView value = findViewById(R.id.value);
        value.setText(transaction.getValue().toNumberOfCoins() + " coins");
        TextView date = findViewById(R.id.date);
        date.setText(new Date(transaction.getTimeStamp()).toString());

    }
}
