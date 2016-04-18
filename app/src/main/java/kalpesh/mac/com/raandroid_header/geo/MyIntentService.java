package kalpesh.mac.com.raandroid_header.geo;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kalpesh.mac.com.raandroid_header.MainActivity;

/**
 * Created by clive on 3/20/14.
 */
public class MyIntentService extends IntentService {

    public static final String INPUT_CODE = "inCode";
    public static final String TAG = "tag";
    public static final String OUTPUT_LATITUDE = "outLatitude";
    public static final String OUTPUT_LONGITUDE = "outLongitude";
    public static final String OUTPUT_GEO_MODEL = "outGeoModel";
    public static final String OPTION = "option";


    String inputCode;
    LatLng coordinates;
    String tag;
    List<GeoModel> geoModel = new ArrayList<GeoModel>();

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        inputCode = intent.getStringExtra(INPUT_CODE);
        tag = intent.getStringExtra(TAG);
        coordinates = zipToLatLng(inputCode);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(
                MainActivity.ResponseReceiver.LOCAL_ACTION);

        if(inputCode == null){
            geoModel = (List<GeoModel>) intent.getSerializableExtra("geoModel");

            int i = 0;

            while (i != geoModel.size()){
                coordinates = zipToLatLng(geoModel.get(i).getPostCode());

                geoModel.get(i).setLatitude(coordinates.latitude);
                geoModel.get(i).setLongitude(coordinates.longitude);

                i++;
            }

            broadcastIntent.putExtra(OPTION, 0);
            broadcastIntent.putExtra(OUTPUT_GEO_MODEL, (Serializable) geoModel);

        }
        else {
            broadcastIntent.putExtra(OPTION, 1);
            broadcastIntent.putExtra(OUTPUT_LATITUDE, coordinates.latitude);
            broadcastIntent.putExtra(OUTPUT_LONGITUDE, coordinates.longitude);
            broadcastIntent.putExtra(TAG, tag);
        }
        LocalBroadcastManager localBroadcastManager
                = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }

    private LatLng zipToLatLng(String inputCode) {
        Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocationName(inputCode, 1);
            while (addresses.size() == 0) {
                    addresses = geocoder.getFromLocationName(inputCode, 1);
            }
            if (addresses.size() > 0) {

                return new LatLng(addresses.get(0).getLongitude(), addresses.get(0).getLatitude());

            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return new LatLng(0, 0);
    }
}
