package org.tmacoin.post.android.tmitter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.tma.blockchain.Wallet;
import org.tma.peer.thin.SecureMessage;
import org.tma.peer.thin.TwitterAccount;
import org.tma.util.Encryptor;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;
import org.tmacoin.post.android.persistance.AddressStore;

import java.util.Date;
import java.util.List;

public class TmitterAdapter extends ArrayAdapter<TwitterAccount> {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private final Activity context;
    private final List<TwitterAccount> list;

    static class ViewHolder {
        public TextView accountName;
        public TextView dateTextView;
        public TextView descriptionTextView;
    }

    public TmitterAdapter(Activity context, List<TwitterAccount> list) {
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
            rowView = inflater.inflate(R.layout.message_rowlayout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.accountName = (TextView) rowView.findViewById(R.id.from);
            viewHolder.dateTextView = (TextView) rowView.findViewById(R.id.subjectTextView);
            viewHolder.descriptionTextView = (TextView) rowView.findViewById(R.id.dateTextView);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        TwitterAccount twitterAccount = list.get(position);

        holder.accountName.setText(twitterAccount.getName());
        holder.dateTextView.setText(new Date(twitterAccount.getTimeStamp()).toString());
        holder.descriptionTextView.setText(twitterAccount.getDescription());

        return rowView;
    }

}
