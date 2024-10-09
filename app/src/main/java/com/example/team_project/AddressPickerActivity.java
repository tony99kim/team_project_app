package com.example.team_project;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private String selectedAddress;
    private EditText editTextSearch;
    private ListView listViewAddresses;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_picker);

        Toolbar toolbar = findViewById(R.id.toolbar_address_picker);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editTextSearch = findViewById(R.id.etSearch);
        listViewAddresses = findViewById(R.id.listViewAddresses);
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList);
        listViewAddresses.setAdapter(addressAdapter);

        Button buttonSearch = findViewById(R.id.btnSearch);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = editTextSearch.getText().toString();
                if (!location.isEmpty()) {
                    searchAddresses(location);
                } else {
                    Toast.makeText(AddressPickerActivity.this, "주소를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        listViewAddresses.setOnItemClickListener((parent, view, position, id) -> {
            Address address = addressList.get(position);
            selectedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
            selectedAddress = address.getAddressLine(0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("선택된 위치"));
            addressAdapter.clear();
        });

        Button buttonSelectAddress = findViewById(R.id.btnSelectAddress);
        buttonSelectAddress.setOnClickListener(v -> {
            if (selectedLatLng != null && selectedAddress != null) {
                Intent intent = new Intent();
                intent.putExtra("selectedAddress", selectedAddress);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(AddressPickerActivity.this, "주소를 선택하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAddresses(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addressList.clear();
            List<Address> addresses = geocoder.getFromLocationName(location, 5);
            if (addresses != null && !addresses.isEmpty()) {
                addressList.addAll(addresses);
                addressAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "주소 검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}