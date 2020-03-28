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

    public String getContactFromID(String id, int newStarred){
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.CommonDataKinds.Phone._ID + "=? AND " +
                            ContactsContract.Data.MIMETYPE + "='" +
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'", new String[] {id})
                    .withValue(ContactsContract.CommonDataKinds.Phone.STARRED, newStarred)
                    .build());

            ContentProviderResult[] result =  parentActivty.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            if(result.length >= 0 ){
                return  "Sucessfully";
            }else {
                return  "Failed";
            }
        } catch (Exception e) {
            Log.w("UpdateContact", e.getMessage()+"");
            e.printStackTrace();
            return "Something Went Wrong";
        }
    }
}
