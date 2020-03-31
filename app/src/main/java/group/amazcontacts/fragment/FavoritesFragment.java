package group.amazcontacts.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.util.List;

import group.amazcontacts.R;
import group.amazcontacts.activity.ContactDetailActivity;
import group.amazcontacts.adapter.ContactAdapter;
import group.amazcontacts.adapter.ContactDetailAdapter;
import group.amazcontacts.model.Contact;
import group.amazcontacts.service.ContactDatabaseHandler;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final String TAG = this.getClass().getSimpleName();

    private String mParam1;
    private String mParam2;

    private static ListView favListView;
    private static ProgressBar progressBar;
    private static AppCompatActivity parentActivty;
    private static TextView emotyTextView;
    private static ContactAdapter contactAdapter;
    public FavoritesFragment() {
        // Required empty public constructor
    }

    public AppCompatActivity getParentActivty() {
        return parentActivty;
    }

    public void setParentActivty(AppCompatActivity parentActivty) {
        this.parentActivty = parentActivty;
    }

    public static ListView getFavListView() {
        return favListView;
    }

    public  void setFavListView(ListView favListView) {
        this.favListView = favListView;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoritesFragment.
     */

    public static FavoritesFragment newInstance(String param1, String param2) {
        FavoritesFragment fragment = new FavoritesFragment();
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
        View v = inflater.inflate(R.layout.favorites_fragment, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        favListView = view.findViewById(R.id.fav_list_view);
        favListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        favListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getParentActivty() , ContactDetailActivity.class);
                // data
                i.putExtra("contact",contactAdapter.getContactList().get(position));
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
        progressBar = view.findViewById(R.id.loading_progress_bar);
        emotyTextView = view.findViewById(R.id.empty_textView);
        loadListFavoriteToScreen();
    }

    private static void startLoading(){
        progressBar.setVisibility(View.VISIBLE);
    }
    private static void doneLoading(){
        progressBar.setVisibility(View.INVISIBLE);
    }
    private static void setEmptyString(){
        emotyTextView.setVisibility(View.VISIBLE);
    }
    private static void setNotEmptyString(){
        emotyTextView.setVisibility(View.INVISIBLE);
    }
    private void loadListFavoriteToScreen(){
        new FavoriteContactUpdateUI(FavoritesFragment.this).execute();
    }
    static class FavoriteContactUpdateUI extends AsyncTask<Void , String , ContactAdapter>{
        private FavoritesFragment favoritesFragment;

        public FavoriteContactUpdateUI(FavoritesFragment favoritesFragment) {
            this.favoritesFragment = favoritesFragment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startLoading();
            setNotEmptyString();
        }

        @Override
        protected ContactAdapter doInBackground(Void... voids) {
            ContactDatabaseHandler contactDatabaseHandler = new ContactDatabaseHandler(favoritesFragment.getParentActivty());
            List<Contact> favoriteContacts = contactDatabaseHandler.getListFavContact();
            ContactAdapter contactAdapter = new ContactAdapter(favoriteContacts, favoritesFragment.getParentActivty());
            return contactAdapter;
        }

        @Override
        protected void onPostExecute(ContactAdapter resultContactAdapter) {
            contactAdapter = resultContactAdapter;
            if(contactAdapter.getContactList().size() == 0){
                setEmptyString();
                doneLoading();
            }else{
                favListView.setAdapter(contactAdapter);
                doneLoading();
                contactAdapter.notifyDataSetChanged();
            }

        }
    }
}
