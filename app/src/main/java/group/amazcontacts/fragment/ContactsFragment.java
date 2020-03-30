package group.amazcontacts.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import group.amazcontacts.R;
import group.amazcontacts.activity.ContactDetailActivity;
import group.amazcontacts.activity.MainActivity;
import group.amazcontacts.adapter.ContactAdapter;
import group.amazcontacts.model.Contact;
import group.amazcontacts.model.PhoneNumber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {
    private static List<Contact> contactList;
    private static ListView contactListView;
    private static View thisView;
    private AppCompatActivity parentActivty;
    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ListView getListView() {
        return contactListView;
    }

    public AppCompatActivity getParentActivty() {
        return parentActivty;
    }

    public void setParentActivty(AppCompatActivity parentActivty) {
        this.parentActivty = parentActivty;
    }

    public static ContactsFragment newInstance(String param1, String param2) {
        return new ContactsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.contacts_fragment, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        thisView = view;
        contactListView = thisView.findViewById(R.id.contacts_list_view);
        contactListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        contactListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            List<Integer> listPositions = new ArrayList<>();
//            Menu actionModeMenu;

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

//                actionModeMenu = menu;

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
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

                            // After deleting contacts, update UI and finish action mode tool bar (X contacts selected.)
                            new ContactsUpdateUI(getActivity()).execute("");
                            if (mode != null)
                                mode.finish();
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
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getParentActivty() , ContactDetailActivity.class);
                // data
                i.putExtra("contact",contactList.get(position));
                // avatar
                ImageView im =  view.findViewById(R.id.contact_avatar);
                Bitmap bm = ((BitmapDrawable)im.getDrawable()).getBitmap();

                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, bStream);
                byte[] byteArray = bStream.toByteArray();
                i.putExtra("avatar",byteArray);

                startActivity(i);
            }
        });
        setContacts(getContext(), getActivity());
    }


    private static Context contextX;

//    public static Context getContextX() {
//        return contextX;
//    }


    public static void setContacts(Context context, Activity activity) {
        setContacts(context, activity, "");
    }


    public static void setContacts(Context context, Activity activity, String searchKey) {
        contextX = context;
        SharedPreferences pref = activity.getSharedPreferences("AmazContacts", 0); // 0 = MODE_PRIVATE
        boolean isReadContactsPermissionGranted = pref.getBoolean("isReadContactsPermissionGranted", false);
        boolean isWriteContactsPermissionGranted = pref.getBoolean("isWriteContactsPermissionGranted", false);
        if (isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
            new ContactsUpdateUI(activity, searchKey).execute("");
        }
    }

    public static List<Contact> getContacts(Context ctx) {
        return getContacts(ctx, "");
    }

    public static List<Contact> getContacts(Context ctx, String searchKey) {
        List<Contact> list = new ArrayList<>();
        ContentResolver contentResolver = ctx.getContentResolver();
        String selection = null;
        String[] args = null;
        if (searchKey != null && !searchKey.equals("")) {
            selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ('%' || ? || '%')";
            args = new String[]{searchKey};
        }
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, selection, args, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // CONTACT ID
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                    args = new String[]{id};

                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            selection, args, null);

                    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(),
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

                    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));
                    Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                    Bitmap photo = null;
                    if (inputStream != null) {
                        photo = BitmapFactory.decodeStream(inputStream);
                    }


                    Contact contact = new Contact();
                    List<PhoneNumber> phoneNumbers = new ArrayList<>();
                    int count = 0;

                    assert cursorInfo != null;
                    while (cursorInfo.moveToNext()) {
                        if (count == 0) { // Many contacts has the same ID will be converted to ONLY one contact
                            contact.setId(id);
                            contact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                            boolean isFavored = (cursorInfo.getInt(cursorInfo.getColumnIndex(ContactsContract.Contacts.STARRED)) == 1);
                            contact.setFavored(isFavored);
                            if (photo != null)
                                contact.setAvatar_url(pURI.toString());
                            else
                                contact.setAvatar_url(Uri.parse("android.resource://group.amazcontacts/" + R.mipmap.default_contact_avatar).toString());
                        }

                        String phoneNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.
                                Phone.NUMBER));
                        String type = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.
                                Phone.TYPE));

                        PhoneNumber phoneAndType = new PhoneNumber(type, phoneNumber.replace(" ", ""));// FORMAT: TYPE, PHONE_NUMBER

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
        private String searchKey;
        private ProgressDialog dialog;

        public ContactsUpdateUI(Activity activity) {
            this.activity = activity;
            this.searchKey = "";
            dialog = new ProgressDialog(activity);
        }

        public ContactsUpdateUI(Activity activity, String searchKey) {
            this.activity = activity;
            this.searchKey = searchKey;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Loading your contacts, please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) { // Only non-GUI task
            contactList = getContacts(Objects.requireNonNull(contextX), searchKey);
            contactAdapter = new ContactAdapter(contactList, activity);
            return null;
        }

        @Override // GUI task here
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
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
            cur.close();
        }

    }


}
