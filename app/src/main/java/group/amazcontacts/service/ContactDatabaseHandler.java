package group.amazcontacts.service;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.UserDictionary;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import group.amazcontacts.R;
import group.amazcontacts.model.Contact;
import group.amazcontacts.model.PhoneNumber;

public class ContactDatabaseHandler {
    private final String TAG = this.getClass().getSimpleName();
    private AppCompatActivity parentActivty;

    public ContactDatabaseHandler(AppCompatActivity parentActivty) {
        this.parentActivty = parentActivty;
    }

    public AppCompatActivity getParentActivty() {
        return parentActivty;
    }

    public void setParentActivty(AppCompatActivity parentActivty) {
        this.parentActivty = parentActivty;
    }

    public String setContactStarById(String id, int newStarred){
        try {
            ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>();
            String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ? ";
            String [] selectionArgs = new String [] {id};
            contentProviderOperations.add(ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(selection , selectionArgs)
                    .withValue(ContactsContract.CommonDataKinds.Phone.STARRED, newStarred ).build());
            ContentProviderResult[] results = parentActivty.getContentResolver().applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);
            Log.i(TAG, "setContactStarById: result"+results);
            Log.i(TAG, "setContactStarById: getAllContact"+getAllContact());
            if (results.length >0){
                return "Mark as favorite";
            }else{
                return "Mark as not favorite ";
            }

        } catch (Exception e) {
            Log.w("UpdateContact", e.getMessage()+"");
            e.printStackTrace();
            return "Something Went Wrong";
        }
    }
    public String getAllContact(){
        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

            String [] projections = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.STARRED};
            String selection = null;
            String []selectionArgs = null;
            String sortOrder =  null;

            ContentResolver resolver = parentActivty.getContentResolver();
            Cursor cursor = resolver.query(uri , projections ,selection , selectionArgs , sortOrder);
            String result = "";
            while(cursor.moveToNext()){
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String starred = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));
                result += "Name : "+name+" Number: "+number+" Starred: "+starred;
                result += '\n';
                Log.i(TAG, "Name : "+name+" Number: "+number+" Starred: "+starred);
            }
            return result;

        } catch (Exception e) {
            Log.w("UpdateContact", e.getMessage()+"");
            e.printStackTrace();
            return "Something Went Wrong";
        }
    }
    public List<Contact> getListFavContact(){
        List<Contact> contacts = new ArrayList<>();
        ContentResolver contentResolver = parentActivty.getContentResolver();

        String [] projections = null;
        String selection = ContactsContract.CommonDataKinds.Phone.STARRED + " = ? ";
        String [] selectionArgs = {"1"};
        String sortOrder = null;
        try{
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projections, selection, selectionArgs, sortOrder);
            while(cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String starred = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.STARRED));
                boolean isStarred = starred.equalsIgnoreCase("1");
                // getavatar url
                String avatarUrl = "";
                InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(parentActivty.getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

                Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));
                Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                Bitmap photo = null;
                if (inputStream != null) {
                    photo = BitmapFactory.decodeStream(inputStream);
                }
                if (photo != null)
                    avatarUrl = pURI.toString();
                else
                    avatarUrl = Uri.parse("android.resource://group.amazcontacts/" + R.mipmap.default_contact_avatar).toString();
                // done get avatar copy from Hoàng

                int numberOfPhones = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                List<PhoneNumber> phoneNumbers = new ArrayList<>();
                if(numberOfPhones > 0 ){
                    phoneNumbers = getPhoneNumberById(id);
                }
                Contact c = new Contact(id,"",avatarUrl, name, phoneNumbers, isStarred);
                contacts.add(c);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "getListContact: failed" + e.getMessage());
        }finally {
            return contacts;
        }
    }
    public List<PhoneNumber> getPhoneNumberById(String id){
        List<PhoneNumber> contacts = new ArrayList<>();
        ContentResolver contentResolver = parentActivty.getContentResolver();

        String [] projections = null;
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?";
        String [] selectionArgs = {id};
        String sortOrder = null;
        try{
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projections, selection, selectionArgs, sortOrder);
            while(cursor.moveToNext()){
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String phoneType = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                PhoneNumber pn = new PhoneNumber(phoneType, phoneNumber);
                contacts.add(pn);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "getPhoneNumberById: Failed "+e.getMessage());
        }finally {
            return contacts;
        }
    }
}
