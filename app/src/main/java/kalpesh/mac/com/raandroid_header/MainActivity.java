package kalpesh.mac.com.raandroid_header;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import kalpesh.mac.com.raandroid_header.adapter.Adapter;
import kalpesh.mac.com.raandroid_header.geo.GeoModel;
import kalpesh.mac.com.raandroid_header.geo.MyIntentService;
import kalpesh.mac.com.raandroid_header.model.Example;
import kalpesh.mac.com.raandroid_header.model.Restaurant;
import kalpesh.mac.com.raandroid_header.observables.IRestaurant;
import kalpesh.mac.com.raandroid_header.services.Services;
import kalpesh.mac.com.raandroid_header.utilities.RxUtils;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends ListActivity {
    //Composite Subscription
    private IRestaurant _api;
    private List<Restaurant> mRestaurantList;

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private ProgressDialog pDialog;
    Toolbar toolbar;
    boolean intentNotBusy = true;

    private ResponseReceiver receiver;
    List<GeoModel> geoModel = new ArrayList<GeoModel>();
    TextView mapProgress;

    /**
     * Subscription that represents a group of Subscriptions that are unsubscribed together.
     */
    private CompositeSubscription _subscriptions = new CompositeSubscription();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mapProgress = (TextView) findViewById(R.id.mapProgress);


        _api= Services._createRestruentshubApi();

        pDialog = new ProgressDialog(this);
        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();

        pattern();

    }
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter broadcastFilter = new IntentFilter(
                ResponseReceiver.LOCAL_ACTION);
        receiver = new ResponseReceiver();

        LocalBroadcastManager localBroadcastManager =
                LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(receiver,
                broadcastFilter);
        _subscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(_subscriptions);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager localBroadcastManager =
                LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(receiver);
        RxUtils.unsubscribeIfNotNull(_subscriptions);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }
    public void pattern(){

        _subscriptions.add(_api.getRestraurent()
//http://docs.couchbase.com/developer/java-2.0/observables.html
                .timeout(5000, TimeUnit.MILLISECONDS)
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Example>>() {
                    @Override
                    public Observable<? extends Example> call(Throwable throwable) {
                        Toast.makeText(getBaseContext(), "Error ", Toast.LENGTH_SHORT).show();
                        Log.i("ERROR RX","NO MSG" );
                        return Observable.error(throwable);
                    }
                })
                          .retry()
                         .distinct()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Example>() {
                    @Override
                    public void onCompleted() {

                        int i = 0;
                        while (i != mRestaurantList.size() ) {
                            geoModel.add(new GeoModel(i,
                                    mRestaurantList.get(i).getPostcode(),
                                    mRestaurantList.get(i).getName()));
                            i++;
                        }
                        hidePDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hidePDialog();
                    }

                    @Override
                    public void onNext(Example example) {
                        mRestaurantList = example.getRestaurants();
                        System.out.println("Got: " + " (" + Thread.currentThread().getName() + ")");
                        Adapter adapt = new Adapter(getApplicationContext(), R.layout.row, mRestaurantList);
                        setListAdapter(adapt);
                    }
                }));
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        GeoModel retrievedObject = geoModel.get(position);

            startIntentService(0, mRestaurantList.get(position).getName(), mRestaurantList.get(position).getPostcode());
            super.onListItemClick(l, v, position, id);

    }

    private void openMaps(int i, double latitude, double longitude, String tag) {

        Intent intent = new Intent(getApplication(), MapsActivity.class);
        intent.putExtra("case", i);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra(MyIntentService.TAG, tag);
        intent.putExtra(MyIntentService.OUTPUT_GEO_MODEL, (Serializable) geoModel);

        startActivity(intent);
    }

    public void startMap(View view){
        startIntentService(1, "", "");
    }

    protected void startIntentService(int operation, String tag, String zipCode) {
        if(intentNotBusy) {
            mapProgress.setText("Preparing Map...");
            Intent inputIntent = new Intent(MainActivity.this,
                    MyIntentService.class);
            switch (operation) {
                case 0: {
                    inputIntent.putExtra("tag", tag);
                    inputIntent.putExtra(MyIntentService.INPUT_CODE, zipCode);
                }
                break;
                case 1: {
                    inputIntent.putExtra("geoModel", (Serializable) geoModel);
                }
                break;
            }
            startService(inputIntent);
        }
    }

    public class ResponseReceiver extends BroadcastReceiver {

        public static final String LOCAL_ACTION =
                "kalpesh.mac.com.raandroid_header.ALL_DONE";

        @Override
        public void onReceive(Context context, Intent outputIntent) {
            int operation = outputIntent.getIntExtra(MyIntentService.OPTION, 0);
            switch (operation){
                case 0:{
                    geoModel = (List<GeoModel>) outputIntent.getSerializableExtra(MyIntentService.OUTPUT_GEO_MODEL);
                    openMaps(0, 0, 0, "");
                }
                break;
                case 1:{
                    openMaps(1,
                            outputIntent.getDoubleExtra(MyIntentService.OUTPUT_LATITUDE, 0),
                            outputIntent.getDoubleExtra(MyIntentService.OUTPUT_LONGITUDE, 0),
                            outputIntent.getStringExtra(MyIntentService.TAG));
                }
                break;

            }
            intentNotBusy = true;
            mapProgress.setText("");
        }
    }



}
