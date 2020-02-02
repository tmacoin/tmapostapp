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
import org.tmacoin.post.android.messaging.persistance.Address;
import org.tmacoin.post.android.messaging.persistance.AddressStore;

public class AddAddressActivity extends BaseActivity {

    private AddressStore addressStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        addressStore = new AddressStore(getApplicationContext());
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
        TextView tma_address = findViewById(R.id.tma_address);
        EditText name = findViewById(R.id.name);
        addressStore.save(new Address(tma_address.getText().toString(), name.getText().toString()));
        Intent intent = new Intent(this, ShowMessageActivity.class);
        intent.putExtra("secureMessage", secureMessage);
        startActivity(intent);
        Toast.makeText(this, getResources().getString(R.string.address_was_added), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if(addressStore != null) {
            addressStore.onDestroy();
        }
        super.onDestroy();
    }

}
