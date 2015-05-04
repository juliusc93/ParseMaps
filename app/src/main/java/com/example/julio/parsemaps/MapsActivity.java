package com.example.julio.parsemaps;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ProgressDialog pDialog;
    List<ParseObject> ob;
    private ArrayList LongitudeList;
    private ArrayList LatitudeList;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "ul16zwahYjrrXXro5fQhzPdHyqdrMqSbFWFguh8Q", "rM4loY7OKNaXC7riGZzP2Oj48uLqjqxnyQXsSj7R");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        LatLng barranquilla = new LatLng(10.999883, -74.823112);
        mMap.addMarker(new MarkerOptions().position(barranquilla).title("Marker"));
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(barranquilla, 5);
        mMap.animateCamera(yourLocation);
    }

    public void GetData(View view){
        stopCapturing();
        Toast.makeText(getApplicationContext(), "Retrieving data...", Toast.LENGTH_SHORT).show();
        new RetrieveData().execute();
    }
    public void SaveData(View view){
        stopCapturing();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,this);
        Toast.makeText(getApplicationContext(), "Uploading to Parse...", Toast.LENGTH_SHORT).show();
    }

    public void stopCapturing() {

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        new UploadData(location).execute();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class RetrieveData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setTitle("Retrieving data from Parse");
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            LatitudeList = new ArrayList<String>();
            LongitudeList = new ArrayList<String>();
            try {
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Coordinates");
                ob = query.find();
                for (ParseObject data : ob) {
                    LatitudeList.add(data.get("latitud"));
                    LongitudeList.add(data.get("longitud"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, LatitudeList);
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            PolylineOptions rectOptions = new PolylineOptions();
            for(int i=0;i<LongitudeList.size();i++)
            {
                rectOptions.add(new LatLng(Double.parseDouble(LatitudeList.get(i).toString()),Double.parseDouble(LongitudeList.get(i).toString())));
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(LatitudeList.get(i).toString()),Double.parseDouble(LongitudeList.get(i).toString()))).title("LastMe"));
            }
            Polyline polyline = mMap.addPolyline(rectOptions);
            rectOptions.width(2);
            rectOptions.color(Color.GREEN);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(LatitudeList.get(0).toString()),Double.parseDouble(LongitudeList.get(0).toString()))).zoom(13).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.moveCamera(cameraUpdate);
            pDialog.dismiss();
        }
    }

    private class UploadData extends AsyncTask<Void, Void, Void> {

        Location location;
        public UploadData(Location location){
            super();
            this.location = location;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ParseObject coords = new ParseObject("Coordinates");
            coords.put("Latitude",location.getLatitude());
            coords.put("Longitude",location.getLongitude());
            coords.saveInBackground();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
        }

    }
}
