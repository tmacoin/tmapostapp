package org.tmacoin.post.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.tma.util.TmaLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class ExportFilesConfig extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final int REQUEST_CODE = 1;
    private ListView listView;
    private File selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_files);

        File directory = getApplicationContext().getFilesDir().getAbsoluteFile();
        if (directory.listFiles().length > 0) {
            final File[] fList = (directory.listFiles())[0].listFiles();

            listView = findViewById(R.id.simpleListView);
            FileAdapter arrayAdapter = new FileAdapter(this, fList);
            listView.setAdapter(arrayAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (fList.length > 0) {
                        selectedFile = fList[position];
                        try {
                            createFile();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        logger.error("No files in config directory");
                    }
                }
            });
        } else {
            logger.error("Possible no config directory");
        }

    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, selectedFile.getName());
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        startActivityForResult(intent, REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                copyFile(selectedFile, uri);
            }
        }
    }

    private void copyFile(File selectedFile, Uri uri) {
        InputStream is = null;
        OutputStream os = null;
        ParcelFileDescriptor pfd = null;
        try {
            pfd = getContentResolver().openFileDescriptor(uri, "w");
            os = new FileOutputStream(pfd.getFileDescriptor());
            is = new FileInputStream(selectedFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if(is != null) {
                    is.close();
                }
                if(os != null) {
                    os.close();
                }
                if(pfd != null) {
                    pfd.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


}
