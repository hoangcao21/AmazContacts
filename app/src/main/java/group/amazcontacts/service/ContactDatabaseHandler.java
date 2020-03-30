package group.amazcontacts.service;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.UserDictionary;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import group.amazcontacts.model.Contact;

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
                return "Successfully";
            }else{
                return "Not Sucessfully";
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
}
