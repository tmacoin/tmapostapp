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

        SpannableString content = new SpannableString(currentFile.getName());
        content.setSpan(new UnderlineSpan(), 0, currentFile.getName().length(), 0);
        release.setText(content, TextView.BufferType.SPANNABLE);

        return rowView;
    }

}
