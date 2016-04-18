package kalpesh.mac.com.raandroid_header;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import kalpesh.mac.com.raandroid_header.geo.GeoModel;
import kalpesh.mac.com.raandroid_header.geo.MyIntentService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    int operation;
    String tag;
    LatLng selectedLatLng;
    List<GeoModel> geoModel = new ArrayList<GeoModel>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//GET DATA FROM PREVIOUS ACTIVITY
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
        operation = bundle.getInt("case");
            switch (operation){
                case 0:
                {
                    geoModel = (List<GeoModel>) bundle.getSerializable(MyIntentService.OUTPUT_GEO_MODEL);

                }
                break;
                case 1:{
                    tag = bundle.getString(MyIntentService.TAG);
                    selectedLatLng = new LatLng(bundle.getDouble("longitude"), bundle.getDouble("latitude"));
                }
                break;
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);


        switch (operation){
            case 0:{
                int i = 0;
                while (i != geoModel.size()){
                    selectedLatLng = new LatLng(geoModel.get(i).getLongitude(), geoModel.get(i).getLatitude());
                    mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(geoModel.get(i).getName()));
                    i++;
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.488813, -0.055375), 10.0f));
            }
            break;
            case 1:{
                mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(tag));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedLatLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15f));
            }
            break;
        }

    }
}
