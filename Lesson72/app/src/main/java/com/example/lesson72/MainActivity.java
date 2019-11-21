package com.example.lesson72;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements GpsDataStorage.Listener {

    private static final int PERMISSION_REQUEST_CODE = 1234;
    private SimpleDateFormat mTimestampFmt;
    private TextView textView;

    private final MyLocationCallback mLocationCallback = new MyLocationCallback();

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                        && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    throw new RuntimeException("Error");
                }
            }
        }
        prepareLocationService();
    }

    @Override
    public void onDataChanged(GpsDataStorage sender) {
        final List<GpsDataStorage.Data> array = sender.getAll();
        final Formatter formatter = new Formatter();
        formatter.format("%12s %12s %12s ", "Время", "Latitude", "Longitude");
        for (int i = 0; i < array.size(); ++i) {
            final GpsDataStorage.Data data = array.get(i);
            final String timestamp = mTimestampFmt.format(new Date(data.timestamp));
            if (data.success) {
                formatter.format("%12s %12.6f %12.6f ", timestamp, data.latitude, data.longitude);
            } else {
                formatter.format("%12s %12.6f %12.6f ", timestamp, "--------", "--------");
            }
        }
        textView.setText(formatter.toString());
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimestampFmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        GpsDataStorage.getInstance().setListener(this);
        textView = findViewById(R.id.view_output);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            prepareLocationService();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getFusedLocationProviderClient(this)
                .removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    private void prepareLocationService() {

        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        final LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequest)
                .addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                        launchLocationUpdates(locationRequest);
                    }
                });

    }

    @SuppressLint("MissingPermission")
    private void launchLocationUpdates(final LocationRequest locationRequest) {
        getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    private void onLocationChanged(final Location lastLocation) {
        if (lastLocation != null) {
            GpsDataStorage.getInstance().addData(lastLocation.getLatitude(), lastLocation.getLongitude());
        } else {
            GpsDataStorage.getInstance().addError();
        }
    }

    private class MyLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(final LocationResult locationResult) {
            onLocationChanged(locationResult.getLastLocation());
        }
    }
}
