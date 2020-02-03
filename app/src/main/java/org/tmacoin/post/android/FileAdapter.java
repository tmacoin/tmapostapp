package org.tmacoin.post.android;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.tma.util.TmaLogger;

import java.io.File;
import java.util.List;

public class FileAdapter extends ArrayAdapter<File> {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private final Activity context;
    private final File[] list;

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

        TextView release = rowView.findViewById(R.id.textView_fileName);
        release.setText(currentFile.getName());

        return rowView;
    }

}
