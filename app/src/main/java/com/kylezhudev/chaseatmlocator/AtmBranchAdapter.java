package com.kylezhudev.chaseatmlocator;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

public class AtmBranchAdapter extends RecyclerView.Adapter<AtmBranchAdapter.AtmBranchViewHolder>{
    private Context mContext;
    private BranchAtmLocation[] mBranchAndAtmLocations;
    private static final String BRANCH = "branch";
    private static final String ATM = "atm";
    public static final String LOCATION_EXTRA_KEY = "location";

    public AtmBranchAdapter(Context context){
        this.mContext = context;
    }


    public class AtmBranchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mIvBranchAtm;
        private TextView mTvBranchAtm, mTvDistance, mTvName, mTvAddress, mTvCityZip;


        public AtmBranchViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mIvBranchAtm = itemView.findViewById(R.id.iv_branch_atm);
            mTvBranchAtm = itemView.findViewById(R.id.tv_branch_atm);
            mTvDistance = itemView.findViewById(R.id.tv_distance);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvAddress = itemView.findViewById(R.id.tv_address);
            mTvCityZip = itemView.findViewById(R.id.tv_city_and_zip);
        }


        //start DetailActivity when list items are clicked.
        //pass saved BranchAtmLocation to DetailActivity
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            BranchAtmLocation branchAtmLocation = mBranchAndAtmLocations[position];
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            String serializedLocation = (new Gson()).toJson(branchAtmLocation);
            intent.putExtra(LOCATION_EXTRA_KEY, serializedLocation);
            mContext.startActivity(intent);

        }
    }



    @Override
    public AtmBranchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.atm_item, parent, false);
        return new AtmBranchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AtmBranchViewHolder holder, int position) {
        BranchAtmLocation branchAtmLocation = mBranchAndAtmLocations[position];
        String locationType = branchAtmLocation.getLocType();
        //switch to different icon based on the atm/branch locType
        switch (locationType){
            case BRANCH :
                holder.mIvBranchAtm.setImageResource(R.drawable.ic_branch_24dp);
                break;
            case ATM :
                holder.mIvBranchAtm.setImageResource(R.drawable.ic_atm_24dp);
                break;
        }

        holder.mTvBranchAtm.setText(branchAtmLocation.getReformattedLocType());
        holder.mTvDistance.setText(Double.toString(branchAtmLocation.getDistance()));
        holder.mTvDistance.append(mContext.getString(R.string.miles));
        holder.mTvName.setText(branchAtmLocation.getName());
        holder.mTvAddress.setText(branchAtmLocation.getAddress());
        holder.mTvCityZip.setText(branchAtmLocation.getCityZip());




    }

    @Override
    public int getItemCount() {
        if(mBranchAndAtmLocations == null){
            return 0;
        }else{
            return mBranchAndAtmLocations.length;
        }

    }

    // use to save the results after loader is finished in ResultListActivity
    //notify the adapter that data is updated
    public void saveBranchAtmResults(BranchAtmLocation[] branchAtmLocations){
        if (branchAtmLocations != null){
            mBranchAndAtmLocations = branchAtmLocations;
            notifyDataSetChanged();
        }else{
            mBranchAndAtmLocations = null;
        }
    }



}
