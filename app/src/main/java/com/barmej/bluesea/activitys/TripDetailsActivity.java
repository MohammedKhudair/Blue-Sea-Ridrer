package com.barmej.bluesea.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.barmej.bluesea.R;
import com.barmej.bluesea.callback.GetTripData;
import com.barmej.bluesea.callback.ReserveTrip;
import com.barmej.bluesea.callback.TripListenerUpdates;
import com.barmej.bluesea.databinding.ActivityTripDetailsBinding;
import com.barmej.bluesea.domain.TripManager;
import com.barmej.bluesea.domain.entity.Rider;
import com.barmej.bluesea.domain.entity.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

public class TripDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TRIP_EXTRA = "TRIP_EXTRA";
    private ActivityTripDetailsBinding binding;
    Trip trip;
    private GoogleMap mGoogleMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tripMapView.onCreate(savedInstanceState);
        binding.tripMapView.getMapAsync(this);

        // زر حجز الرحله
        binding.reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reserveTheTrip();
            }
        });

        //جلب بيانات الرحله من Intent
        Intent intent = getIntent();
        if (intent != null) {
            trip = (Trip) intent.getSerializableExtra(TRIP_EXTRA);

            hideForm(true);
            TripManager.getInstance().getTripData(trip.getId(), new GetTripData() {
                @Override
                public void tripData(Trip trip) {
                    TripDetailsActivity.this.trip = trip;

                    binding.dateTextView.setText(trip.getDate());
                    binding.fromCountryTextView.setText(trip.getFromCountry());
                    binding.toCountryTextView.setText(trip.getToCountry());
                    binding.availableSeatsTextView.setText(String.valueOf(trip.getAvailableSeats()));

                    hideForm(false);
                }
            });
        }

    }

    // التاكد من الاماكن المتاحه و حجز الرحله
    private void reserveTheTrip() {
        TripManager.getInstance().ensureIfThereAreAvailableSeats(trip, new ReserveTrip() {
            @Override
            public void reserve(boolean reserved) {
                if (reserved) {
                    TripManager.getInstance().reserveTheTrip(trip, TripDetailsActivity.this);
                    binding.reserveButton.setEnabled(false);
                    binding.reserveDoneTextView.setVisibility(View.VISIBLE);

                    String seats = (String) binding.availableSeatsTextView.getText();
                    int seat = Integer.parseInt(seats) - 1;
                    binding.availableSeatsTextView.setText(seat + "");

                    TripManager.getInstance().getTripData(trip.getId(), new GetTripData() {
                        @Override
                        public void tripData(Trip trip) {
                            if (trip.getStatus().equals(Trip.Status.AVAILABLE.name())) {
                                binding.reserveButton.setText(R.string.trip_has_not_started_yet);

                            } else if (trip.getStatus().equals(Trip.Status.ON_TRIP.name())) {
                                startListeningToTrip();

                            } else if (trip.getStatus().equals(Trip.Status.ARRIVED.name())) {
                                binding.reserveButton.setText(R.string.trip_has_not_started_yet);
                                TripManager.getInstance().updateTripToAvailable(trip);
                            }
                        }
                    });


                } else {
                    Snackbar.make(binding.getRoot(), R.string.no_available_seats, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    // بدأ الاستماع الى تحديثات الرحله
    private void startListeningToTrip() {
        setMarkersLocation();

        TripManager.getInstance().startListeningToUpdates(trip, new TripListenerUpdates() {
            @Override
            public void tripListener(Trip trip) {
                LatLng latLng = new LatLng(trip.getCurrentLat(), trip.getCurrentLng());

                if (trip.getStatus().equals(Trip.Status.ARRIVED.name())) {
                    binding.reserveButton.setText(R.string.trip_arrived);

                } else {
                    binding.reserveButton.setText(R.string.trip_underway);
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
        mGoogleMap = googleMap;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.tripMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.tripMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.tripMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.tripMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.tripMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.tripMapView.onDestroy();
    }

    private void hideForm(boolean hide) {
        if (hide) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.materialCardView.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.materialCardView.setVisibility(View.VISIBLE);
        }
    }
}