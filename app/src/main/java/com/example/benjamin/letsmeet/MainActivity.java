package com.example.benjamin.letsmeet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnGroupClickListerner {
    private final int Location_PERMISSION_CODE =1;
    private final String TAG = "inMain";
    private FirebaseDatabase database;
    private String userid, useremail, username;
    private Location currentLocation = null;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (location != null) {
                    currentLocation = location;
                    database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference().child("Users");
                    myRef.child(userid).child("location").setValue(location.getLatitude(),location.getLongitude());
                    Log.d("MyLocation: ", "(" + location.getLatitude() + "," + location.getLongitude() + ")");
                    /*todo: check locations of members in the same group, and calculate the location distance,
                      if members are close enough, send everyone notification.
                      so far, just the group owner do this operation
                    */
                    tempGroups.clear();
                    DatabaseReference group = database.getReference().child("Groups");
                    group.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot group : dataSnapshot.getChildren()){
                                if(group.child("Group_Owner_id").getValue(String.class).equals(useremail)){
                                    Group tempGroup = new Group();
                                    tempGroup.setGroupID(group.child("GroupID").getValue(String.class));
                                    tempGroup.setGroup_Owner_id(group.child("Group_Owner_id").getValue(String.class));
                                    tempGroup.setGroup_Owner_name(group.child("Group_Owner_name").getValue(String.class));
                                    tempGroup.setMember(group.child("Member").getValue(String.class));
                                    tempGroup.setMember_name(group.child("Member_name").getValue(String.class));
                                    tempGroup.setTopic(group.child("Topic").getValue(String.class));
                                    tempGroups.add(tempGroup);
                                }

                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    sendNotificationToUser(useremail, "Hi there puf!");
                }
            }
        }
    };


    private List<Group> groups;
    //recyclerview
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    List<Group> tempGroups = new ArrayList<>();

    SharedPreferences userpreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        groups = new ArrayList<>();

        //share preference
        userpreference = getSharedPreferences("userpreference", Context.MODE_PRIVATE);

       FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GroupEditActivity.class);
                intent.putExtra("email", useremail);
                startActivity(intent);
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();


        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();

        Intent intentfromlogin = getIntent();
        if(intentfromlogin != null) {
            userid = intentfromlogin.getExtras().getString("userid");
            useremail = intentfromlogin.getExtras().getString("useremail");


            final DatabaseReference myRef = database.getReference().child("Users");

            myRef.child(userid).child("email").setValue(useremail);
            Query query = myRef.child(userid).child("name");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        myRef.child(userid).child("name").setValue("Default user name");
                    } else {
                        username = dataSnapshot.getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            SharedPreferences.Editor editor = userpreference.edit();
            editor.putString("currentuserid", userid);
            editor.putString("currentuseremail", useremail);
            editor.putString("currentusername", username);
            editor.commit();

        }


        final DatabaseReference groupRef = database.getReference().child("Groups");

        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();
                String tempmembers = "";
                for(DataSnapshot taskSnapshot : dataSnapshot.getChildren()){
                    final String tempMember = taskSnapshot.child("Member").getValue(String.class);

                    if(tempMember.equals(useremail)){

                        final Group group = new Group();
                        String topic = taskSnapshot.child("Topic").getValue(String.class);

                        group.setTopic(topic);

                        String group_id = taskSnapshot.child("GroupID").getValue(String.class);

                        for(DataSnapshot taskSnapshot1 : dataSnapshot.getChildren()){

                            String tempgroupid = taskSnapshot1.child("GroupID").getValue(String.class);

                            if(tempgroupid.equals(group_id)){

                                String memberName = taskSnapshot1.child("Member_name").getValue(String.class);
                                if(!memberName.equals(username)){
                                    tempmembers += memberName + ", ";

                                }
                            }
                        }

                            group.setMember(tempmembers);
                            groups.add(group);
                            tempmembers = "";


                    }

                }


                    recyclerView = findViewById(R.id.recyclerView);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this) );
                    displayRecordSet();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(listener, new IntentFilter("notification"));
    }

    private BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            String title = intent.getStringExtra("Title");
            String body = intent.getStringExtra("Body");
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(body)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "Ok Clicked", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        }
    };

    private void displayRecordSet() {
        adapter = new MyAdapter(groups, this, this);
//        checkedIds = ((MyAdapter) adapter).checkedItems;
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            startLocationUpdate();
        }else{
            request_permission();
        }

        String id = userpreference.getString("currentuserid", null);
        String email = userpreference.getString("currentuseremail", null);
        String name = userpreference.getString("currentusername", null);

        if(id != null){
            userid = id;
        }

        if(email != null){
            useremail = email;
        }

        if(name != null){
            username = name;
        }
    }


    private boolean isGroupOwner(String email){
        boolean isOwner = false;


        return isOwner;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(50000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void request_permission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Location_PERMISSION_CODE);

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Location permission is needed")
                    .setMessage("To run this app, the location access is needed")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Location_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Location_PERMISSION_CODE);
        }

    }

    @Override
    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Location_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                startLocationUpdate();

            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClick(Group group) {

    }

    @Override
    public void onItemLongClick(Group group) {

    }

    public static void sendNotificationToUser(String user, final String message) {
        //Firebase ref = new Firebase(FIREBASE_URL);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notifications = database.getReference().child("notificationRequests");
        //final Firebase notifications = ref.child("notificationRequests");

        Map notification = new HashMap<>();
        notification.put("username", user);
        notification.put("message", message);

        notifications.push().setValue(notification);
    }
}
