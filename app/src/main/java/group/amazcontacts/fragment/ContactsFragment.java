package group.amazcontacts.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import group.amazcontacts.R;
import group.amazcontacts.adapter.ContactAdapter;
import group.amazcontacts.model.Contact;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static List<Contact> contactList;
    private static ListView contactListView;
    private static View thisView;

    private String mParam1;
    private String mParam2;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ListView getListView() {
        return contactListView;
    }

    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.contacts_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        thisView = view;
        setContacts(getContext(), getActivity());
    }

    public static void setContacts(Context context, Activity activity) {
        SharedPreferences pref = activity.getSharedPreferences("AmazContacts", 0); // 0 = MODE_PRIVATE
        boolean isContactPermissionGranted = pref.getBoolean("isContactPermissionGranted", false);
        if (isContactPermissionGranted) {
            contactListView = thisView.findViewById(R.id.contacts_list_view);
            contactList = getContacts(Objects.requireNonNull(context));
            ContactAdapter contactAdapter = new ContactAdapter(contactList, activity);

            contactListView.setAdapter(contactAdapter);
        }
    }

    public static List<Contact> getContacts(Context ctx) {
        List<Contact> list = new ArrayList<>();
        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // CONTACT ID
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(),
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

                    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));
                    Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                    Bitmap photo = null;
                    if (inputStream != null) {
                        photo = BitmapFactory.decodeStream(inputStream);
                        photo.getHeight();
                    }


                    Contact contact = new Contact();
                    List<List<String>> phoneNumbers = new ArrayList<>();
                    int count = 0;

                    assert cursorInfo != null;
                    while (cursorInfo.moveToNext()) {

                        if (count == 0) { // Many contacts has the same ID will be converted to ONLY one contact
                            contact.setId(id);
                            contact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                            if (photo != null)
                                contact.setAvatar_url(pURI.toString());
                            else
                                contact.setAvatar_url(Uri.parse("android.resource://group.amazcontacts/" + R.mipmap.default_contact_avatar).toString());
                        }

                        String phoneNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.
                                Phone.NUMBER));
                        String type = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.
                                Phone.TYPE));

                        List<String> phoneAndType = new ArrayList<>(); // FORMAT: TYPE, PHONE_NUMBER
                        phoneAndType.add(type);
                        phoneAndType.add(phoneNumber);

                        phoneNumbers.add(phoneAndType);
                        count = 1;
                    }
                    contact.setPhoneNumbers(phoneNumbers);
                    list.add(contact);

                    cursorInfo.close();
                }
            }
            cursor.close();
        }
        return list;
    }


}
