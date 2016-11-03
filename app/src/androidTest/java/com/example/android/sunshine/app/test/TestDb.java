package com.example.android.sunshine.app.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.renderscript.Double2;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.LocationContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Naledi Madlopha on 2016/11/03.
 * TODO: Add a class header comment!
 */

public class TestDb extends AndroidTestCase {

    private final static String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public static String TEST_CITY_NAME = "North Pole";
    public static String TEST_LOCATION_SETTING= "99705";
    public static double TEST_LATITUDE = 64.772;
    public static double TEST_LONGITUDE = -147.355;

    ContentValues getLocationContentValues() {
        ContentValues values = new ContentValues();

        values.put(LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION_SETTING);
        values.put(LocationEntry.COLUMN_COORD_LAT, TEST_LATITUDE);
        values.put(LocationEntry.COLUMN_COORD_LONG, TEST_LONGITUDE);

        return values;
    }

    ContentValues getWeatherContentValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();

        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
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

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = getLocationContentValues();

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values) ;

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME, // Table to Query
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // Columns to group by
                null, // Columns to filter by row groups
                null // Sort order
        );

        if (cursor.moveToFirst()) {
            validateCursor(values, cursor);

            ContentValues weatherValues = getWeatherContentValues(locationRowId);

            long weatherRowId;
            weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

            // Verify we got a row back.
            assertTrue(weatherRowId != -1);
            Log.d(LOG_TAG, "New weather row id: " + weatherRowId);

            // A cursor is your primary interface to the query results.
            Cursor weatherCursor = db.query(
                    WeatherEntry.TABLE_NAME, // Table to Query
                    null,
                    null, // Columns for the "where" clause
                    null, // Values for the "where" clause
                    null, // Columns to group by
                    null, // Columns to filter by row groups
                    null // Sort order
            );

            if (weatherCursor.moveToFirst()) {
                validateCursor(weatherValues, weatherCursor);
            } else {
                // That's weird, it works on MY machine...
                fail("No weather data returned :(");
            }
        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }
    }
}
