package com.example.distancecalculator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int PERMISSIONS_REQUEST_LOCATION = 100;
    private LocationManager locationManager;
    private TextView distanceTextView;
    private android.widget.Button resetButton;
    private Location lastLocation;
    private float totalDistance = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        distanceTextView = findViewById(R.id.distanceTextView);
        resetButton = findViewById(R.id.resetButton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        resetButton.setOnClickListener(v -> {
            totalDistance = 0.0f;
            lastLocation = null;
            updateDistanceUI();
            Toast.makeText(this, "Distance reset", Toast.LENGTH_SHORT).show();
        });

        checkLocationPermissions();
    }

    private void updateDistanceUI() {
        distanceTextView.setText(String.format("Distance: %.2f km", totalDistance));
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot calculate distance.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            Toast.makeText(this, "GPS tracking started.", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error starting GPS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Ignore inaccurate locations (e.g., accuracy > 50 meters)
        if (location.hasAccuracy() && location.getAccuracy() > 50) {
            return;
        }

        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location) / 1000; // convert to km
            // Only add if movement is more than 5 meters to avoid GPS jitter
            if (lastLocation.distanceTo(location) > 5) {
                totalDistance += distance;
                updateDistanceUI();
            }
        }
        lastLocation = location;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
