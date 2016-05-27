package mng.com.googleautocompletesample;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by Farouk Touzi.
 */
public class PlacesListViewAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = PlacesListViewAdapter.class.getSimpleName();

    private ArrayList<PlaceAutocomplete> mResultList;
    private ArrayList<PlaceAutocomplete> resultList;

    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds mBounds;
    private AutocompleteFilter mPlaceFilter;

    private Activity mActivity;
    private LayoutInflater mLayoutInflater;

    public PlacesListViewAdapter(GoogleApiClient googleApiClient,
                                 LatLngBounds bounds, AutocompleteFilter filter, Activity activity) {
        mGoogleApiClient = googleApiClient;
        mBounds = bounds;
        mPlaceFilter = filter;
        mActivity = activity;

        mLayoutInflater = LayoutInflater.from(activity);

    }

    @Override
    public int getCount() {
        if (mResultList != null) {
            return mResultList.size();
        }
        return 0;
    }


    @Override
    public PlaceAutocomplete getItem(int position) {
        return mResultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        MainListHolder mHolder;
        View view = convertView;

        if (convertView == null) {
            mHolder = new MainListHolder();

            view = mLayoutInflater.inflate(R.layout.places_list_view_adapter, null);
            mHolder.descriptionTextView = (TextView) view.findViewById(R.id.description);

            view.setTag(mHolder);
        } else {
            mHolder = (MainListHolder) view.getTag();
        }

        mHolder.descriptionTextView.setText(getItem(position).getDescription());

        return view;
    }

    /**
     * Returns the filter for the current set of autocomplete results.
     *
     * @return
     */
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                synchronized (filterResults) {

                    if (constraint != null) {
                        getAutocomplete(constraint);

                        if (resultList != null) {
                            filterResults.values = resultList;
                            filterResults.count = resultList.size();
                        }
                    }
                    return filterResults;
                }
            }


            @Override
            protected void publishResults(CharSequence constraint, final FilterResults filterResults) {
                synchronized (filterResults) {

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (filterResults != null && filterResults.count >= 0) {
                                mResultList = resultList;
                                notifyDataSetChanged();
                            } else {
                                notifyDataSetInvalidated();
                            }
                        }
                    });

                }

            }
        };

        return filter;
    }

    private ArrayList<PlaceAutocomplete> getAutocomplete(CharSequence constraint) {

        if (mGoogleApiClient.isConnected()) {

            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                            mBounds, mPlaceFilter);

            AutocompletePredictionBuffer autocompletePredictions = results.await(60, TimeUnit.SECONDS);
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {

                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                autocompletePredictions.release();

                return new ArrayList<>();
            }

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            resultList = new ArrayList<>(autocompletePredictions.getCount());

            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getFullText(null)));
            }

            // Release the buffer now that all data has been copied.
            autocompletePredictions.release();
            return resultList;

        }
        return null;
    }

    public class MainListHolder {
        private TextView descriptionTextView;
    }

}

