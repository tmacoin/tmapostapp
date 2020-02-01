package org.tmacoin.post.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tma.util.TmaLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class ExportFilesConfig extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_RUNTIME_PERMISSION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
        //startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), REQUEST_CODE);

        //Uri selectedUri = Uri.parse(getFilesDir().getAbsolutePath() + "/config/");
        //Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setDataAndType(selectedUri, "resource/folder");


        //Intent intent = new Intent(activity, FileChooser::class.java)
        //intent.putExtra(Constants.INITIAL_DIRECTORY, File(storageDirPath).absolutePath)
        //startActivityForResult(intent, CODE_INTENT );


        Intent intent = new Intent();
        intent.setAction(getFilesDir().getAbsolutePath() + "/config/");
        intent.setType("text/plain");
        //intent.putExtra("CONTENT_TYPE", "text/plain");
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        String[] t = getApplicationContext().fileList();
        File directory = getApplicationContext().getFilesDir();

        if (CheckPermission(ExportFilesConfig.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // you have permission go ahead
            //createApplicationFolder();
            String g = "good";
        } else {
            // you do not have permission go request runtime permissions
            RequestPermission(ExportFilesConfig.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_RUNTIME_PERMISSION);
        }



        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), REQUEST_CODE);
        //startActivity(intent);
    }

    public void RequestPermission(Activity thisActivity, String Permission, int Code) {
        if (ContextCompat.checkSelfPermission(thisActivity,
                Permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Permission)) {
            } else {
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Permission},
                        Code);
            }
        }
    }

    public boolean CheckPermission(Context context, String Permission) {
        if (ContextCompat.checkSelfPermission(context,
                Permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
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
