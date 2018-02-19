package com.kylezhudev.chaseatmlocator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ResultListActivity extends AppCompatActivity implements LocationListener{

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String LOG_TAG = ResultListActivity.class.getSimpleName();
    private static final long REQUEST_UPDATE_INTERVAL = 5000;
    private static final long REQUEST_UPDATE_FASTEST_INTERVAL = 1000;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates";
    private static final String CURRENT_LOCATION_KEY = "current-location";
    private static final int LOCATION_LOADER_ID = 301;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingClient;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates;
    private boolean mRequestingRestartLoader;
    private String mRawResult;
    private LocationSettingsRequest mLocationSettingRequest;
    private BranchAtmLocation[] mBranchAtmLocations;



    private double mLat, mLng;
    private RecyclerView mRvResultList;
    private AtmBranchAdapter mAtmBranchAdapter;


    private LoaderManager.LoaderCallbacks<String> mLocationsLoaderCallbacks;


    private TextView mTvNetworkError;
    private TextView mTvNearbyLabel;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayoutl;


    private Context mContext = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_list);
        permissionChecker();
        mTvNetworkError = findViewById(R.id.tv_network_error);
        mTvNearbyLabel = findViewById(R.id.tv_nearby_label);
        mProgressBar = findViewById(R.id.progress_bar);
        mSwipeRefreshLayoutl = findViewById(R.id.swipe_refresh_layout);
        mRvResultList = findViewById(R.id.rv_atm_list);
        mRvResultList.setLayoutManager(new LinearLayoutManager(this));
        mRvResultList.hasFixedSize();
        mAtmBranchAdapter = new AtmBranchAdapter(this);
        mRvResultList.setAdapter(mAtmBranchAdapter);

        mRequestingLocationUpdates = false;
        mRequestingRestartLoader = false;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingClient = LocationServices.getSettingsClient(this);
        requestWifi();
        prepareLocationUpdate();
        if (isPermissionGranted()) {
            startLocationUpdate();
        }
        pullToRefresh();
    }

    private void prepareLocationUpdate() {
        //setup location callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                defineLatAndLng(mCurrentLocation);
                setLocationsLoader();
                getSupportLoaderManager().initLoader(LOCATION_LOADER_ID, null, mLocationsLoaderCallbacks);
            }
        };

        //setup location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(REQUEST_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(REQUEST_UPDATE_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //setup location setting request
        mLocationSettingRequest = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest).build();


    }


    private void startLocationUpdate() {
        mSettingClient.checkLocationSettings(mLocationSettingRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        requestLocationUpdate();
                        Log.i(LOG_TAG, "Started Location update");


                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ResolvableApiException && !isLocationEnabled()) {
                            requestFineLocationPermission();
                            showSettingDialog();
                            mRequestingLocationUpdates = true;
                        }
                    }
                });
    }

    private void requestWifi(){
        if(!isConnectedInternet()){
            showNetworkSettingDialog();
        }
    }

    private boolean isConnectedInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        showNetworkError();
        return false;

    }


    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    private void permissionChecker() {
        if (!isPermissionGranted()) {
            requestFineLocationPermission();
        }
    }

    private void requestFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }

    }

    private void showSettingDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle(getString(R.string.turn_on_location))
                .setMessage(getString(R.string.location_message))
                .setPositiveButton(getString(R.string.location_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        mRequestingLocationUpdates = true;
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        alertDialogBuilder.show();
    }

    private void showPermissionDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle(getString(R.string.grant_permission))
                .setMessage(getString(R.string.permission_message))
                .setPositiveButton(getString(R.string.allow), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestFineLocationPermission();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        alertDialogBuilder.show();
    }

    private void showNetworkSettingDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle(getString(R.string.turn_on_network))
                .setMessage(getString(R.string.network_message))
                .setPositiveButton(getString(R.string.network_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        mRequestingRestartLoader = true;
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        alertDialogBuilder.show();
    }

    private void requestLocationUpdate() {
        //have to check permission again to avoid "error found" inspection
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        Log.i(LOG_TAG, "Requested Location Update");

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length != 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdate();
            } else {
                showPermissionDialog();
            }
        }
    }



    private boolean isPermissionGranted() {
        int currentPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return currentPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void defineLatAndLng(Location currentLocation) {
        if (currentLocation != null) {
            mLat = currentLocation.getLatitude();
            mLng = currentLocation.getLongitude();
            Log.i(LOG_TAG, "Current Location: LAT: " + mCurrentLocation.getLatitude() + " LNG: " + mCurrentLocation.getLongitude());

        }
    }

    private void pullToRefresh(){
        mSwipeRefreshLayoutl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                hideAll();
                startLocationUpdate();
            }
        });

    }

    private void showNetworkError() {
        hideResults();
        mTvNetworkError.setVisibility(View.VISIBLE);
    }

    private void showResult() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mTvNetworkError.setVisibility(View.INVISIBLE);
        mRvResultList.setVisibility(View.VISIBLE);
        mTvNearbyLabel.setVisibility(View.VISIBLE);
    }

    private void hideResults(){
        mTvNearbyLabel.setVisibility(View.INVISIBLE);
        mRvResultList.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void hideAll(){
        mProgressBar.setVisibility(View.INVISIBLE);
        mTvNetworkError.setVisibility(View.INVISIBLE);
        mRvResultList.setVisibility(View.INVISIBLE);
        mTvNearbyLabel.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        outState.putParcelable(CURRENT_LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            if (savedInstanceState.keySet().contains(CURRENT_LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(CURRENT_LOCATION_KEY);
                Log.i(LOG_TAG, "Saved Location: Lat " + String.valueOf(mCurrentLocation.getLatitude()) + " Lng " + String.valueOf(mCurrentLocation.getLongitude()));
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mRequestingLocationUpdates = true;

    }

    //only restart location update when permission is granted and update is required
    @Override
    protected void onResume() {
        super.onResume();
        if (isPermissionGranted() && mRequestingLocationUpdates) {
            startLocationUpdate();
        }else if(!isPermissionGranted() && mRequestingLocationUpdates){
            requestFineLocationPermission();
        }
        if(mRequestingRestartLoader){

            hideAll();
            startLocationUpdate();
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }



    //stop location update to save battery
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

    }

    //use AsyncTackLoader to handle network request
    private void setLocationsLoader() {
        mLocationsLoaderCallbacks = new LoaderManager.LoaderCallbacks<String>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<String>(mContext) {
                    @Override
                    protected void onStartLoading() {
                        mProgressBar.setVisibility(View.VISIBLE);
                        if (mRawResult != null) {
                            deliverResult(mRawResult);
                        }
                        forceLoad();
                        Log.i(LOG_TAG, "Loader Started");
                    }

                    @Override
                    public String loadInBackground() {
                        Log.i(LOG_TAG, "Load in back ground");
                        URL url;
                        String rawJsonResults;
                        try {
                            url = Utilities.builtLocationResultUrl(Double.toString(mLat), Double.toString(mLng));
                            Log.i(LOG_TAG, "Created URL: " + url.toString());
                            rawJsonResults = Utilities.getRawJsonResults(url);
                            mRawResult = rawJsonResults;
                            Log.i(LOG_TAG, "Raw Json String " + mRawResult);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            Log.i(LOG_TAG, "fail url");
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "fail Json");
                            e.printStackTrace();
                        } catch (IOException e) {
                            Log.i(LOG_TAG, "I/O");
                            e.printStackTrace();
                        }

                        return mRawResult;
                    }

                    @Override
                    public void deliverResult(String data) {
                        mRawResult = data;
                        super.deliverResult(data);
                    }
                };
            }

            // retrieve data and pass it to RecyclerView adapter
            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
                Log.i(LOG_TAG, "On Loaded finished");
                if (data == null) {
                    showNetworkError();
                } else {
                    try {
                        mBranchAtmLocations = Utilities.getBranchAtmLocations(data);
                        mAtmBranchAdapter.saveBranchAtmResults(mBranchAtmLocations);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(mSwipeRefreshLayoutl != null){
                        mSwipeRefreshLayoutl.setRefreshing(false);
                    }

                    stopLocationUpdates();
                    showResult();
                }
            }

            @Override
            public void onLoaderReset(Loader<String> loader) {

            }
        };
    }




}
