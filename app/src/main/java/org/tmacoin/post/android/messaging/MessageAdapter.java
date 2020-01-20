package org.tmacoin.post.android.messaging;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.tma.blockchain.Wallet;
import org.tma.peer.thin.SecureMessage;
import org.tma.util.Base58;
import org.tma.util.Encryptor;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<SecureMessage> {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final Encryptor encryptor = new Encryptor();

    private final Activity context;
    private final List<SecureMessage> list;

    static class ViewHolder {
        public TextView from;
        public TextView subject;
        public TextView date;
    }

    public MessageAdapter(Activity context, List<SecureMessage> list) {
        super(context, R.layout.rowlayout, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.rowlayout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.from = (TextView) rowView.findViewById(R.id.from);
            viewHolder.subject = (TextView) rowView.findViewById(R.id.subject);
            viewHolder.date = (TextView) rowView.findViewById(R.id.date);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        SecureMessage secureMessage = list.get(position);
        holder.from.setText("From: " + StringUtil.getStringFromKey(secureMessage.getSender()));
        holder.subject.setText("Subject: " + getSubject(secureMessage));
        holder.date.setText("Date: " + new Date(secureMessage.getTimeStamp()).toString());

        return rowView;
    }

    private String getSubject(SecureMessage secureMessage) {
        Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
        if(!secureMessage.getRecipient().equals(wallet.getTmaAddress())) {
            return "";
        }
        try {
            String str = StringUtil.trimToNull(secureMessage.getText());
            if(str != null) {
                str = new String(encryptor.decryptAsymm(Base58.decode(str), wallet.getPrivateKey()), StandardCharsets.UTF_8);
                int index = str.indexOf("\n");
                index = index == -1? str.length(): index;
                return str.substring(0, index);
            }
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

}
