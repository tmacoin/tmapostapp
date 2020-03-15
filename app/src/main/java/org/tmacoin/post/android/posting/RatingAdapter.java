package org.tmacoin.post.android.posting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.peer.Network;
import org.tma.peer.thin.Ratee;
import org.tma.peer.thin.Rating;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SearchRateeRequest;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.TmaAndroidUtil;

import java.util.Date;
import java.util.List;

public class RatingAdapter extends ArrayAdapter<Rating> {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private final Activity context;
    private final List<Rating> list;

    static class ViewHolder {
        public TextView rater;
        public TextView post;
        public TextView rateTextView;
        public TextView dateTextView;
        public TextView commentTextView;

    }

    public RatingAdapter(Activity context, List<Rating> list) {
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
            rowView = inflater.inflate(R.layout.rating_rowlayout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.rater = (TextView) rowView.findViewById(R.id.rater);
            viewHolder.post = (TextView) rowView.findViewById(R.id.post);
            viewHolder.rateTextView = (TextView) rowView.findViewById(R.id.rateTextView);
            viewHolder.dateTextView = (TextView) rowView.findViewById(R.id.dateTextView);
            viewHolder.commentTextView = (TextView) rowView.findViewById(R.id.commentTextView);


            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final Rating rating = list.get(position);

        holder.rater.setText(rating.getRater());
        holder.rater.setPaintFlags(holder.rater.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        holder.rater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatings(rating.getRater());
            }
        });

        holder.post.setText(rating.getRatee());
        holder.post.setPaintFlags(holder.post.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        holder.post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatee(rating.getRatee(), rating.getTransactionId());
            }
        });

        holder.rateTextView.setText(rating.getRate());
        holder.dateTextView.setText(new Date(rating.getTimeStamp()).toString());
        holder.commentTextView.setText(rating.getComment());

        return rowView;
    }

    private void showRatee(String account, String transactionId) {
        Network network = Network.getInstance();
        TmaAndroidUtil.checkNetwork();

        SearchRateeRequest request = new SearchRateeRequest(network, account, transactionId);
        request.start();
        Ratee ratee = (Ratee) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(ratee == null) {
            Toast.makeText(context, "Failed to retrieve ratee. Please try again", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, ShowPostActivity.class);
        intent.putExtra("ratee", ratee);
        context.startActivity(intent);
    }

    private void showRatings(String rater) {
        Intent intent = new Intent(context, MyRatingsActivity.class);
        intent.putExtra("rater", rater);
        context.startActivity(intent);
    }

}
