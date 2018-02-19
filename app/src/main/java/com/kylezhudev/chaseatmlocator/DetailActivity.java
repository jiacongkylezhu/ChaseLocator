package com.kylezhudev.chaseatmlocator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final String BRANCH = "branch";
    private static final String ATM = "atm";
    private BranchAtmLocation mBranchAtmLocation;
    private double mLat;
    private double mLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        String locationString = intent.getStringExtra(AtmBranchAdapter.LOCATION_EXTRA_KEY);
        mBranchAtmLocation = (new Gson()).fromJson(locationString, BranchAtmLocation.class);

        setupUI();
        setLatLng(locationString);
        setFragment();
    }


    //find view by id and set text to all views based on BranchAtmLocation
    private void setupUI(){
        ImageView mIvBranchAtm = findViewById(R.id.iv_branch_atm_detail);
        TextView mTvName = findViewById(R.id.tv_name_detail);
        TextView mTvDistance = findViewById(R.id.tv_distance_detail);
        TextView mTvAddress = findViewById(R.id.tv_address_detail);
        TextView mTvCityAndZip = findViewById(R.id.tv_city_and_zip_detail);
        TextView mTvPhoneNum = findViewById(R.id.tv_phone_number_detail);
        TextView mTvNumAtm = findViewById(R.id.tv_num_of_atms_detail);
        TextView  mTvLobbyHrs = findViewById(R.id.tv_lobby_hrs_detail);
        TextView mTvDriveUpHrs = findViewById(R.id.tv_drive_up_hrs_detail);
        TextView mTvType = findViewById(R.id.tv_type_f_detail);

        //change different icon when the locType is different
        if(mBranchAtmLocation != null){
            String locType = mBranchAtmLocation.getLocType();
            switch(locType){
                case BRANCH:
                    mIvBranchAtm.setImageResource(R.drawable.ic_branch_24dp);
                    break;
                case ATM:
                    mIvBranchAtm.setImageResource(R.drawable.ic_atm_24dp);
                    break;
            }

            mTvName.setText(mBranchAtmLocation.getName());
            mTvDistance.setText(Double.toString(mBranchAtmLocation.getDistance()));
            mTvAddress.setText(mBranchAtmLocation.getAddress());
            mTvCityAndZip.setText(mBranchAtmLocation.getCityZip());
            mTvPhoneNum.setText(mBranchAtmLocation.getReformattedPhone(mBranchAtmLocation.getPhone()));
            mTvNumAtm.setText(Integer.toString(mBranchAtmLocation.getAtms()));
            mTvLobbyHrs.setText(mBranchAtmLocation.getReformattedHrs(mBranchAtmLocation.getLobbyHrs()));
            mTvDriveUpHrs.setText(mBranchAtmLocation.getReformattedHrs(mBranchAtmLocation.getDriveUpHrs()));
            mTvType.setText(mBranchAtmLocation.getType());

        }else{
            Log.e(LOG_TAG, "BranchAtmLocation is null");
        }

    }

    //set up google map fragment
    private void setFragment(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map_fragment_in_detail);
        mapFragment.getMapAsync(this);
    }


    private void setLatLng(String branchAtmLocation){
        mBranchAtmLocation = (new Gson()).fromJson(branchAtmLocation, BranchAtmLocation.class);
        mLat = mBranchAtmLocation.getLat();
        mLng = mBranchAtmLocation.getLng();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng currentLatLng = new LatLng(mLat, mLng);
        Log.i(LOG_TAG, "LAT: " + mLat + " LNG: " + mLng);
        googleMap.addMarker(new MarkerOptions().position(currentLatLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14.0f));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(false);

    }


}
