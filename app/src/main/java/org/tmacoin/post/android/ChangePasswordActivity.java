package org.tmacoin.post.android;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.util.TmaLogger;

public class ChangePasswordActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        final EditText oldPassword = findViewById(R.id.oldPassword);
        oldPassword.setTransformationMethod(new MyPasswordTransformationMethod());
        final EditText newPassword = findViewById(R.id.newPassword);
        newPassword.setTransformationMethod(new MyPasswordTransformationMethod());
        final EditText reenterNew = findViewById(R.id.reenterNew);
        reenterNew.setTransformationMethod(new MyPasswordTransformationMethod());

        reenterNew.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideKeyboard(v);
                    buttonClicked();
                }
                return false;
            }
        });

        Button changePasswordButton = findViewById(R.id.changePasswordButton);

        changePasswordButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                buttonClicked();
            }
        });

    }

    private void buttonClicked() {

        final EditText oldPassword = findViewById(R.id.oldPassword);
        final EditText newPassword = findViewById(R.id.newPassword);
        final EditText reenterNew = findViewById(R.id.reenterNew);
        if(!newPassword.getText().toString().equals(reenterNew.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.reentered_password_does_not_match), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            PasswordUtil passwordUtil = new PasswordUtil();
            if (!passwordUtil.loadKeys(oldPassword.getText().toString())) {
                Toast.makeText(this, getResources().getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                return;
            }
            passwordUtil.saveKeys(newPassword.getText().toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        setContentView(R.layout.activity_password_changed);
    }
}
