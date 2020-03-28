package group.amazcontacts.adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import java.util.List;

import group.amazcontacts.R;
import group.amazcontacts.model.Contact;
import group.amazcontacts.model.PhoneNumber;

public class ContactDetailAdapter extends BaseAdapter {
    private List<PhoneNumber> phoneNumbers;
    private Context parentContext;

    public ContactDetailAdapter(List<PhoneNumber> phoneNumbers, Context parentContext) {
        this.phoneNumbers = phoneNumbers;
        this.parentContext = parentContext;
    }

    @Override
    public int getCount() {
        return phoneNumbers.size();
    }

    @Override
    public Object getItem(int position) {
        return phoneNumbers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(parentContext.getApplicationContext(), R.layout.contact_detail_item, null);
        TextView phoneType = v.findViewById(R.id.phone_number_type);
        final TextView phoneNumber = v.findViewById(R.id.phone_number);

        PhoneNumber pn = phoneNumbers.get(position);
        phoneNumber.setText(pn.getPhoneNumber());
        phoneType.setText(pn.getDetailPhoneType());
        Button btn = v.findViewById(R.id.buttonCall);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber.getText()));
                if (ActivityCompat.checkSelfPermission(parentContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                parentContext.startActivity(intent);
            }
        });
        return v;
    }
}
