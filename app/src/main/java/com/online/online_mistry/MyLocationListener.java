package com.online.online_mistry;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;
import java.util.Locale;

import androidx.core.app.ActivityCompat;

import static android.content.Context.LOCATION_SERVICE;

public class MyLocationListener implements LocationListener {
    Context context;
    boolean isGpsEnabled = false;
    boolean isNetworkEnabled = false;
    boolean isGpsTrackingEnabled = false;
    Location location;
    double longitude, latitude;
    int GiocoderMaxResult = 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BY_UPATES = 1000 * 60 * 1;
    private LocationManager locationManager;
    String provide_info;

    public MyLocationListener(Context context) {
        this.context = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService( LOCATION_SERVICE );
            isGpsEnabled = locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
            //alternate

            if (isGpsEnabled) {
                this.isGpsTrackingEnabled = true;
                provide_info = LocationManager.GPS_PROVIDER;


            if (!provide_info.isEmpty()) {
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION )
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION )
                        != PackageManager.PERMISSION_GRANTED) {

                    return location;
                }
                locationManager.requestLocationUpdates( provide_info,
                        MIN_TIME_BY_UPATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this );
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation( provide_info );
                    updateGpsCordinates();
                }
            }
        }

        }
        catch (Exception e){e.printStackTrace();}
        return location;
    }

    private void updateGpsCordinates()
    {
        if(location!=null)
        {
            latitude=location.getLatitude();
            longitude=location.getLongitude();
        }
    }
    public double getlatitude()
    {
        if(location!=null)
        {
            latitude=location.getLatitude();
        }
        return latitude;
    }
    public double getlongitude()
    {
        if(location!=null)
        {
            longitude=location.getLongitude();
        }
        return longitude;
    }
    @Override
    public void onLocationChanged(Location loc)
    {



    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider,
                                int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
public boolean getIsGPSTrackingEnabled()
       {
           return this.isGpsTrackingEnabled;
       }

       public List<Address> getgeoAddress()
       {
           if(location!=null)
           {
               Geocoder geocoder=new Geocoder( context, Locale.ENGLISH );
               try
               {
                   List<Address> address=geocoder.getFromLocation( latitude,longitude,this.GiocoderMaxResult );
                   return  address;

               }catch (Exception e){
                   e.printStackTrace();
               }
           }

           return  null;
       }
       public String getAddress()
       {
           List<Address> addresses=getgeoAddress(  );
           if(addresses!=null && addresses.size()>0)
           {
               // no need of this Address address=addresses.get( 0 );
               String addressline=addresses.get( 0 ).getAddressLine( 0);
               return addressline;
           }
           else
               {
                 return null;
               }

       }
       public String getLocality()
       {
           List<Address> addresses=getgeoAddress(  );
           if(addresses!=null && addresses.size()>0)
           {
               String locality = addresses.get( 0 ).getLocality();
               return locality;
           }
           else
           {
               return null;
           }

       }
       public String getPostalCode()
       {
           List<Address> addresses=getgeoAddress(  );
           if (addresses!=null && addresses.size()>0)
           {
               String postal=addresses.get( 0 ).getPostalCode();
               return postal;

           }
           else
           {
               return null;
           }
       }
       public String getCountryname()
       {
           List<Address> addresses=getgeoAddress(  );
           if(addresses!=null && addresses.size()>0)
           {
               String country=addresses.get( 0 ).getCountryName();
               return country;
           }
           else
           {
               return null;
           }
       }

}

