package mng.com.googleautocompletesample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private static final LatLngBounds DEFAULT_BOUNDS = new LatLngBounds(new LatLng(0, 0), new LatLng(0, 0));

    private EditText mAutocompleteView;
    private ListView listView;
    private PlacesListViewAdapter mAutoCompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.recyclerView);
        mAutocompleteView = (EditText) findViewById(R.id.autocomplete_places);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void searchPlace(final Editable editable) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!editable.toString().equals("") && mGoogleApiClient.isConnected()) {
                    try {
                        mAutoCompleteAdapter.getFilter().filter(editable.toString());

                    } catch (Exception e) {
                        Log.e(TAG, "Exception");

                    }
                }
            }
        });

    }

    @Override
    public void onResume() {
        buildGoogleApiClient();
        super.onResume();

        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }

        mAutocompleteView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Utils.hideKeyboard(MainActivity.this);

                }
                return false;
            }
        });
        
        mAutoCompleteAdapter = new PlacesListViewAdapter(mGoogleApiClient, DEFAULT_BOUNDS, null, MainActivity.this);
        listView.setAdapter(mAutoCompleteAdapter);

        // https://developers.google.com/places/web-service/autocomplete#place_autocomplete_responses
        // The Google Places API Web Service returns up to 5 results.

        mAutocompleteView.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(final Editable s) {
                searchPlace(s);

            }

        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final PlaceAutocomplete item = mAutoCompleteAdapter.getItem(i);
                final String placeId = String.valueOf(item.placeId);

                Utils.hideKeyboard(MainActivity.this);

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

}