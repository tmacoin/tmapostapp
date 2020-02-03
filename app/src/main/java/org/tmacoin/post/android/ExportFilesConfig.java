package org.tmacoin.post.android;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.tma.util.TmaLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class ExportFilesConfig extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final int REQUEST_CODE = 1;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_files);

        //Intent intent = new Intent();
        //intent.setAction(getFilesDir().getAbsolutePath() + "/config/");
        //intent.setType("text/plain");

        File directory = getApplicationContext().getFilesDir().getAbsoluteFile();
        final File[] fList = (directory.listFiles())[0].listFiles();
        for (File file: fList) {
            logger.info("FILE:",file);
        }

        listView = findViewById(R.id.simpleListView);
        FileAdapter arrayAdapter = new FileAdapter(this, fList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                File file2 = fList[position];
                try {
                    Toast.makeText(ExportFilesConfig.this, "You have clicked on " + file2.getCanonicalPath(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });



       // startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setContentView(R.layout.activity_file_processed);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData(); //The uri with the location of the file

            //dir with selected file
            File sourceFile = new File(getFileName(selectedFile));

            //download dir
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() );
            //FileUtils.copy(); ????
           //copyFile(selectedFile, dir.getAbsolutePath());
            return;
        }
       // TextView textViewFile = findViewById(R.id.textViewFile);
       // textViewFile.setText("Copying file was canceled");
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

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
