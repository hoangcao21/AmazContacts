package group.amazcontacts.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
        TextView phoneNumber = v.findViewById(R.id.phone_number);

        PhoneNumber pn = phoneNumbers.get(position);
        phoneNumber.setText(pn.getPhoneNumber());
        phoneType.setText(pn.getPhoneType());

        return v;
    }
}
