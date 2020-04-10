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
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.location.Location.distanceBetween;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static List<Festival> festivals;
    private static LatLngBounds FranceMetroBounds = new LatLngBounds(
            new LatLng(42.6965954131, -4.32784220122),
            new LatLng(50.4644483399, 7.38468690323)
    );

    //Location
    Location mCurrentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    //Interface
    private GoogleMap mMap;
    private ImageButton btnNom;
    private ImageButton btnDomaine;
    private ImageButton btnDepartement;
    private EditText editTxtRecherche;
    private Spinner spinnerDepartements;
    private Spinner spinnerDomaines;

    //Markers
    private LatLng mLatLng;
    private ArrayList<MarkerOptions> mMarkers = new ArrayList<>();

    //Dates

    public MapsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup - Retrait du status bar en landscape
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
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

        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();

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
                setFocusFrance();
            }
        });

        //Recherche par nom
        btnNom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean festivalFound = false;
                mMap.clear(); //Reset de la Map
                for (Festival festival : festivals) {
                    if (festival.getNom_de_la_manifestation().equalsIgnoreCase(editTxtRecherche.getText().toString())) {
                        festivalFound = true;
                        mLatLng = new LatLng(festival.getLatitude(), festival.getLongitude());
                        placementMarkers(festival);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 7));
                        fetchLastLocation();
                        setFocus();
                    }
                }
                if (!festivalFound)
                    Toast.makeText(MapsActivity.this, "Festival introuvable", Toast.LENGTH_LONG).show();
            }
        });

        //Recherche par domaine
        btnDomaine.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.clear(); //Reset de la Map
                for (Festival festival : festivals) {
                    if (festival.getDomaine().equalsIgnoreCase(spinnerDomaines.getSelectedItem().toString())) {
                        mLatLng = new LatLng(festival.getLatitude(), festival.getLongitude());
                        placementMarkers(festival);
                    }
                }
                fetchLastLocation();
                setFocus();
            }
        });

        //Recherche par département
        btnDepartement.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.clear(); //Reset de la Map
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
                fetchLastLocation();
                setFocus();
            }
        });
    }

    private void rechercheDepartement(Festival festival, String s) {
        if (festival.getDepartement().equals(s)) {
            mLatLng = new LatLng(festival.getLatitude(), festival.getLongitude());
            placementMarkers(festival);
        }
    }

    private void setFocusFrance() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(MapsActivity.FranceMetroBounds, 150), 2000, null);
    }

    private void setFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MarkerOptions marker : mMarkers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        mMarkers.clear(); //Reset des markers
    }

    private void placementMarkers(Festival festival) {
        float[] distance = {0}; //Initalisation de tableau pour la distance
        String dates = getDatesFestivals(festival); //Récupération des dates formatées
        distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), festival.getLatitude(), festival.getLongitude(), distance); //Calcul de la distance entre les markers
        mMarkers.add(new MarkerOptions().position(mLatLng)); //On ajoute dans un tableau dédié au focus

        //On ajoute sur la map
        mMap.addMarker(new MarkerOptions()
                .position(mLatLng)
                .title(festival.getNom_de_la_manifestation())
                .snippet(dates + "\n"
                        + "Domaine : \t" + festival.getDomaine() + "\n"
                        + "Site Web : \t" + festival.getSite_web() + "\n"
                        + "Distance : \t" + (int) distance[0] / 1000 + " km"));
    }

    private String getDatesFestivals(Festival festival) {
        String dates = null;
        SimpleDateFormat oldDateFormat =new SimpleDateFormat("yyyy-MM-dd", new Locale("fr", "FR"));
        SimpleDateFormat newDateFormat =new SimpleDateFormat("dd MMMM yyyy", new Locale("fr", "FR"));

        if (festival.getDate_de_debut() != null && festival.getDate_de_fin() != null) {
            try {
                Date dateDebutBrut = oldDateFormat.parse(festival.getDate_de_debut());
                Date dateFinBrut = oldDateFormat.parse(festival.getDate_de_fin());

                assert dateDebutBrut != null;
                assert dateFinBrut != null;

                String dateDebut = newDateFormat.format(dateDebutBrut);
                String dateFin = newDateFormat.format(dateFinBrut);
                dates = String.format("Du %1$s au %2$s", dateDebut, dateFin);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else
            dates = "Dates inconnues";

        return dates;
    }

    private void fetchLastLocation() {
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mCurrentLocation = location;
                    mLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    mMarkers.add(new MarkerOptions().position(mLatLng));
                    mMap.addMarker(new MarkerOptions()
                            .position(mLatLng)
                            .flat(true)
                            .title("Vous êtes ici")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }
            }
        });
    }
}
