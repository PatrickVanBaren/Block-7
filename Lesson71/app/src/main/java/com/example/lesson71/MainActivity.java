package com.example.lesson71;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GpsDataStorage.Listener {

    private static final int PERMISSION_REQUEST_COD = 123;
    private TextView textView;
    private LocationManager locationManager;
    private SimpleDateFormat mTimestampFmt;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            saveLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        GpsDataStorage.getInstance().setListener(this);

        textView = findViewById(R.id.text_view);
        mTimestampFmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            startTrackingLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_COD);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_COD) {
            for (int i = 0; i < permissions.length; i++){
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                        && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    throw new RuntimeException("ACCESS_FINE_LOCATION is absolutely required");
                }
            }
        }
        startTrackingLocation();
    }

    @SuppressLint("MissingPermission")
    private void startTrackingLocation() {
        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {//?
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        }
        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {//?
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, locationListener);
        }
        final Location lastKnowGpsLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnowGpsLocation != null) {
            saveLocation(lastKnowGpsLocation);
        } else {
            final Location lastKnowNetworkLocation =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            saveLocation(lastKnowNetworkLocation);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    public void saveLocation(Location location) {
        if (location != null) {
            GpsDataStorage.getInstance().addData(location.getLatitude(), location.getLongitude());
        } else {
            GpsDataStorage.getInstance().addError();
        }
    }

    @Override
    public void onDataChanged(GpsDataStorage sender) {
        final List<GpsDataStorage.Data> array = sender.getAll();
        final Formatter formatter = new Formatter();
        formatter.format("%12s %12s %12s", "Время", "Latitude", "Longitude");
        for (int i = 0; i < array.size(); ++i) {
            final GpsDataStorage.Data data = array.get(i);
            final String timestamp = mTimestampFmt.format(new Date(data.timestamp));
            if (data.success) {
                formatter.format("%12s %12.6f %12.6f", timestamp, data.latitude, data.longitude);
            } else {
                formatter.format("%12s %12.6f %12.6f", timestamp, "--------", "--------");
            }
        }
        textView.setText(formatter.toString());
    }
}
