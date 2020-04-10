package com.yfortier.koifaire;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private boolean isDatabaseLoaded;
    private boolean isPermissionGiven;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.splash);

        // Splash random
        int[] ListOfImages = {R.drawable.splash1, R.drawable.splash2, R.drawable.splash3, R.drawable.splash4, R.drawable.splash5};
        Random random = new Random(System.currentTimeMillis());
        int posOfImage = random.nextInt(ListOfImages.length);
        imageView.setBackgroundResource(ListOfImages[posOfImage]);

        // A-t-on la permission de le faire ?
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            setPermissionGiven(true);
        }
        //Connexion à la BDD
        new FirebaseDatabaseHelper().readDatabase(new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataIsLoaded(List<Festival> festivals, List<String> keys) {
                MapsActivity.festivals = festivals;
                setDatabaseLoaded(true);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setPermissionGiven(true);
            } else {
                finish();
            }
        }
    }

    public void setPermissionGiven(boolean permissionGiven) {
        isPermissionGiven = permissionGiven;
        startMapsActity();
    }

    public void setDatabaseLoaded(boolean databaseLoaded) {
        isDatabaseLoaded = databaseLoaded;
        startMapsActity();
    }

    public void startMapsActity() {
        if (isPermissionGiven && isDatabaseLoaded) {
            Intent i = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(i);
            finish();
        }

    }
}
