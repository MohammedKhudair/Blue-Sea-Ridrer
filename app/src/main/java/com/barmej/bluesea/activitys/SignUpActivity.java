package com.barmej.bluesea.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.barmej.bluesea.R;
import com.barmej.bluesea.data.SharedPreferencesHelper;
import com.barmej.bluesea.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    private static final int PERMISSION_REQUEST_READ_STORAGE = 1;
    private static final int REQUEST_GET_PHOTO = 2;

    FirebaseAuth mAuth;
    private boolean mReadStoragePermissionGranted;
    private Uri mPhotoUri;
    private Uri downloadUrlPhoto;
    private String userName;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SignUpActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //
        binding.createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestExternalStoragePermission();
                if (mReadStoragePermissionGranted) {
                    launchGalleryIntent();
                }
            }
        });

    }

    // التاكد من الحقول وانشاء الحساب
    private void createAccount() {
        userName = binding.username.getText().toString();
        String email = binding.editTextEmail.getText().toString();
        String password = binding.editTextPassword.getText().toString();

        if (mPhotoUri == null) {
            Toast.makeText(this, R.string.profile_photo, Toast.LENGTH_SHORT).show();
            return;
        }
        if (userName.equals("")) {
            binding.editTextInputLayoutUsername.setError(getString(R.string.username_can_not_be_empty));
            return;
        } else
            binding.editTextInputLayoutUsername.setErrorEnabled(false);

        if (!isValidEmail(email)) {
            binding.editTextInputLayoutEmail.setError(getString(R.string.is_email_valid));
            return;
        } else
            binding.editTextInputLayoutEmail.setErrorEnabled(false);
        if (password.length() < 6) {
            binding.editTextInputLayoutPassword.setError(getString(R.string.password_is_short));
            return;
        } else
            binding.editTextInputLayoutPassword.setErrorEnabled(false);

        hideForm(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SharedPreferencesHelper.setRiderId(task.getResult().getUser().getUid(), SignUpActivity.this);
                            SharedPreferencesHelper.setAssignTrip("", SignUpActivity.this);
                            uploadPhoto();
                        } else {
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            hideForm(false);
                        }
                    }
                });
    }

    //تحميل صوره المستخدم
    private void uploadPhoto() {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference photoStorageReference = firebaseStorage.getReference().child(UUID.randomUUID().toString());

        photoStorageReference.putFile(mPhotoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    photoStorageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                downloadUrlPhoto = task.getResult();
                                updateProfile();
                            } else {
                                Snackbar.make(binding.getRoot(), getString(R.string.get_photo_url_failed), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Snackbar.make(binding.getRoot(), getString(R.string.upload_image_falild), Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    // تحديث ملف المستخدم مع الاسم والصوره
    private void updateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .setPhotoUri(downloadUrlPhoto)
                .build();

        assert user != null;
        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, R.string.new_account_created, Toast.LENGTH_SHORT).show();
                    startActivity(MainActivity.getStartIntent(SignUpActivity.this));
                    finish();
                } else
                    Toast.makeText(SignUpActivity.this, R.string.failed_update_profile, Toast.LENGTH_SHORT).show();
            }
        });
    }


    // طلب اذن صلاحيه الوصول الى الذاكره
    private void requestExternalStoragePermission() {
        mReadStoragePermissionGranted = false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mReadStoragePermissionGranted = true;
        } else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_STORAGE);

    }

    //اختيار صوره من المعرض
    private void launchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Choose photo"), REQUEST_GET_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GET_PHOTO) {
            if (resultCode == RESULT_OK) {
                mPhotoUri = data.getData();
                binding.userImageView.setImageURI(mPhotoUri);
            } else
                Toast.makeText(this, R.string.profile_photo, Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private void hideForm(boolean hide) {
        if (hide) {
            binding.progressBar.setVisibility(View.VISIBLE);

            binding.editTextInputLayoutUsername.setVisibility(View.INVISIBLE);
            binding.editTextInputLayoutEmail.setVisibility(View.INVISIBLE);
            binding.editTextInputLayoutPassword.setVisibility(View.INVISIBLE);
            binding.createAccountButton.setVisibility(View.INVISIBLE);
            binding.accountButton.setVisibility(View.INVISIBLE);
            binding.userImageView.setVisibility(View.INVISIBLE);
            binding.imageView.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);

            binding.editTextInputLayoutUsername.setVisibility(View.VISIBLE);
            binding.editTextInputLayoutEmail.setVisibility(View.VISIBLE);
            binding.editTextInputLayoutPassword.setVisibility(View.VISIBLE);
            binding.createAccountButton.setVisibility(View.VISIBLE);
            binding.accountButton.setVisibility(View.VISIBLE);
            binding.userImageView.setVisibility(View.VISIBLE);
            binding.imageView.setVisibility(View.VISIBLE);
        }

    }

    public void GoToLogInActivity(View view) {
        startActivity(new Intent(this, LogInActivity.class));
    }
}