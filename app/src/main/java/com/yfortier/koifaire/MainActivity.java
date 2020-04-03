package com.yfortier.koifaire;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Splash random
        int[] yourListOfImages = {R.drawable.splash1, R.drawable.splash2, R.drawable.splash3, R.drawable.splash4, R.drawable.splash5};
        Random random = new Random(System.currentTimeMillis());
        int posOfImage = random.nextInt(yourListOfImages.length);
        ImageView imageView = findViewById(R.id.splash);
        Log.e("Image : ", String.valueOf(posOfImage));
        imageView.setBackgroundResource(yourListOfImages[posOfImage]);

        //Location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);

        }
        //Connexion à la BDD
        new FirebaseDatabaseHelper().readDatabase(new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataIsLoaded(List<Festival> festivals, List<String> keys) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                MapsActivity.festivals = festivals;
                startActivity(i);
            }
        });
    }
}
