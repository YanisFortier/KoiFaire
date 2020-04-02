package com.yfortier.koifaire;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static List<Festival> festivals;

    public static LatLngBounds FranceMetroBounds = new LatLngBounds(
            new LatLng(42.6965954131, -4.32784220122),
            new LatLng(50.4644483399, 7.38468690323)
    );

    private GoogleMap mMap;
    private ImageButton btnNom;
    private ImageButton btnDomaine;
    private ImageButton btnDepartement;

    private EditText editTxtRecherche;
    private Spinner spinnerDepartements;
    private Spinner spinnerDomaines;

    private LatLng mLatLng;
    private ArrayList<MarkerOptions> mMarkers = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;

    public MapsActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup - Retrait du status bar en landscape
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


        //Setup - FindViewById
        setContentView(R.layout.activity_maps);
        btnNom = findViewById(R.id.btnNom);
        btnDomaine = findViewById(R.id.btnDomaine);
        btnDepartement = findViewById(R.id.btnDepartement);

        editTxtRecherche = findViewById(R.id.txtRecherche);
        spinnerDomaines = findViewById(R.id.spinnerDomaines);
        spinnerDepartements = findViewById(R.id.spinnerDepartements);

        //Position
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                        }
                    }
                });


        //Call de la GoogleMap
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Fonction utilisée pour formater le snippet du marker
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(MapsActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(MapsActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(MapsActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                focusFrance();
            }
        });

        //Recherche par nom
        btnNom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                focusFrance();
                rechercheNom();
            }
        });

        //Recherche par département
        btnDepartement.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.clear(); //Reset de la Map
                mMarkers.clear(); //Reset des markers

                // Traitement pour les DOM
                for (Festival festival : festivals) {
                    switch (spinnerDepartements.getSelectedItem().toString().substring(0, 3)) {
                        case "971":
                            rechercheDepartement(festival, "971");
                            break;
                        case "972":
                            rechercheDepartement(festival, "972");
                            break;
                        case "973":
                            rechercheDepartement(festival, "973");
                            break;
                        case "974":
                            rechercheDepartement(festival, "974");
                            break;
                        case "976":
                            rechercheDepartement(festival, "976");
                            break;
                        default:
                            rechercheDepartement(festival, spinnerDepartements.getSelectedItem().toString().substring(0, 2));
                    }
                }

                // Focus sur le département
                setFocus();
            }
        });

        //Recherche par domaine
        btnDomaine.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.clear(); //Reset de la Map
                mMarkers.clear();

                // On se balade dans la liste des festivals
                for (Festival festival : festivals) {
                    // Vérification si le nom correspond
                    if (festival.getDomaine().equalsIgnoreCase(spinnerDomaines.getSelectedItem().toString())) {
                        //Si c'est bon, on fait un jolie marker et on zoom
                        mLatLng = new LatLng(festival.getLatitude(), festival.getLongitude());
                        mMarkers.add(new MarkerOptions().position(mLatLng));
                        mMap.addMarker(new MarkerOptions()
                                .position(mLatLng)
                                .title(festival.getNom_de_la_manifestation())
                                .snippet("Domaine : \t" + festival.getDomaine() + "\n"
                                        + "Site Web : \t" + festival.getSite_web()));
                    }
                }
                //Focus domaine
                setFocus();

            }
        });
    }

    private void focusFrance() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(MapsActivity.FranceMetroBounds, 150), 2000, null);
    }

    private void rechercheNom() {
        mMap.clear(); //Reset de la Map
        mMarkers.clear();

        // On se balade dans la liste des festivals
        for (Festival festival : festivals) {
            // Vérification si le nom correspond
            if (festival.getNom_de_la_manifestation().equalsIgnoreCase(editTxtRecherche.getText().toString())) {
                //Si c'est bon, on fait un jolie marker et on zoom
                mLatLng = new LatLng(festival.getLatitude(), festival.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(mLatLng)
                        .title(festival.getNom_de_la_manifestation())
                        .snippet("Domaine : \t" + festival.getDomaine() + "\n"
                                + "Site Web : \t" + festival.getSite_web()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 7));
            }
        }


    }

    private void setFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MarkerOptions marker : mMarkers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    private void rechercheDepartement(Festival festival, String s) {
        if (festival.getDepartement().equals(s)) {
            mLatLng = new LatLng(festival.getLatitude(), festival.getLongitude());
            mMarkers.add(new MarkerOptions().position(mLatLng));
            mMap.addMarker(new MarkerOptions()
                    .position(mLatLng)
                    .title(festival.getNom_de_la_manifestation())
                    .snippet("Domaine : \t" + festival.getDomaine() + "\n"
                            + "Site Web : \t" + festival.getSite_web()));
        }
    }
}
