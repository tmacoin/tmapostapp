package org.tmacoin.post.android.messaging;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.peer.thin.SecureMessage;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.messaging.persistance.Address;
import org.tmacoin.post.android.messaging.persistance.AddressStore;

public class AddAddressActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private AddressStore addressStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        addressStore = new AddressStore(getApplicationContext());
        final SecureMessage secureMessage = (SecureMessage)getIntent().getSerializableExtra("secureMessage");
        TextView tma_address = findViewById(R.id.tma_address);

        String tmaAddress = StringUtil.getStringFromKey(secureMessage.getSender());
        tma_address.setText(tmaAddress);

        Button button = findViewById(R.id.add_to_address_book);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(secureMessage);
            }
        });

        EditText name = findViewById(R.id.name);
        String str = addressStore.findNameByTmaAddress(tmaAddress);
        if(str != null) {
            name.setText(str);
        }

        Button delete_address_button = findViewById(R.id.delete_address_button);
        delete_address_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButtonClicked(secureMessage);
            }
        });
    }

    private void deleteButtonClicked(SecureMessage secureMessage) {
        TextView tma_address = findViewById(R.id.tma_address);

        addressStore.deleteByTmaAddress(tma_address.getText().toString());

        Intent intent = new Intent(this, ShowMessageActivity.class);
        intent.putExtra("secureMessage", secureMessage);
        startActivity(intent);
        Toast.makeText(this, getResources().getString(R.string.address_was_deleted), Toast.LENGTH_LONG).show();
    }

    private void buttonClicked(SecureMessage secureMessage) {
        TextView tma_address = findViewById(R.id.tma_address);
        EditText name = findViewById(R.id.name);
        try {
            addressStore.save(new Address(tma_address.getText().toString(), name.getText().toString()));
        } catch (SQLiteConstraintException e) {
            Toast.makeText(this, getResources().getString(R.string.name_already_exists), Toast.LENGTH_LONG).show();
            return;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
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
