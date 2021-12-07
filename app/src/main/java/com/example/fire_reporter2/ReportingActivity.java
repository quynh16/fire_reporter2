package com.example.fire_reporter2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReportingActivity extends AppCompatActivity {
    private static final String TAG = "ReportingActivity";
    ImageView imageView;
    Button submitReport;
    Bitmap photo;
    BottomNavigationView navbar;
    ActivityResultLauncher<Intent> activityResultLauncher;
    FirebaseDatabase database;
    DatabaseReference reference;

    String user_id;
    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporting);

        ImageButton profileBtn = findViewById(R.id.profile_btn);
        profileBtn.setVisibility(View.GONE);

        imageView = findViewById(R.id.image_view);
        submitReport = findViewById(R.id.submit_report);
        navbar = findViewById(R.id.bottom_navbar);

        submitReport.setVisibility(View.INVISIBLE);

        getUserID();

        submitReport.setOnClickListener((View v) -> {
            SendEmailService.getInstance(getApplicationContext()).emailExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    SendEmailService.getInstance(getApplicationContext()).SendEmail(photo);
                    Toast.makeText(ReportingActivity.this, "Report successfully sent.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.putExtra("id", user_id);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            });
        });

        navbar.setSelectedItemId(R.id.reporting);
        navbar.setOnItemSelectedListener((@NonNull MenuItem item) -> {
            Intent intent;
            int id = item.getItemId();
            switch (id) {
                case R.id.home:
                    intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.putExtra("id", user_id);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.reporting:
                    intent = new Intent(getApplicationContext(), ReportingActivity.class);
                    intent.putExtra("id", user_id);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.map:
                    intent = new Intent(getApplicationContext(), MapActivity.class);
                    intent.putExtra("id", user_id);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });

        openCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                activityResultLauncher.launch(cameraIntent);
            } else {
                Log.w(TAG, "camera permission denied");
                Toast.makeText(this, "Camera access not enabled", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("id", user_id);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        }
    }

    private void openCamera() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    photo = (Bitmap) result.getData().getExtras().get("data");
                    imageView.setImageBitmap(photo);
                    submitReport.setVisibility(View.VISIBLE);
                }
            }
        });

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            Log.w(TAG, "Requesting camera permissions");
        } else {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            activityResultLauncher.launch(cameraIntent);
        }
    }

    private void getUserID() {
        Intent intent = getIntent();
        user_id = intent.getStringExtra("id");
    }
}