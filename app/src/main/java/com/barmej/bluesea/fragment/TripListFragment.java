package com.barmej.bluesea.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.barmej.bluesea.TripAdapter;
import com.barmej.bluesea.activitys.CurrentTripActivity;
import com.barmej.bluesea.activitys.TripDetailsActivity;
import com.barmej.bluesea.data.SharedPreferencesHelper;
import com.barmej.bluesea.databinding.FragmentTripsListBinding;
import com.barmej.bluesea.domain.entity.Trip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TripListFragment extends Fragment implements TripAdapter.OnTripItemClickListener {
    private static final String TRIP_EXTRA = "TRIP_EXTRA";
    private static String TRIP_REF_PATH = "trips";
    FragmentTripsListBinding binding;
    FirebaseDatabase firebaseDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTripsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<Trip> tripArrayList = new ArrayList<Trip>();
        TripAdapter adapter = new TripAdapter(tripArrayList,getContext(),this);
        binding.recyclerview.setAdapter(adapter);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar(true);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference(TRIP_REF_PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()){
                    tripArrayList.add(s.getValue(Trip.class));
                }
                adapter.notifyDataSetChanged();
                progressBar(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void progressBar(boolean hide) {
        if (hide) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onTripItemClicked(Trip trip) {
        if (trip.getId().equals(SharedPreferencesHelper.getAssignTrip(getContext()))) {
            Intent intent = new Intent(getActivity(), CurrentTripActivity.class);
            intent.putExtra(TRIP_EXTRA, trip);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), TripDetailsActivity.class);
            intent.putExtra(TRIP_EXTRA, trip);
            startActivity(intent);
        }
    }

}
