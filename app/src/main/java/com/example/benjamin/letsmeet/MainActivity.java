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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnGroupClickListerner {

    private List<Group> groups;
    //recyclerview
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    List<Group> tempGroups = new ArrayList<>();
    List<User> tempUsers = new ArrayList<>();
    List<Location> tempLocations = new ArrayList<>();
    String[] group_ids;
    SharedPreferences userpreference;
    private String newToken;
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
//                    DatabaseReference myRef = database.getReference().child("Users");
//                    myRef.child(userid).child("location").setValue(location.getLatitude() + ", " + location.getLongitude());
//                    Log.d("MyLocation: ", "(" + location.getLatitude() + "," + location.getLongitude() + ")");
                    final DatabaseReference updateLocationREF = database.getReference().child("Groups");
                    updateLocationREF.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot group : dataSnapshot.getChildren()){
                                if(group.child("Member").getValue(String.class).equals(useremail)){
                                    String key = group.getKey();
                                    Log.d(TAG, "Key: " + key);
                                    updateLocationREF.child(key).child("Location").setValue(currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            /*todo: check locations of members in the same group, and calculate the location distance,
                      if members are close enough, send everyone notification.
                      so far, just the group owner do this operation
                    */
            Log.d(TAG, "*******New round********");
            DatabaseReference group = database.getReference().child("Groups");
            group.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    tempGroups.clear();
                    for(DataSnapshot group : dataSnapshot.getChildren()){
                        if(group.child("Group_Owner_id").getValue(String.class).equals(useremail)){
                            Group tempGroup = new Group();
                            tempGroup.setGroupID(group.child("GroupID").getValue(String.class));
                            tempGroup.setGroup_Owner_id(group.child("Group_Owner_id").getValue(String.class));
                            tempGroup.setGroup_Owner_name(group.child("Group_Owner_name").getValue(String.class));
                            tempGroup.setMember(group.child("Member").getValue(String.class));
                            tempGroup.setMember_name(group.child("Member_name").getValue(String.class));
                            tempGroup.setTopic(group.child("Topic").getValue(String.class));
                            tempGroup.setLocation(group.child("Location").getValue(String.class));
                            tempGroups.add(tempGroup);
                            Log.d(TAG, "tempGroup added to tempGroupS.");

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            //if current user is not a group owner, the tempGroups.size() ==0
            //get group ids of groups that current user is the group owner
            Log.d(TAG, "tempGroups count:" + tempGroups.size());
            if(tempGroups.size() > 0 && tempGroups.size() < 100){
                tempUsers.clear();
                group_ids = new String[100];//allow user be the owner of maximum 100 groups
                int count = 0;
                String temp_group_id = "";
                for(Group g : tempGroups){
                    String group_id = g.getGroupID();
                    if(!group_id.equals(temp_group_id)){
                        group_ids[count] = group_id;
                        Log.d(TAG, "unique group id: " + group_ids[count]);
                        temp_group_id = group_id;
                        count++;
                    }
                }

                Log.d(TAG, "total unique groups size is: " + count);
                //find all members in the same group
                for(int i = 0; i < count; i++){
                    String group_id = group_ids[i];
                    Log.d(TAG,"*****group: " + group_id);
                    List<MyLocation> locationsInOneGroup = new ArrayList<>();
                    for(Group g : tempGroups){
                        if(g.getGroupID().equals(group_id)){
                            //get location
                            String[] oneLocation = g.getLocation().split(",[ ]*");
                            MyLocation tempLocation = new MyLocation();
                            tempLocation.setLatitude(Double.valueOf(oneLocation[0]));
                            tempLocation.setLongitude(Double.valueOf(oneLocation[1]));
                            locationsInOneGroup.add(tempLocation);

                        }
                    }
                    //having all locations of each members in same group in locationsInOneGroup
                    //arrayList
                    //calculate distance

                    if(locationsInOneGroup.size() > 1){
                        //put locations in object array
                        Log.d(TAG,"^^More than one location. " + locationsInOneGroup.size() + "sets of " +
                                "location data");
                        MyLocation[] locations = new MyLocation[locationsInOneGroup.size()];
                        int locationCount = 0;
                        for(MyLocation L : locationsInOneGroup){
                            locations[locationCount] = L;
                            Log.d(TAG, "index " + locationCount + ": " + "" +
                                    "latitude: " + locations[locationCount].getLatitude() +
                                    "Longtitude: " + locations[locationCount].getLongitude());
                            locationCount++;

                        }
                        Log.d(TAG, "***Before calculate distance: ");
                        //calculate distance
                        boolean close = true;
                        Log.d(TAG, "" + locations.length + " sets of locations that needed to be calculated.");
                        for(int a = 0; a < locations.length; a++){
                            for(int b = a+1; b < locations.length; b++){
                                if(distance(locations[a].getLatitude(),
                                        locations[a].getLongitude(),
                                        locations[b].getLatitude(),
                                        locations[b].getLongitude(),
                                        'M') > 100.0){
                                    close = false;
                                }
                            }
                        }
                        if(close){
                            Log.d(TAG, "Seems everyone close enough.");

//                                            DatabaseReference ref = database.getReference().child("Notifications");
//                                            ref.child(userid).child("Token").setValue(newToken);
                        }else{
                            Log.d(TAG, "one or more members not near by.");
                        }
                    }else{
                        Log.d(TAG,"Only have one location");
                    }
                }
            }
        }
    };

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
                String tempGroupMember = "";
                for(DataSnapshot singleGroup : dataSnapshot.getChildren()){
                    tempGroupMember = singleGroup.child("Member").getValue(String.class);

                    //find all groups that owner is current user
                    if(tempGroupMember != null && tempGroupMember.equals(useremail)){

                        Group group = new Group();
                        String topic = singleGroup.child("Topic").getValue(String.class);

                        group.setTopic(topic);

                        String group_id = singleGroup.child("GroupID").getValue(String.class);

                        String tempMembers = "";

                        for(DataSnapshot singleGroup2 : dataSnapshot.getChildren()){

                            String tempgroupid = singleGroup2.child("GroupID").getValue(String.class);

                            if(tempgroupid.equals(group_id)){

                                String memberName = singleGroup2.child("Member_name").getValue(String.class);
                                tempMembers += memberName + ", ";
                            }
                        }

                        group.setMember(tempMembers);
                        groups.add(group);

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

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                newToken = instanceIdResult.getToken();
                Log.e("newToken", newToken);
                DatabaseReference ref = database.getReference().child("Users");
                ref.child(userid).child("token").setValue(newToken);
            }
        });
    }

    private BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            String title = intent.getStringExtra("Title");
            String body = intent.getStringExtra("Body");
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(body)
                    .setPositiveButton("Let's Meet!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "Ok Clicked", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Nah", new DialogInterface.OnClickListener() {
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

    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'M') {
            dist = dist * 1.609344 * 1000;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
