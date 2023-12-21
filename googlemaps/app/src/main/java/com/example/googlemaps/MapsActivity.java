package com.example.googlemaps;

import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.googlemaps.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Menambahkan kontrol zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Mengatur lokasi awal pada peta
        LatLng initialLocation = new LatLng(-7.2819705, 112.795323);
        mMap.addMarker(new MarkerOptions().position(initialLocation).title("Marker in Air Uni"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation));

        // Menambahkan listener untuk mengganti posisi saat marker di-klik
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Menghapus marker yang sudah ada (jika ada)
                mMap.clear();

                // Menambahkan marker pada posisi pengguna
                Marker userMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));

                // Menambahkan marker pada posisi tujuan
                LatLng destination = new LatLng(-7.12345, 112.54321); // Ganti dengan koordinat tujuan
                Marker destinationMarker = mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));

                // Menampilkan rute antara pengguna dan tujuan
                String url = getDirectionsUrl(latLng, destination);
                new FetchDirectionsTask().execute(url);
            }
        });
    }

    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=YOUR_API_KEY";
    }

    private class FetchDirectionsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            // Ambil data rute dari Directions API
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url[0]).openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                StringBuilder result = new StringBuilder();
                while (data != -1) {
                    char current = (char) data;
                    result.append(current);
                    data = reader.read();
                }
                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Parsing data rute dan tampilkan pada peta
            // Implementasikan parsing data JSON di sini
            // ...

            // Contoh: Tampilkan rute pada peta
            // drawRouteOnMap(result);
        }
    }
}
