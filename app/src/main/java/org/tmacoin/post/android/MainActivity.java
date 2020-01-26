package org.tmacoin.post.android;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.peer.Network;
import org.tma.util.Constants;
import org.tma.util.TmaLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Network.getInstance() != null) {
            connectedToTmaNetwork();
            return;
        }

        Constants.FILES_DIRECTORY = getFilesDir().getAbsolutePath() + "/";
        File keyFile = new File(Constants.FILES_DIRECTORY + Constants.KEYS);
        if(keyFile.exists()) {
            complete();
            return;
        }

        showSelectKeyFileDialog();

    }

    private void showSelectKeyFileDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Key File")
                .setMessage("Do you want to use an existing key file?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectKeyFile();
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        createNewKeyFile();
                    }}).show();
    }

    private boolean startNetwork(String passphrase) {

        try {
            Wallets.WALLET_NAME = "0";
            PasswordUtil passwordUtil = new PasswordUtil();
            if (!passwordUtil.loadKeys(passphrase)) {
                logger.error("Could not load keys");
                File keyFile = new File(Constants.FILES_DIRECTORY + Constants.KEYS);
                if(keyFile.exists()) {
                    Toast.makeText(this, getResources().getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                }
                return false;
            }
            Toast.makeText(this, getResources().getString(R.string.connecting_wait), Toast.LENGTH_LONG).show();
            logger.debug("TMA POST starting up");
            StartNetwork.getInstance().start(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void createNewKeyFile() {
        setContentView(R.layout.activity_main_confirm);
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        Button submitButton = findViewById(R.id.button);
        final EditText password = findViewById(R.id.password);
        password.setTransformationMethod(new MyPasswordTransformationMethod());
        final EditText confirmPassword = findViewById(R.id.confirmPassword);
        confirmPassword.setTransformationMethod(new MyPasswordTransformationMethod());

        confirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if(!password.getText().toString().equals(confirmPassword.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "Password and Confirm Password do no match. Please try again.", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    Wallets.WALLET_NAME = "0";
                    PasswordUtil passwordUtil = new PasswordUtil();
                    passwordUtil.generateKey(Wallets.TMA, Wallets.WALLET_NAME, password.getText().toString());
                    doStartNetwork(v, password.getText().toString(), pgsBar);
                }
                return false;
            }
        });

        submitButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!password.getText().toString().equals(confirmPassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Password and Confirm Password do no match. Please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
                Wallets.WALLET_NAME = "0";
                PasswordUtil passwordUtil = new PasswordUtil();
                passwordUtil.generateKey(Wallets.TMA, Wallets.WALLET_NAME, password.getText().toString());
                doStartNetwork(v, password.getText().toString(), pgsBar);
            }
        });
    }

    private boolean doStartNetwork(View v, String password, ProgressBar pgsBar) {
        hideKeyboard(v);
        boolean result = startNetwork(password);
        if(result) {
            pgsBar.setVisibility(View.VISIBLE);
        }
        return result;
    }

    private void selectKeyFile() {
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select keys.csv"), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData(); //The uri with the location of the file
            File keyFile = new File(Constants.FILES_DIRECTORY + Constants.KEYS);
            File parentDirectory = keyFile.getParentFile();
            boolean parentDirectoryCreated = false;
            if(parentDirectory != null) {
                parentDirectoryCreated = parentDirectory.mkdirs();
            }
            if(parentDirectoryCreated) {
                logger.debug("Parent directory created: {}", parentDirectory.getAbsolutePath());
            }

            copyFile(selectedFile, keyFile.getAbsolutePath());
        }
        complete();
        Toast.makeText(getApplicationContext(), "Key File was copied to " + getFilesDir().getAbsolutePath(), Toast.LENGTH_LONG).show();
    }


    private void complete() {
        setContentView(R.layout.activity_main);
        final ProgressBar pgsBar = findViewById(R.id.progressBar);
        Button submitButton = findViewById(R.id.button);
        final EditText password = findViewById(R.id.password);
        password.setTransformationMethod(new MyPasswordTransformationMethod());

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if(!doStartNetwork(v, password.getText().toString(), pgsBar)) {
                        File keyFile = new File(Constants.FILES_DIRECTORY + Constants.KEYS);
                        if(!keyFile.exists()) {
                            showPreviousKeyFileDialog();
                        }
                    }

                }
                return false;
            }
        });

        submitButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!doStartNetwork(v, password.getText().toString(), pgsBar)) {
                    File keyFile = new File(Constants.FILES_DIRECTORY + Constants.KEYS);
                    if(!keyFile.exists()) {
                        showPreviousKeyFileDialog();
                    }
                }
            }
        });
    }

    private void showPreviousKeyFileDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Key File")
                .setMessage("Previously selected key file is invalid. Do you want to use another valid key file?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectKeyFile();
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        createNewKeyFile();
                    }}).show();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void connectedToTmaNetwork() {
        Intent intent = new Intent(this, ConnectedToNetworkActivity.class);
        startActivity(intent);
        finish();
    }

    private void copyFile(Uri selectedFile, String destination) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = getContentResolver().openInputStream(selectedFile);
            if(is == null) {
                return;
            }
            os = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if(is != null) {
                    is.close();
                }
                if(os != null) {
                    os.close();
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }
}
