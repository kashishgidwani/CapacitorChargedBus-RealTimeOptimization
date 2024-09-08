package com.example.bus;


import static java.util.Collections.addAll;
import com.google.firebase.auth.FirebaseAuth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BusSearchActivity<PolylineOptions> extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker userLocationMarker, pickupMarker, dropoffMarker;
    private LatLng pickupLatLng, dropoffLatLng;
    private PlacesClient placesClient;

    private EditText pickupLocationInput, dropoffLocationInput;
    private Button findBusStopButton, signOutButton;
    private FirebaseAuth mAuth; // Firebase authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_search);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        pickupLocationInput = findViewById(R.id.pickupLocation);
        dropoffLocationInput = findViewById(R.id.dropoffLocation);
        findBusStopButton = findViewById(R.id.findBusStopButton);
        signOutButton = findViewById(R.id.signOutButton);

        // Set up the sign-out button
        signOutButton.setOnClickListener(v -> {
            mAuth.signOut();  // Sign out from Firebase
            Toast.makeText(BusSearchActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            // Redirect to LoginActivity
            finish();
            startActivity(new Intent(BusSearchActivity.this, LoginActivity.class));
        });

        // Initialize Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set button click listener to find routes
        findBusStopButton.setOnClickListener(v -> {
            String pickupLocation = pickupLocationInput.getText().toString();
            String dropoffLocation = dropoffLocationInput.getText().toString();

            if (!pickupLocation.isEmpty() && !dropoffLocation.isEmpty()) {
                // Get LatLng from Google Places API
                fetchPlaceCoordinates(pickupLocation, true);
                fetchPlaceCoordinates(dropoffLocation, false);
            } else {
                Toast.makeText(this, "Please enter both pickup and dropoff locations", Toast.LENGTH_SHORT).show();
            }
        });

        // Request location permission
        requestLocationPermission();
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Byte PolyUtil;



    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                if (userLocationMarker != null) {
                    userLocationMarker.remove();
                }

                userLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .title("You are here"));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    // Fetch place coordinates using Google Places API
    private void fetchPlaceCoordinates(String placeName, boolean isPickup) {
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeName, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            if (place.getLatLng() != null) {
                if (isPickup) {
                    pickupLatLng = place.getLatLng();
                    addPickupMarker();
                } else {
                    dropoffLatLng = place.getLatLng();
                    addDropoffMarker();
                    if (pickupLatLng != null && dropoffLatLng != null) {
                        showRouteOnMap(pickupLatLng, dropoffLatLng);  // Fetch directions after both locations are available
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(BusSearchActivity.this, "Place not found", Toast.LENGTH_SHORT).show();
        });
    }

    private void addPickupMarker() {
        if (pickupMarker != null) pickupMarker.remove();
        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup Location"));
    }

    private void addDropoffMarker() {
        if (dropoffMarker != null) dropoffMarker.remove();
        dropoffMarker = mMap.addMarker(new MarkerOptions().position(dropoffLatLng).title("Dropoff Location"));
    }

    // Use Google Directions API to show route between pickup and dropoff locations
    private void showRouteOnMap(LatLng pickupLatLng, LatLng dropoffLatLng) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + pickupLatLng.latitude + "," +
                pickupLatLng.longitude + "&destination=" + dropoffLatLng.latitude + "," + dropoffLatLng.longitude +
                "&key=YOUR_API_KEY";

        Request request = new Request.Builder().url(url).build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();

                JSONObject jsonObject = new JSONObject(jsonData);
                JSONArray routes = jsonObject.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    String polyline = route.getJSONObject("overview_polyline").getString("points");
                    runOnUiThread(() -> drawRouteOnMap(polyline));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void drawRouteOnMap(String polyline) {
        if (mMap != null) {
//            PolylineOptions options = new PolylineOptions()
            GoogleMap googleMap;
            com.google.android.gms.maps.model.PolylineOptions options = new com.google.android.gms.maps.model.PolylineOptions()
                    .addAll(com.google.maps.android.PolyUtil.decode(polyline))
                    .width(10)
                    // Set polyline width
                    .color(Color.BLUE);

            mMap.addPolyline((com.google.android.gms.maps.model.PolylineOptions) options);
        }
    }




    @Override
    protected void onPause() {
        super.onPause();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
