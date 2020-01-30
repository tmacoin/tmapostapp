package org.tmacoin.post.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.tma.util.Constants;
import org.tma.util.TmaLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class GetFilesConfig extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_file_config);
        Button locateButton = findViewById(R.id.get_peers_button);
        //showSelectKeyFileDialog();
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectKeyFile();
            }
        });
    }

    private void selectKeyFile() {
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), REQUEST_CODE);
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
                logger.debug(getString(R.string.parent_directory_created) + ": {}", parentDirectory.getAbsolutePath());
            }

            copyFile(selectedFile, keyFile.getAbsolutePath());
        }
        setContentView(R.layout.activity_file_processed);
        Toast.makeText(getApplicationContext(), getString(R.string.file_copied_to) + " " + getFilesDir().getAbsolutePath(), Toast.LENGTH_LONG).show();
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
