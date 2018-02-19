package com.kylezhudev.chaseatmlocator;


import android.os.Build;
import android.telephony.PhoneNumberUtils;

import java.text.DateFormatSymbols;
import java.util.ArrayList;

public class BranchAtmLocation {
    private String state, locType, label, address, city, zip, name,
            bank, type, phone;
    private int atms;
    private double distance, lat, lng;
    private ArrayList<String> lobbyHrs, driveUpHrs, services;

    private static final String BRANCH = "branch";
    private static final String ATM = "atms";


    public BranchAtmLocation(String state, String locType, String label, String address, String city,
                             String zip, String name, double lat, double lng, String bank, String type,
                             String phone, int atms, double distance, ArrayList<String> lobbyHrs,
                             ArrayList<String> driveUpHrs, ArrayList<String> services) {
        this.state = state;
        this.locType = locType;
        this.label = label;
        this.address = address;
        this.city = city;
        this.zip = zip;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.bank = bank;
        this.type = type;
        this.phone = phone;
        this.atms = atms;
        this.distance = distance;
        this.lobbyHrs = lobbyHrs;
        this.driveUpHrs = driveUpHrs;
        this.services = services;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLocType() {
        return locType;
    }

    public void setLocType(String locType) {
        this.locType = locType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getType(){
        return type;
    }


    //change branch first letter to uppercase or atms to all uppercase
    public String getReformattedLocType() {
        String reformatType;
        switch (locType) {
            case BRANCH:
                String firstLetter = locType.substring(0, 1).toUpperCase();
                reformatType = firstLetter + locType.substring(1);
                break;
            case ATM:
                reformatType = locType.toUpperCase();
                break;

            default:
                return locType;
        }
        return reformatType;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhone() {
        return phone;
    }

    public String getReformattedPhone(String phone){
        String formattedPhone;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PhoneNumberUtils phoneNumberUtils = new PhoneNumberUtils();
            formattedPhone = phoneNumberUtils.formatNumber(phone,"US");
            return formattedPhone;
        }

        return phone;

    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAtms() {
        return atms;
    }

    public void setAtms(int atms) {
        this.atms = atms;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public ArrayList<String> getLobbyHrs() {
        return lobbyHrs;
    }

    public void setLobbyHrs(ArrayList<String> lobbyHrs) {
        this.lobbyHrs = lobbyHrs;
    }

    public ArrayList<String> getDriveUpHrs() {
        return driveUpHrs;
    }

    public void setDriveUpHrs(ArrayList<String> driveUpHrs) {
        this.driveUpHrs = driveUpHrs;
    }

    public ArrayList<String> getServices() {
        return services;
    }

    public void setServices(ArrayList<String> services) {
        this.services = services;
    }

    //combine city, state, and zip code to one string
    public String getCityZip() {
        String city = getCity();
        String state = getState();
        String zip = getZip();
        String cityZip = city + ", " + state + " " + zip;
        return cityZip;
    }

    //combine two string arrays into one
    public String getReformattedHrs(ArrayList<String> hrsList){
        String reFormattedHrs;
        shortDaySymbols();
        Object[] hrsObject = hrsList.toArray();
        String[] hrsArray = new String[hrsObject.length];
        for(int i = 0; i < hrsArray.length; i++){
            String hrs = (String) hrsObject[i];
            if(hrs.isEmpty()){
                hrsArray[i] = "Closed";
            }else{
                hrsArray[i] = (String) hrsObject[i];
            }
        }

        StringBuilder stringBuilder = new StringBuilder(1000);

        for (int j = 0; j < hrsArray.length; j++){
            stringBuilder.append(shortDaySymbols()[j + 1] + " " + hrsArray[j] + "\n"); //since shortWeekDays starts on Sat, j + 1 makes it start on Sun
        }
        reFormattedHrs = stringBuilder.toString();
        return reFormattedHrs;



    }

    private String[] shortDaySymbols(){
        DateFormatSymbols symbols = new DateFormatSymbols();
        String[] daySymbols = symbols.getShortWeekdays();
        return daySymbols;
    }

}
