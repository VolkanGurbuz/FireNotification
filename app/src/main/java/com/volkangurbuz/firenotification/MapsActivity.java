package com.volkangurbuz.firenotification;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onesignal.OneSignal;
import com.volkangurbuz.firenotification.Model.Location;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ArrayList<Location> allLocations;
    private VideoView videoView;
    private  String videoName;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        videoView = findViewById(R.id.videoView);
        videoView.setVisibility(View.INVISIBLE);



        allLocations = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("locations");
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
       /* usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(deviceToken);


        usersRef.child("device_token").setValue(deviceToken);*/

        loadMaps();

      /*  mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                  //  downloadUri(videoName);
                    Toast.makeText(MapsActivity.this, "marker"+ marker.getId(), Toast.LENGTH_SHORT).show();



                return false;
            }
        });*/

    }

    @Override
    protected void onStart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
        super.onStart();
    }

    //by this sing in anonymously method you can send the video to the database.

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(MapsActivity.this, "success", Toast.LENGTH_LONG).show();
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(MapsActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    //download the video on the database.
    public void downloadUri(String urlName) throws IOException {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://uberme-abb8d.appspot.com/");
        StorageReference islandRef = storageRef.child("videos/" + urlName);


        File folder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/videos/");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        //final File localFile = new File(folder,urlName);
        //localFile.createNewFile();


        islandRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                MediaController mc = new MediaController(MapsActivity.this);
                videoView.start();
                videoView.setVisibility(View.VISIBLE);
                videoView.setMediaController(mc);
                videoView.setVideoURI(uri);
                videoView.requestFocus();


            }
        });


        /*
        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                Log.e("firebase ",";local tem file created  created " +localFile.toString());
                Toast.makeText(MapsActivity.this, "created success", Toast.LENGTH_SHORT).show();
                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
                Toast.makeText(MapsActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });*/

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String videoNamePos= playVideoOfPosition(marker.getPosition()+"");
                try {
                    downloadUri(videoNamePos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

    }


    //get the all maps location from the database to see all
    public void loadMaps() {

        //bu ref ile gerekl;i olan degisken degerl;erinin her birini alyioruz ve bunlari gbir listede kaydedip gonderoyuirz
        //modele kaydediyoruz
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    final String lat = ds.child("lat").getValue(String.class);
                    String lon = ds.child("long").getValue(String.class);
                    String video = ds.child("videoname").getValue(String.class);
                    Location l = new Location(video, lat, lon);
                //lokasyon model e alinan her bir degeri kaydetti k ve bunu lsiteye aktariyoruz
                    allLocations.add(l);
                    Log.d("size", allLocations.size()+" " );

                    for (int i = 0; i < allLocations.size(); i++) {
                        double latLoc = Double.parseDouble(allLocations.get(i).getLatitude());
                        double longLoc = Double.parseDouble(allLocations.get(i).getLongitude());

                        videoName = allLocations.get(i).getVideoID();

                        LatLng sydney = new LatLng(latLoc, longLoc);

                        int height = 100;
                        int width = 100;
                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.fire);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        Marker marker = mMap.addMarker(new MarkerOptions().
                                icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).position(sydney).title("Video Downloading"));


                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLoc, longLoc), 15.0f));
                        //  mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //show the video of the position
    public String playVideoOfPosition(String loc){
        String videoName = "";
        for (int i = 0 ; i < allLocations.size();i++){
            String locMap = "lat/lng: "+ "("+allLocations.get(i).getLatitude()+","+ allLocations.get(i).getLongitude()+")";

          if (TextUtils.equals(loc, locMap)){

              videoName = allLocations.get(i).getVideoID();

          }

        }

        return videoName;

    }



}
