package org.tmacoin.post.android.tmitter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.tma.peer.thin.Tweet;
import org.tma.peer.thin.TwitterAccount;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.R;

import java.util.Date;
import java.util.List;

public class TmeetAdapter extends ArrayAdapter<Tweet> {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private final Activity context;
    private final List<Tweet> list;

    static class ViewHolder {
        public TextView accountName;
        public TextView dateTextView;
        public TextView descriptionTextView;
    }

    public TmeetAdapter(Activity context, List<Tweet> list) {
        super(context, R.layout.tmitter_rowlayout, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.tmeet_rowlayout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.accountName = (TextView) rowView.findViewById(R.id.accountName);
            viewHolder.dateTextView = (TextView) rowView.findViewById(R.id.dateTextView);
            viewHolder.descriptionTextView = (TextView) rowView.findViewById(R.id.descriptionTextView);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Tweet tweet = list.get(position);

        holder.accountName.setText(tweet.getFromTwitterAccount());
        holder.dateTextView.setText(new Date(tweet.getTimeStamp()).toString());
        holder.descriptionTextView.setText(tweet.getText());

        return rowView;
    }

}
