package com.example.android.sunshine.app.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.LocationContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by Naledi Madlopha on 2016/11/03.
 * TODO: Add a class header comment!
 */

public class TestProvider extends AndroidTestCase {

    private final static String LOG_TAG = TestProvider.class.getSimpleName();

//    public void testDeleteDb() throws Throwable {
//        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
//    }



    public static String TEST_CITY_NAME = "North Pole";
    public static String TEST_LOCATION = "99705";
    public static String TEST_DATE = "20161201";


    public static ContentValues getLocationContentValues() {
        ContentValues values = new ContentValues();

        double TEST_LATITUDE = 64.772;
        double TEST_LONGITUDE = -147.355;

        values.put(LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        values.put(LocationEntry.COLUMN_COORD_LAT, TEST_LATITUDE);
        values.put(LocationEntry.COLUMN_COORD_LONG, TEST_LONGITUDE);

        return values;
    }

    ContentValues getWeatherContentValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();

        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteriods");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 322);

        return weatherValues;
    }

    public static void validateCursor(ContentValues expectedValues, Cursor valueCursor) {

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {

            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(-1 == idx);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
    }

    public void testInsertReadProvider() {
        // Create a new map of values, where column names are the keys
        ContentValues values = getLocationContentValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, values) ;
        long locationRowId = ContentUris.parseId(locationUri);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId), // Table to Query
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // Sort order
        );

        if (cursor.moveToFirst()) {
            validateCursor(values, cursor);

            ContentValues weatherValues = getWeatherContentValues(locationRowId);

            Uri weatherUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI
                    , weatherValues);
            long weatherRowId = ContentUris.parseId(weatherUri);

            // A cursor is your primary interface to the query results.
            Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI,
                    null, // leaving "columns" null just returns all the columns
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            if (weatherCursor.moveToFirst()) {
                validateCursor(weatherValues, weatherCursor);
            } else {
                // That's weird, it works on MY machine...
                fail("No weather data returned :(");
            }

            weatherCursor.close();

            // A cursor is your primary interface
            weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                    null, // leaving "columns" null just returns all the columns
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            if (weatherCursor.moveToFirst()) {
                validateCursor(weatherValues, weatherCursor);
            } else {
                // That's weird, it works on MY machine...
                fail("No weather data returned :(");
            }

            weatherCursor.close();

            // A cursor is your primary interface
            weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION, TEST_DATE),
                    null, // leaving "columns" null just returns all the columns
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            weatherCursor.close();

            // A cursor is your primary interface
            weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE),
                    null, // leaving "columns" null just returns all the columns
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );
        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }
    }

    public void testUpdateLocation() {
        testDeleteAllRecords();

        // Create a new map of values, where column names are the keys
        ContentValues values = getLocationContentValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, values) ;
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues values2 = new ContentValues(values);
        values2.put(LocationEntry._ID, locationRowId);
        values2.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(LocationEntry.CONTENT_URI,
                values2,
                LocationEntry._ID + " = ?",
                new String[] { Long.toString(locationRowId) });

        assertEquals(1, count);

        // A cursor is your primary interface
        Cursor cursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                null, // leaving "columns" null just returns all the columns
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (cursor.moveToFirst()) {
            validateCursor(values2, cursor);
        }

        cursor.close();
    }

    public void testDeleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), 0);
        cursor.close();

        cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), 0);
        cursor.close();
    }

    public void testGetType() {

        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = TEST_LOCATION;
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = TEST_DATE;
        // content://com.example.android.sunshine.app/weather/94074/20140506
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }
}
