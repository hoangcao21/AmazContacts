package group.amazcontacts.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import group.amazcontacts.R;
import group.amazcontacts.activity.MainActivity;
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
        contactListView = thisView.findViewById(R.id.contacts_list_view);
        contactListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        contactListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            List<Integer> listPositions = new ArrayList<>();

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (!listPositions.contains(position)) {
                    listPositions.add(position);
                } else {
                    listPositions.remove((Integer) position);
//                    contactListView.getChildAt(position).setBackgroundColor(0x00000000); // Transparent
                }

                mode.setTitle(listPositions.size() + " contact(s) selected.");
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MainActivity.getToolbar().setVisibility(View.GONE);
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.contacts_context_menu, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, final MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Are you sure to delete contacts(s)?");
                    builder.setMessage("Once your choose to delete, you can't recover it.");
                    builder.setPositiveButton("Yes, delete contact(s)", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            for (int index : listPositions) {
                                Contact contact = contactList.get(index);
                                deleteContactById(getContext(), Long.parseLong(contact.getId()));
                            }

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                MainActivity.getToolbar().setVisibility(View.VISIBLE);
                listPositions.clear();
            }
        });
        setContacts(getContext(), getActivity());
    }


    private static Context contextX;

    public static void setContacts(Context context, Activity activity) {
        contextX = context;
        SharedPreferences pref = activity.getSharedPreferences("AmazContacts", 0); // 0 = MODE_PRIVATE
        boolean isReadContactsPermissionGranted = pref.getBoolean("isReadContactsPermissionGranted", false);
        boolean isWriteContactsPermissionGranted = pref.getBoolean("isWriteContactsPermissionGranted", false);
        if (isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
            new ContactsUpdateUI(activity).execute("");
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

    static class ContactsUpdateUI extends AsyncTask<String, String, String> {
        private ContactAdapter contactAdapter;
        private Activity activity;

        public ContactsUpdateUI(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... strings) {
            contactList = getContacts(Objects.requireNonNull(contextX));
            contactAdapter = new ContactAdapter(contactList, activity);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            contactListView.setAdapter(contactAdapter);
        }
    }


    // Thực hiện xoá được nhưng máy của HoangCH (Xiaomi) có hệ điều hành MIUI không cho ứng dụng thứ 3 xoá contact ))
    // Anh em code xong test hộ tôi chức năng này trên Samsung thử coi :v
    private void deleteContactById(final Context ctx, final long id) {
        final Cursor cur = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts._ID + "="
                + id, null, null);

        if (cur != null) {
            while (cur.moveToNext()) {
                try {
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    cur.moveToFirst(); // Because cursor reach end of the database, which meanes NULL (NO data to get)
                    String lookupKey = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                            lookupKey);
                    int delete = ctx.getContentResolver().delete(uri, ContactsContract.Contacts._ID + "=" + id, null);

                } catch (Exception e) {
                    Log.e("TAG", "deleteContactById: ", e);
                }
            }

        }
        cur.close();
    }


}
