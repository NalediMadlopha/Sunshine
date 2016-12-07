package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.LocationContract;
import com.example.android.sunshine.app.data.WeatherContract;

public class DetailsActivity extends ActionBarActivity {

    private static final int DETAIL_LOADER = 0;
    public  static final String DATE_KEY = "date";
    public static final String LOCATION_KEY = "location";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.details_container, new DetailsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private String mForecastStr;
        private String mLocation;

        public DetailsFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

//            if ( null != savedInstanceState) {
                getLoaderManager().initLoader(DETAIL_LOADER, null, this);
//            }

        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Add this line in order for this fragment to handle menu events
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailsfragment, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            ShareActionProvider mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // Attach an intent to this SharedActionProvider. You can update this at any time,
            // like when the user selects a new piece of data they might like to share.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null");
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_details, container, false);

            // The details activity called via intent
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);

                ((TextView) rootView.findViewById(R.id.detail_forecast_textview))
                        .setText(mForecastStr);
            }
            return rootView;
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    mForecastStr + FORECAST_SHARE_HASHTAG);

            return shareIntent;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(LOCATION_KEY, mLocation);
        }

        @Override
        public void onResume() {
            super.onResume();
            if ( null != mLocation && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String dateString = getActivity().getIntent().getStringExtra(DATE_KEY);

            // This is called when a new Loader needs to be created. This
            // fragment only uses one loader, so we don't care about checking the id.

            // For the forecast view we're showing only a small subset of the stored data.
            // Specify the columns we need.
            String[] columns = {
                    // In this case the id needs to be fully qualified with a table name, since
                    // the content provider joins the location & weather tables in the background
                    // (both have an _id column)
                    // On the one hand, that's annoying. On the other, you can search weather
                    // using the postalcode, which is only in the Location table. So the
                    // convenience is worth it
                    WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                    WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                    WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                    WeatherContract.WeatherEntry.COLUMN_DEGREES,
                    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                    // This works because the WeatherProvider returns location data joined with
                    // weather data, even though they're stored in two different tables
                    LocationContract.LocationEntry.COLUMN_LOCATION_SETTING
            };

            mLocation = Utility.getPreferredLocation(getActivity());
            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, dateString);

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed
            return new CursorLoader(
                    getActivity(),
                    weatherUri,
                    columns,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.moveToFirst()) {
                String description =
                        data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
                String dataText =
                        data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));

                double high = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
                double low = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));

                boolean isMetric = Utility.isMetric(getActivity());

                TextView dateView = (TextView) getView().findViewById(R.id.detail_date_textview);
                TextView forecastView = (TextView) getView().findViewById(R.id.detail_forecast_textview);
                TextView highView = (TextView) getView().findViewById(R.id.detail_high_textview);
                TextView lowView = (TextView) getView().findViewById(R.id.detail_low_textview);

                dateView.setText(Utility.formatDate(dataText));
                forecastView.setText(description);
                highView.setText(Utility.formatTemperature(high, isMetric) + "\u00b0");
                lowView.setText(Utility.formatTemperature(low, isMetric) + "\u00b0");

                mForecastStr = String.format("%s - %s - %s/%s", dateView.getText(),
                        forecastView.getText(), highView.getText(),
                        lowView.getText());
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
