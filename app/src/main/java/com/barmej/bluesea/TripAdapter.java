package com.barmej.bluesea;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barmej.bluesea.callback.ReserveTrip;
import com.barmej.bluesea.data.SharedPreferencesHelper;
import com.barmej.bluesea.databinding.ItemTripBinding;
import com.barmej.bluesea.domain.TripManager;
import com.barmej.bluesea.domain.entity.Trip;

import java.util.ArrayList;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private ArrayList<Trip> tripArrayList;
    private OnTripItemClickListener itemClickListener;
    private Context context;

    public TripAdapter(ArrayList<Trip> tripArrayList, Context context, OnTripItemClickListener itemClickListener) {
        this.tripArrayList = tripArrayList;
        this.itemClickListener = itemClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public TripAdapter.TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTripBinding binding = ItemTripBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TripViewHolder(binding, itemClickListener, context);
    }

    @Override
    public void onBindViewHolder(@NonNull TripAdapter.TripViewHolder holder, int position) {
        Trip trip = tripArrayList.get(position);
        holder.bind(trip);
    }

    @Override
    public int getItemCount() {
        if (tripArrayList != null) {
            return tripArrayList.size();
        } else
            return 0;
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        private ItemTripBinding binding;
        private Trip trip;
        private Context context;

        public TripViewHolder(@NonNull ItemTripBinding binding, OnTripItemClickListener itemClickListener, Context context) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = context;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.onTripItemClicked(trip);
                }
            });
        }

        void bind(Trip trip) {
            this.trip = trip;
            if (trip.getId().equals(SharedPreferencesHelper.getAssignTrip(context))) {
                binding.dateTextView.setText(trip.getDate());
                binding.fromCountryTextView.setText(trip.getFromCountry());
                binding.toCountryTextView.setText(trip.getToCountry());

                binding.reservedTextView.setVisibility(View.VISIBLE);
                binding.availableSeatsTextView.setVisibility(View.INVISIBLE);
                binding.seatsTextView.setVisibility(View.INVISIBLE);
            } else {
                binding.dateTextView.setText(trip.getDate());
                binding.fromCountryTextView.setText(trip.getFromCountry());
                binding.toCountryTextView.setText(trip.getToCountry());
                binding.availableSeatsTextView.setText(String.valueOf(trip.getAvailableSeats()));
            }
        }
    }


    public interface OnTripItemClickListener {
        void onTripItemClicked(Trip trip);
    }
}
