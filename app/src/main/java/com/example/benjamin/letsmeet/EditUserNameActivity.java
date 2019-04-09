package com.example.benjamin.letsmeet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EditUserNameActivity extends AppCompatActivity {

    final String TAG = "inEditUserName";

    EditText userNameEditText;
    Button saveButton;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference userRef = database.getReference().child("Users");
    final DatabaseReference groupRef = database.getReference().child("Groups");
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    String userEmail;
    String newName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_name);

        userNameEditText = findViewById(R.id.userNameEditText);


        Intent intentFromMain = getIntent();
        if(intentFromMain != null){
            Log.d(TAG, "intentFrom Main not null");
            String userName = intentFromMain.getStringExtra("userName");
            Log.d(TAG, "userName from main is: " + userName);
            userNameEditText.setText(userName);

        }else{
            Log.d(TAG, "intent from main is null.");
        }


        saveButton = findViewById(R.id.saveChangeNameButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValid()){
                    newName = userNameEditText.getText().toString();
                    FirebaseUser user = mAuth.getCurrentUser();
                    userEmail = user.getEmail();
                    String userID = user.getUid();
                    Log.i(TAG, "user email: " + userEmail + "\n user id: " + userID);
                    userRef.child(userID).child("name").setValue(newName);
                    groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                if(data.child("Member").getValue(String.class).equals(userEmail)){
                                    String group = data.getKey();
                                    groupRef.child(group).child("Member_name").setValue(newName);
                                }
                            }
                            Toast.makeText(getApplicationContext(), "User name updated!", Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            }
        });
    }

    private boolean checkValid(){
        if(userNameEditText.getText().toString().isEmpty()){
            Log.d(TAG, "Empty user name field.");
            Toast.makeText(EditUserNameActivity.this, "Please enter a user name.", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }
}
