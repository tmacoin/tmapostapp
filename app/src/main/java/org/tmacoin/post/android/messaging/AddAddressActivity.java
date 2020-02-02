package org.tmacoin.post.android.messaging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.peer.thin.SecureMessage;
import org.tma.util.StringUtil;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;

public class AddAddressActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        final SecureMessage secureMessage = (SecureMessage)getIntent().getSerializableExtra("secureMessage");
        TextView tma_address = findViewById(R.id.tma_address);
        tma_address.setText(StringUtil.getStringFromKey(secureMessage.getSender()));

        Button button = findViewById(R.id.add_to_address_book);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(secureMessage);
            }
        });
    }

    private void buttonClicked(SecureMessage secureMessage) {
        Intent intent = new Intent(this, ShowMessageActivity.class);
        intent.putExtra("secureMessage", secureMessage);
        startActivity(intent);
        Toast.makeText(this, getResources().getString(R.string.adress_was_added), Toast.LENGTH_LONG).show();
    }
}
