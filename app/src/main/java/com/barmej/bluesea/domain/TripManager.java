package com.barmej.bluesea.domain;

import android.content.Context;

import androidx.annotation.NonNull;

import com.barmej.bluesea.callback.GetTripData;
import com.barmej.bluesea.callback.ReserveTrip;
import com.barmej.bluesea.callback.TripListenerUpdates;
import com.barmej.bluesea.data.SharedPreferencesHelper;
import com.barmej.bluesea.domain.entity.Rider;
import com.barmej.bluesea.domain.entity.Trip;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TripManager {
    private static TripManager INSTANCE;
    private static final String TRIP_REF_PATH = "trips";
    private static final String RIDER_REF_PATH = "riders";

    private FirebaseDatabase firebaseDatabase;
    private ValueEventListener tripValueEventListenerListener;

    private TripManager() {
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public static TripManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TripManager();
        }
        return INSTANCE;
    }


    public void ensureIfThereAreAvailableSeats(Trip trip, ReserveTrip reserveTrip) {
        firebaseDatabase.getReference(TRIP_REF_PATH).child(trip.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Trip trip = snapshot.getValue(Trip.class);
                assert trip != null;
                int availableSeats = trip.getAvailableSeats();
                reserveTrip.reserve(availableSeats != 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void reserveTheTrip(Trip trip, Context context) {
        String riderId = SharedPreferencesHelper.getRiderId(context);
        Rider rider = new Rider();
        rider.setId(riderId);
        rider.setAssignedTrip(trip.getId());

        trip.setAvailableSeats(trip.getAvailableSeats() - 1);
        trip.setReservedSeats(trip.getReservedSeats() + 1);
        firebaseDatabase.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
        firebaseDatabase.getReference(RIDER_REF_PATH).child(riderId).setValue(rider);

        SharedPreferencesHelper.setAssignTrip(trip.getId(), context);
    }

    public void isTripReserved(Context context, Trip trip, ReserveTrip reserveTrip) {
        String riderId = SharedPreferencesHelper.getRiderId(context);
        firebaseDatabase.getReference(RIDER_REF_PATH).child(riderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Rider rider = snapshot.getValue(Rider.class);
                if (rider != null) {
                    reserveTrip.reserve(trip.getId().equals(rider.getAssignedTrip()));
                } else {
                    reserveTrip.reserve(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void updateTripToAvailable(Trip trip){
        trip.setStatus(Trip.Status.AVAILABLE.name());
        firebaseDatabase.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
    }

    public void getTripData(String id, GetTripData getTripData) {
        firebaseDatabase.getReference(TRIP_REF_PATH).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Trip trip = snapshot.getValue(Trip.class);
                getTripData.tripData(trip);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void startListeningToUpdates(Trip trip, TripListenerUpdates tripListenerUpdates) {
        tripValueEventListenerListener = firebaseDatabase.getReference(TRIP_REF_PATH)
                .child(trip.getId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Trip trip = snapshot.getValue(Trip.class);
                        tripListenerUpdates.tripListener(trip);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void stopListeningToUpdates(String id) {
        if (tripValueEventListenerListener != null) {
            firebaseDatabase.getReference(TRIP_REF_PATH).child(id).removeEventListener(tripValueEventListenerListener);
            tripValueEventListenerListener = null;
        }

    }
}
