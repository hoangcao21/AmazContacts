package group.amazcontacts.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import group.amazcontacts.AddNewContactActivity;
import group.amazcontacts.R;
import group.amazcontacts.model.PhoneNumber;

public class PhoneInputAdapter extends BaseAdapter {
    private ArrayList<PhoneNumber> list;
    private ArrayList<String> spinnerItems;
    private Context context;

    public PhoneInputAdapter(ArrayList<PhoneNumber> list, ArrayList<String> spinnerItems, Context context) {
        this.list = list;
        this.spinnerItems = spinnerItems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Spinner spinnerPhoneType = null;
        EditText editTextPhoneNumber = null;
        Button buttonRemovePhoneNumber = null;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.phone_input_layout, null);
        }
        spinnerPhoneType = view.findViewById(R.id.spinnerPhoneType);
        editTextPhoneNumber = view.findViewById(R.id.editTextPhoneNumber);
        buttonRemovePhoneNumber = view.findViewById(R.id.buttonRemovePhoneNumber);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, spinnerItems);
        spinnerPhoneType.setAdapter(adapter);

        buttonRemovePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(position);
                notifyDataSetChanged();
            }
        });

        final PhoneNumber phoneNumber = list.get(position);
        editTextPhoneNumber.setText(phoneNumber.getPhoneNumber());


        return view;
    }

}
