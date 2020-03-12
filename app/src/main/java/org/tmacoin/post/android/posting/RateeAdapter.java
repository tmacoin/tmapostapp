package org.tmacoin.post.android.posting;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.tma.peer.thin.Ratee;
import org.tma.peer.thin.Tweet;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.R;

import java.util.Date;
import java.util.List;

public class RateeAdapter extends ArrayAdapter<Ratee> {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private final Activity context;
    private final List<Ratee> list;

    static class ViewHolder {
        public TextView post;
        public TextView dateTextView;
        public TextView descriptionTextView;
        public TextView totalRatingTextView;
    }

    public RateeAdapter(Activity context, List<Ratee> list) {
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
            rowView = inflater.inflate(R.layout.ratee_rowlayout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.post = (TextView) rowView.findViewById(R.id.accountName);
            viewHolder.dateTextView = (TextView) rowView.findViewById(R.id.dateTextView);
            viewHolder.descriptionTextView = (TextView) rowView.findViewById(R.id.descriptionTextView);
            viewHolder.totalRatingTextView = (TextView) rowView.findViewById(R.id.totalRatingTextView);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Ratee ratee = list.get(position);

        holder.post.setText(ratee.getName());
        holder.dateTextView.setText(new Date(ratee.getTimeStamp()).toString());
        holder.descriptionTextView.setText(ratee.getDescription());
        holder.totalRatingTextView.setText(Long.toString(ratee.getTotalRating()));

        return rowView;
    }

}
