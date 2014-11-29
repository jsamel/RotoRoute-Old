package com.example.flyboyz.rotoroute;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;


public class MapActivity extends Activity {
    private static final String[] BUILDINGS = new String[] {
            "CSIC", "Hornbake Library", "Stamp Student Union", "AVW", "EGR"
    };
    private GoogleMap map;
    private LocationManager locationManager;
    private Marker userMarker;
    private int userIcon, destIcon;
    private String url;
    private DirectionsResponse directionsResponse;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        String destination = getIntent().getStringExtra("destination");
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.mapDestination);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, BUILDINGS);
        textView.setAdapter(adapter);
        textView.setText(destination);

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean foundMatch = false;
                for (String str: BUILDINGS) {
                    if (str.equals(s.toString())) {
                        foundMatch = true;
                    }
                }

                Button button = (Button) findViewById(R.id.destSearch);
                if (foundMatch) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if(!isNetworkAvailable()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Location is not available");
            alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                } });
            AlertDialog alert = alertDialog.create();
            alert.show();
        } else {
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            userIcon = R.drawable.current_position;
            destIcon = R.drawable.red_pin;

            if (map == null) {
                map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
                if (map != null) {
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    updateLocation();
                    setDestination();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateLocation(){
        Location lastLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        double latitude = lastLoc.getLatitude();
        double longitude = lastLoc.getLongitude();
        LatLng lastLatLng = new LatLng(latitude, longitude);

        if(userMarker!=null) {
            userMarker.remove();
        }

        userMarker = map.addMarker(new MarkerOptions()
                .position(lastLatLng)
                .title("You are here")
                .icon(BitmapDescriptorFactory.fromResource(userIcon))
                .snippet("Your last recorded location"));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 13));
        //map.animateCamera(CameraUpdateFactory.newLatLng(lastLatLng), 3000, null);
    }


    public void setDestination() {
        url = "https://maps.googleapis.com/maps/api/directions/json?sensor=true&mode=walking&";
        Location lastLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        double latitude = lastLoc.getLatitude();
        double longitude = lastLoc.getLongitude();
        url += "origin=" + latitude + "," + longitude + "&";

        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.mapDestination);
        String dest = textView.getText().toString();
        url += "destination=" + dest;

        try {
            URL u = new URL(url);
            HttpGetter get = new HttpGetter();
            get.execute(u);

            /*HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpClient.execute(httpPost, localContext);

            Log.d("RESPONSE: " , response.toString());
            */
            /*InputStream in = response.getEntity().getContent();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in);
            return doc;*/
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void showMap(View view) {
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.mapDestination);
        String dest = textView.getText().toString();


    }


    private class HttpGetter extends AsyncTask<URL, Void, Void> {
        private HttpGetter() {

        }

        @Override
        protected Void doInBackground(URL... params) {
            // Create an HTTP client
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet getRequest;
            HttpResponse response;

            getRequest = new HttpGet(url);
            // Execute the request and get an input stream of the response

            try {
                response = client.execute(getRequest);
                InputStream stream = response.getEntity().getContent();

                // Use GSON to to convert the stream into Java objects
                Reader reader = new InputStreamReader(stream);

                //Log.d("Here", "About to parse JSON");
                /**/
                Gson gson = new Gson();
                directionsResponse = gson.fromJson(reader, DirectionsResponse.class);
                Log.d("parse JSON ", directionsResponse == null ? "null response" : "directions response found");


            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            //Log.d("executed: ", mapsResponse.toString());
            /*
            if(type == Type.police) {
                String policeName = policeMapsResponse.results.get(0).name;
                String policeAddress = policeMapsResponse.results.get(0).vicinity;

                TextView police = (TextView)findViewById(R.id.policeText);
                police.setText("Police Station: " + policeName);

                TextView policeAddr = (TextView)findViewById(R.id.policeInfoText);
                policeAddr.setText(policeAddress);

                detailedPoliceUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
                detailedPoliceUrl += policeMapsResponse.results.get(0).reference;
                detailedPoliceUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";

                URL u;
                try {
                    u = new URL(detailedPoliceUrl);
                    HttpDetailedGetter get = new HttpDetailedGetter(type);
                    get.execute(u);

                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                String fireName = fireMapsResponse.results.get(0).name;
                String fireAddress = fireMapsResponse.results.get(0).vicinity;

                TextView fire = (TextView)findViewById(R.id.fireText);
                fire.setText("Fire Station: " + fireName);

                TextView fireAddr = (TextView)findViewById(R.id.fireInfoText);
                fireAddr.setText(fireAddress);

                detailedFireUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
                detailedFireUrl += fireMapsResponse.results.get(0).reference;
                detailedFireUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";

                URL u;
                try {
                    u = new URL(detailedFireUrl);
                    HttpDetailedGetter get = new HttpDetailedGetter(type);
                    get.execute(u);

                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
                */

        }

    }
}
