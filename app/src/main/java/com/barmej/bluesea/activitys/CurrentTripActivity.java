package com.barmej.bluesea.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.barmej.bluesea.R;
import com.barmej.bluesea.callback.GetTripData;
import com.barmej.bluesea.callback.TripListenerUpdates;
import com.barmej.bluesea.databinding.ActivityCurrentTripBinding;
import com.barmej.bluesea.domain.TripManager;
import com.barmej.bluesea.domain.entity.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CurrentTripActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TRIP_EXTRA = "TRIP_EXTRA";
    private ActivityCurrentTripBinding binding;
    private Trip trip;
    private GoogleMap mGoogleMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCurrentTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        Intent intent = getIntent();
        if (intent != null) {
            trip = (Trip) intent.getSerializableExtra(TRIP_EXTRA);

            progressBar(true);
            TripManager.getInstance().getTripData(trip.getId(), new GetTripData() {
                @Override
                public void tripData(Trip trip) {
                    CurrentTripActivity.this.trip = trip;

                    binding.fromCountryTextView.setText(trip.getFromCountry());
                    binding.toCountryTextView.setText(trip.getToCountry());

                    if (trip.getStatus().equals(Trip.Status.AVAILABLE.name())) {
                        binding.tripStatusTextView.setText(R.string.trip_has_not_started_yet);

                    } else if (trip.getStatus().equals(Trip.Status.ON_TRIP.name())) {
                        binding.tripStatusTextView.setText(R.string.trip_underway);
                        startListeningToTrip();

                    } else if (trip.getStatus().equals(Trip.Status.ARRIVED.name())) {
                        binding.tripStatusTextView.setText(R.string.trip_arrived);

                    }
                    progressBar(false);
                }
            });
        }

    }

    //بدء الاستماع الى الرحله
    private void startListeningToTrip() {
        setMarkersLocation();

        TripManager.getInstance().startListeningToUpdates(trip, new TripListenerUpdates() {
            @Override
            public void tripListener(Trip trip) {
                LatLng latLng = new LatLng(trip.getCurrentLat(), trip.getCurrentLng());

                if (trip.getStatus().equals(Trip.Status.ARRIVED.name())) {
                    binding.tripStatusTextView.setText(R.string.trip_arrived);

                } else {
                    binding.tripStatusTextView.setText(R.string.trip_underway);
                    if (marker == null) {
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.sailingboat3);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.icon(descriptor);
                        markerOptions.title("Boat location");
                        marker = mGoogleMap.addMarker(markerOptions);
                    } else
                        marker.setPosition(latLng);
                }
            }
        });
    }

    //وضع موقع علامة البدايه والنهايه
    private void setMarkersLocation() {
        LatLng pickUpLatLng = new LatLng(trip.getPickUpLat(), trip.getPickUpLng());
        LatLng destinationLatLng = new LatLng(trip.getDestinationLat(), trip.getDestinationLng());

        BitmapDescriptor descriptor1 = BitmapDescriptorFactory.fromResource(R.drawable.pickup);
        MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.position(pickUpLatLng);
        markerOptions1.icon(descriptor1);
        mGoogleMap.addMarker(markerOptions1);

        BitmapDescriptor descriptor2 = BitmapDescriptorFactory.fromResource(R.drawable.destination);
        MarkerOptions markerOptions2 = new MarkerOptions();
        markerOptions2.position(destinationLatLng);
        markerOptions2.icon(descriptor2);
        mGoogleMap.addMarker(markerOptions2);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapView.onStop();
        TripManager.getInstance().stopListeningToUpdates(trip.getId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    private void progressBar(boolean hide) {
        if (hide) {
            binding.progressBar2.setVisibility(View.VISIBLE);
            binding.materialCardView.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar2.setVisibility(View.INVISIBLE);
            binding.materialCardView.setVisibility(View.VISIBLE);
        }
    }

}