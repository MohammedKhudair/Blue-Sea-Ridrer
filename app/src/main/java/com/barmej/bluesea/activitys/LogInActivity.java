package com.barmej.bluesea.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import com.barmej.bluesea.R;
import com.barmej.bluesea.data.SharedPreferencesHelper;
import com.barmej.bluesea.databinding.ActivityLogInBinding;
import com.barmej.bluesea.domain.entity.Rider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogInActivity extends AppCompatActivity {
    private ActivityLogInBinding binding;
    private static final String RIDER_REF_PATH = "riders";
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, LogInActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = binding.editTextEmail.getText().toString();
                String password = binding.editTextPassword.getText().toString();

                if (!isValidEmail(email)) {
                    binding.textInputLayoutEmail.setError(getString(R.string.is_email_valid));
                    return;
                } else
                    binding.textInputLayoutEmail.setErrorEnabled(false);

                if (password.length() < 6) {
                    binding.textInputLayoutPassword.setError(getString(R.string.password_is_short));
                    return;
                } else
                    binding.textInputLayoutPassword.setErrorEnabled(false);

                logIn(email, password);
            }
        });

        // Check if user is signed in.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(MainActivity.getStartIntent(LogInActivity.this));
            finish();
        }
    }

    private void logIn(String email, String password) {
        ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setTitle(R.string.app_name);
        mDialog.setMessage(getString(R.string.uploading));
        mDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SharedPreferencesHelper.setRiderId(task.getResult().getUser().getUid(), LogInActivity.this);

                            firebaseDatabase = FirebaseDatabase.getInstance();
                            firebaseDatabase.getReference(RIDER_REF_PATH).child(task.getResult().getUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Rider rider = snapshot.getValue(Rider.class);
                                    if (rider != null) {
                                        SharedPreferencesHelper.setAssignTrip(rider.getAssignedTrip(), LogInActivity.this);
                                    } else {
                                        SharedPreferencesHelper.setAssignTrip("", LogInActivity.this);
                                    }

                                    startActivity(MainActivity.getStartIntent(LogInActivity.this));
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        } else {
                            Snackbar.make(binding.getRoot(), task.getException().getMessage(), Snackbar.LENGTH_INDEFINITE).show();
                            mDialog.dismiss();
                        }
                    }
                });
    }

    private boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public void GoToSignUpActivity(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
    }
}