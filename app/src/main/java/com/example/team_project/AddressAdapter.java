package com.example.team_project;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AddressAdapter extends ArrayAdapter<Address> {

    private final Context context;
    private final List<Address> addresses;

    public AddressAdapter(Context context, List<Address> addresses) {
        super(context, 0, addresses);
        this.context = context;
        this.addresses = addresses;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Address address = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView textViewAddress = convertView.findViewById(android.R.id.text1);
        textViewAddress.setText(address.getAddressLine(0));
        return convertView;
    }
}
