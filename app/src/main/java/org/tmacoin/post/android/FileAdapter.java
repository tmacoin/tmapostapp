package org.tmacoin.post.android;

import android.app.Activity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.tma.util.TmaLogger;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileAdapter extends ArrayAdapter<File> {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private final Activity context;
    private final File[] list;
    private final String DATE_MM_DD_YYYY_TIME = "MM-dd-YYYY HH:mm:ss";
    DecimalFormat formatter = new DecimalFormat("#,###");

    public FileAdapter(Activity context, File[] list) {
        super(context, R.layout.message_rowlayout, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            rowView = LayoutInflater.from(context).inflate(R.layout.file_rowlayout,parent,false);
        }

        File currentFile = list[position];

        TextView textFileName = rowView.findViewById(R.id.textView_fileName);
        SpannableString content = new SpannableString(currentFile.getName());
        content.setSpan(new UnderlineSpan(), 0, currentFile.getName().length(), 0);
        textFileName.setText(content, TextView.BufferType.SPANNABLE);

        TextView textDateModified = rowView.findViewById(R.id.textView_dateModified);
        SimpleDateFormat outputFormat = new SimpleDateFormat(DATE_MM_DD_YYYY_TIME);
        long val = currentFile.lastModified();
        Date date=new Date(val);
        textDateModified.setText(outputFormat.format(date));

        TextView textFileSize = rowView.findViewById(R.id.textView_size);
        textFileSize.setText(String.format("%,d", currentFile.length())+" KB");




        return rowView;
    }

}
