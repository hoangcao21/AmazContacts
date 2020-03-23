package group.amazcontacts.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
 * Use the {@link DialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialFragment extends Fragment {


    // TODO: Implent DIalFragment
    private static List<Contact> contactList;
    private static ListView dialListView;
    private Button btnCall;
    private Button btnDelete;
    private static String phoneNumber = "";
    private static TextView txtPhoneNumber;
    private static Context contextX;
    private static ConstraintLayout layout;
    private static Activity activity;

    // Phải liệt kê hết tất cả phím số ở đây vì Android không cho assign function cho onClick ở tab Attributes :(
    // region All numberpads
    private TextView txt_1;
    private TextView txt_2;
    private TextView txt_3;
    private TextView txt_4;
    private TextView txt_5;
    private TextView txt_6;
    private TextView txt_7;
    private TextView txt_8;
    private TextView txt_9;
    private TextView txt_Star;
    private TextView txt_0;
    private TextView txt_Tag;
    // endregions


    public DialFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Dial.
     */

    public static DialFragment newInstance(String param1, String param2) {
        return new DialFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialListView = view.findViewById(R.id.dialListVIew);
        btnCall = view.findViewById(R.id.btnCall);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnDelete.setVisibility(View.GONE);
        txtPhoneNumber = view.findViewById(R.id.txt_phoneNumber);
        layout = view.findViewById(R.id.dialLayout);
        activity = getActivity();

        isPermissionsGranted();

        txt_1 = view.findViewById(R.id.txt_1);
        txt_2 = view.findViewById(R.id.txt_2);
        txt_3 = view.findViewById(R.id.txt_3);
        txt_4 = view.findViewById(R.id.txt_4);
        txt_5 = view.findViewById(R.id.txt_5);
        txt_6 = view.findViewById(R.id.txt_6);
        txt_7 = view.findViewById(R.id.txt_7);
        txt_8 = view.findViewById(R.id.txt_8);
        txt_9 = view.findViewById(R.id.txt_9);
        txt_Star = view.findViewById(R.id.txt_Star);
        txt_0 = view.findViewById(R.id.txt_0);
        txt_Tag = view.findViewById(R.id.txt_Tag);
        setEvents();
        contextX = getContext();
    }

    public static boolean isPermissionsGranted() {
        boolean flag = false;
        SharedPreferences pref = activity.getSharedPreferences("AmazContacts", 0); // 0 = MODE_PRIVATE
        boolean isReadContactsPermissionGranted = pref.getBoolean("isReadContactsPermissionGranted", false);
        boolean isWriteContactsPermissionGranted = pref.getBoolean("isWriteContactsPermissionGranted", false);
        boolean isCallPhonePermissionGranted = pref.getBoolean("isCallPhonePermissionGranted", false);
        if (!isReadContactsPermissionGranted || !isWriteContactsPermissionGranted || !isCallPhonePermissionGranted) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
            flag = true;
        }

        return flag;
    }

    public static void setPhoneNumber(String phoneNumberX) {
        phoneNumber = phoneNumberX;
        txtPhoneNumber.setText(phoneNumber);
        dialListView.setAdapter(null);
    }

    private void setEvents() {
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber.length() > 0) {
                    phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 1);
                    txtPhoneNumber.setText(phoneNumber);
                }
                if (phoneNumber.isEmpty()) {
                    btnDelete.setVisibility(View.GONE);
                    dialListView.setAdapter(null);
                    return;
                }
                new DialUpdateUI(getActivity()).execute();
            }
        });

        txt_1.setOnClickListener(new NumberpadOnClickListener());
        txt_2.setOnClickListener(new NumberpadOnClickListener());
        txt_3.setOnClickListener(new NumberpadOnClickListener());
        txt_4.setOnClickListener(new NumberpadOnClickListener());
        txt_5.setOnClickListener(new NumberpadOnClickListener());
        txt_6.setOnClickListener(new NumberpadOnClickListener());
        txt_7.setOnClickListener(new NumberpadOnClickListener());
        txt_8.setOnClickListener(new NumberpadOnClickListener());
        txt_9.setOnClickListener(new NumberpadOnClickListener());
        txt_Star.setOnClickListener(new NumberpadOnClickListener());
        txt_0.setOnClickListener(new NumberpadOnClickListener());
        txt_Tag.setOnClickListener(new NumberpadOnClickListener());
    }

    class NumberpadOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            btnDelete.setVisibility(View.VISIBLE);
            dialNumberClick(v);
        }
    }

    private void dialNumberClick(View view) {
        TextView textView = (TextView) view;
        if (phoneNumber.length() < 10) {
            phoneNumber = phoneNumber + textView.getText().toString();
            txtPhoneNumber.setText(phoneNumber);
            new DialUpdateUI(getActivity()).execute();
        } else {
            Toast.makeText(getContext(), "Sorry, the phone number can not exceed 10 numbers!", Toast.LENGTH_LONG).show();
        }
    }

    // TODO: Chỉnh sửa lại query
    public static List<Contact> getContacts(Context ctx, String phoneNumberToFind) {
        List<Contact> list = new ArrayList<>();

        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE '%" + phoneNumberToFind + "%'",
                null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

        if (cursor.getCount() > 0) {
            int count = 0;
            cursor.moveToNext();
            Contact contact = new Contact();

            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            /*
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

            Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));
            Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

            Bitmap photo = null;
            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }
            */
            List<List<String>> phoneNumbers = new ArrayList<>();

            contact.setId(id);
            String prevName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            contact.setName(prevName);
            boolean isFavored = (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) == 1);
            contact.setFavored(isFavored);
            /*
            if (photo != null)
                contact.setAvatar_url(pURI.toString());
            else
            */
            contact.setAvatar_url(Uri.parse("android.resource://group.amazcontacts/" + R.mipmap.default_contact_avatar).toString());

            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.
                    Phone.NUMBER));
            String type = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.
                    Phone.TYPE));
            String name;

            List<String> phoneAndType = new ArrayList<>(); // FORMAT: TYPE, PHONE_NUMBER
            phoneAndType.add(type);
            phoneAndType.add(phoneNumber.replace(" ", ""));
            phoneNumbers.add(phoneAndType);
            contact.setPhoneNumbers(phoneNumbers);
            list.add(contact);

            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (contact.getName().equals(name)) {
                    phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.
                            Phone.NUMBER));
                    type = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.
                            Phone.TYPE));

                    phoneAndType = new ArrayList<>(); // FORMAT: TYPE, PHONE_NUMBER
                    phoneAndType.add(type);
                    phoneAndType.add(phoneNumber.replace(" ", ""));
                    phoneNumbers.add(phoneAndType);
                    contact.setPhoneNumbers(phoneNumbers);
                } else {
                    contact = new Contact();

                    id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    /*
                    inputStream = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(),
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

                    person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));
                    pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                    photo = null;
                    if (inputStream != null) {
                        photo = BitmapFactory.decodeStream(inputStream);
                    }
                    */
                    phoneNumbers = new ArrayList<>();

                    contact.setId(id);
                    contact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    isFavored = (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) == 1);
                    contact.setFavored(isFavored);
                    /*
                    if (photo != null)
                        contact.setAvatar_url(pURI.toString());
                    else
                    */
                    contact.setAvatar_url(Uri.parse("android.resource://group.amazcontacts/" + R.mipmap.default_contact_avatar).toString());

                    phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.
                            Phone.NUMBER));
                    type = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.
                            Phone.TYPE));

                    phoneAndType = new ArrayList<>(); // FORMAT: TYPE, PHONE_NUMBER
                    phoneAndType.add(type);
                    phoneAndType.add(phoneNumber.replace(" ", ""));
                    phoneNumbers.add(phoneAndType);
                    contact.setPhoneNumbers(phoneNumbers);
                }
                if (!prevName.equals(name)) {
                    list.add(contact);
                    prevName = name;
                }
            }
        }
        cursor.close();
        return list;
    }


    static class DialUpdateUI extends AsyncTask<String, String, String> {
        private ContactAdapter contactAdapter;
        private Activity activity;

        public DialUpdateUI(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... strings) { // Only non-GUI task
            contactList = getContacts(Objects.requireNonNull(contextX), phoneNumber);
            contactAdapter = new ContactAdapter(contactList, activity);
            return null;
        }

        @Override // GUI task here
        protected void onPostExecute(String s) {
            dialListView.setAdapter(contactAdapter);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dial_fragment, container, false);
    }
}
