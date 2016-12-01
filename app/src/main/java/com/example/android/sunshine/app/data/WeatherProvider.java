package com.example.android.sunshine.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.style.URLSpan;

/**
 * Created by Naledi Madlopha on 2016/11/18.
 * TODO: Add a class header comment!
 */

public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private WeatherDbHelper mOpenHelper;
    private final static UriMatcher sUriMatcher = buildUriMatcher();

    private final static SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        LocationContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + LocationContract.LocationEntry.TABLE_NAME +
                        "." + LocationContract.LocationEntry._ID);
    }

    private static final String sLocationSettingSelection =
            LocationContract.LocationEntry.TABLE_NAME +
                    "." + LocationContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    private static final String sLocationSettingWithStartDateSelection =
            LocationContract.LocationEntry.TABLE_NAME +
                    "." + LocationContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " >= ? ";

    private static final String sLocationSettingWithDaySelection =
            LocationContract.LocationEntry.TABLE_NAME +
                    "." + LocationContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " = ? ";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {

        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[] {locationSetting};
        } else {
            selection = sLocationSettingWithStartDateSelection;
            selectionArgs = new String[] {locationSetting, startDate};
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getWeatherByLocationSettingWithDate(Uri uri, String[] projection, String sortOrder) {
        String day = WeatherContract.WeatherEntry.getDateFromUri(uri);
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingWithDaySelection,
                new String[] {locationSetting, day},
                null,
                null,
                sortOrder);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE: {
                retCursor = getWeatherByLocationSettingWithDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location/#"
            case LOCATION_ID: {
                retCursor =  mOpenHelper.getReadableDatabase().query(
                        LocationContract.LocationEntry.TABLE_NAME,
                        projection,
                        LocationContract.LocationEntry._ID + " = '"
                                + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LocationContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw  new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return LocationContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return LocationContract.LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);

                if (_id > 0) {
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case LOCATION: {
                long _id = db.insert(LocationContract.LocationEntry.TABLE_NAME, null, values);

                if (_id > 0) {
                    returnUri = LocationContract.LocationEntry.buildLocationUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case WEATHER: {
                rowsDeleted = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            }
            case LOCATION: {
                rowsDeleted = db.delete(LocationContract.LocationEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if ( null == selection || 0 != rowsDeleted ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case WEATHER: {
                rowsUpdated = db.update(WeatherContract.WeatherEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            }
            case LOCATION: {
                rowsUpdated = db.update(LocationContract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (  0 != rowsUpdated ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);

                        if (-1 != _id) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority, LocationContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, LocationContract.PATH_LOCATION + "/#", LOCATION_ID);

        return matcher;
    }
}