package com.kyberwara.wallpapers;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Switch serviceRunning;
    private Button selectImages;
    private Spinner units;
    private EditText period;

    private int REQUEST_CODE_PICK_IMAGE = 100;
    private int REQUEST_CODE_PERMISSIONS = 200;
    private ArrayList<Uri> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceRunning = findViewById(R.id.serviceRunning);
        selectImages = findViewById(R.id.selectImages);
        units = findViewById(R.id.units);
        period = findViewById(R.id.period);

        checkPermissions();

        selectImages.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                selectImages();
            }
        });

        if (serviceActive()) {
            serviceRunning.setChecked(true);
        }

        serviceRunning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    if (images.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No images selected.", Toast.LENGTH_SHORT).show();
                        serviceRunning.setChecked(false);
                        return;
                    }

                    if (period.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Invalid time period.", Toast.LENGTH_SHORT).show();
                        serviceRunning.setChecked(false);
                        return;
                    }

                    // Start service
                    Intent startServiceIntent = new Intent(MainActivity.this, WallpaperService.class);
                    startServiceIntent.putParcelableArrayListExtra("images", images);
                    startServiceIntent.putExtra("period", Integer.parseInt(period.getText().toString()));
                    startServiceIntent.putExtra("timeUnit", units.getItemAtPosition(units.getSelectedItemPosition()).toString());
                    startService(startServiceIntent);

                } else {

                    // Stop service
                    Intent stopServiceIntent = new Intent(MainActivity.this, WallpaperService.class);
                    stopService(stopServiceIntent);
                }
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.time_units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        units.setAdapter(adapter);

        units.setSelection(1);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.SET_WALLPAPER
            };

            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, REQUEST_CODE_PERMISSIONS);
                    break;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Insufficient permissions.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private boolean serviceActive() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WallpaperService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void selectImages() {
        Intent gallery = new Intent();
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        gallery.setType("image/*");

        String[] mimeTypes = {"image/jpg", "image/png"};
        gallery.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(gallery, REQUEST_CODE_PICK_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {
            images.clear();

            // Multiple images selected
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    images.add(imageUri);
                }

            // Single image selected
            } else if (data.getData() != null) {
                images.add(data.getData());
            }

        } else {
            Toast.makeText(getApplicationContext(), "Image selection unsuccessful.", Toast.LENGTH_SHORT).show();
        }
    }
}
