package group.amazcontacts.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import group.amazcontacts.R;
import group.amazcontacts.model.Contact;

public class ContactAdapter extends BaseAdapter {
    private List<Contact> contactList;
    private Activity activity;

    public ContactAdapter(List<Contact> contactList, Activity activity) {
        this.contactList = contactList;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Integer.parseInt(contactList.get(position).getId());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageViewAvatar;
        TextView textViewName;
        TextView textViewPhone;
        Contact contact = contactList.get(position);

        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.contacts_list_layout, null);
        }

        imageViewAvatar = convertView.findViewById(R.id.contact_avatar);
        textViewName = convertView.findViewById(R.id.contact_name);
        textViewPhone = convertView.findViewById(R.id.contact_phone);
//        TESTING PURPOSE:
//        Glide.with(activity.getApplicationContext()).
////                load("https://www.pngkey.com/png/full/114-1149878_setting-user-avatar-in-specific-size-without-breaking.png").
////                into(imageViewAvatar);

        textViewName.setText(contact.getName());
        Glide.with(activity.getApplicationContext()).
                load(contact.getAvatar_url()).
                into(imageViewAvatar);
        textViewPhone.setText(contact.getPhoneNumbers().get(0).getPhoneNumber()); // Only 1st phone number is shown, only 'Info' shows all phone numbers


        return convertView;
    }
}
