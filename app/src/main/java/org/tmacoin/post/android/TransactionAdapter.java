package org.tmacoin.post.android;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.tma.blockchain.Transaction;
import org.tma.peer.thin.Ratee;
import org.tma.util.TmaLogger;

import java.util.Date;
import java.util.List;

public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private final Activity context;
    private final List<Transaction> list;

    static class ViewHolder {
        public TextView sender;
        public TextView recipient;
        public TextView value;
    }

    public TransactionAdapter(Activity context, List<Transaction> list) {
        super(context, R.layout.transaction_rowlayout, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.transaction_rowlayout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.value = (TextView) rowView.findViewById(R.id.value);
            viewHolder.sender = (TextView) rowView.findViewById(R.id.sender);
            viewHolder.recipient = (TextView) rowView.findViewById(R.id.recipient);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Transaction transaction = list.get(position);

        holder.value.setText("Amount: " + transaction.getValue().toNumberOfCoins() + " coins");
        holder.sender.setText("Sender: " + transaction.getSenderAddress());
        holder.recipient.setText("Recipient: " + transaction.getRecipient());

        return rowView;
    }

}
