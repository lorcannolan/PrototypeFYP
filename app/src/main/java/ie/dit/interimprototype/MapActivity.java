package ie.dit.interimprototype;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private GoogleMap myMap;
    private EditText courseSearch;
    private FloatingActionButton save;

    private Address address;
    private LatLng courseLatLang;

    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        courseSearch = (EditText) findViewById(R.id.searchText);
        save = (FloatingActionButton) findViewById(R.id.save);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);

        // Catch enter/search button entered
        courseSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    String course = courseSearch.getText().toString();

                    Geocoder geocoder = new Geocoder(MapActivity.this);
                    List<Address> list = new ArrayList<>();
                    try {
                        list = geocoder.getFromLocationName(course, 1);
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                    if (list.size() > 0) {
                        address = list.get(0);
                        LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
                        courseLatLang = latlng;
                        Log.d(TAG, address.toString());

                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f));

                        MarkerOptions courseLoc = new MarkerOptions()
                                .position(latlng)
                                .title(address.getAddressLine(0));
                        myMap.addMarker(courseLoc);
                    }
                }
                return false;
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = courseRef.push().getKey();
                courseRef.child(id).child("name").setValue(courseSearch.getText().toString());
                courseRef.child(id).child("Address").setValue(address.getAddressLine(0));
                courseRef.child(id).child("location").setValue(courseLatLang);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }
}
