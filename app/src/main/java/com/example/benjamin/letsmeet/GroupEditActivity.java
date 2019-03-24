package com.example.benjamin.letsmeet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupEditActivity extends AppCompatActivity {
    private String userName;
    private String useremail;
    private int num_members = 0;
    Button save;
    EditText topicTextView, membersEditText;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userRef;

    List<User> tempUserData = new ArrayList<>();
    String[] members;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        Intent intentFromMain = getIntent();
        if(intentFromMain != null){
            useremail = intentFromMain.getExtras().getString("email");
        }

        save = findViewById(R.id.savebutton);
        topicTextView = findViewById(R.id.topiceditText);
        membersEditText = findViewById(R.id.editText3);

        //get group owner's username
        tempUserData.clear();
        userRef = database.getReference().child("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user : dataSnapshot.getChildren()){
                    User tempUser = new User();
                    String email = user.child("email").getValue(String.class);
                    String name = user.child("name").getValue(String.class);
                    tempUser.setEmail(email);
                    tempUser.setName(name);
                    tempUserData.add(tempUser);
                }

                for(User u : tempUserData){
                    if(u.getEmail().equals(useremail)){
                        userName = u.getName();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String topic = topicTextView.getText().toString();
                String memberString = membersEditText.getText().toString();


                if(TextUtils.isEmpty(topic)){
                    Toast.makeText(getApplicationContext(), "Topic can not be empty", Toast.LENGTH_LONG).show();
                } else if(!TextUtils.isEmpty(memberString)){

                    members = memberString.split(",[ ]*");
                    num_members = members.length;

                    final DatabaseReference ref = database.getReference().child("Groups");

                    //create group owner
                    String recordID = ref.push().getKey();
                    final String groupid = recordID;
                    ref.child(recordID).child("GroupID").setValue(recordID);
                    ref.child(recordID).child("Topic").setValue(topic);
                    ref.child(recordID).child("Group_Owner_id").setValue(useremail);
                    ref.child(recordID).child("Group_Owner_name").setValue(userName);
                    ref.child(recordID).child("Member").setValue(useremail);
                    ref.child(recordID).child("Member_name").setValue(userName);

                    //add members
                    for(int i = 0; i < num_members; i++){
                        for(User u : tempUserData){
                            if(u.getEmail().equals(members[i])){
                                String tempid = ref.push().getKey();
                                ref.child(tempid).child("GroupID").setValue(groupid);
                                ref.child(tempid).child("Topic").setValue(topic);
                                ref.child(tempid).child("Group_Owner_id").setValue(useremail);
                                ref.child(tempid).child("Group_Owner_name").setValue(userName);
                                ref.child(tempid).child("Member").setValue(members[i]);
                                ref.child(tempid).child("Member_name").setValue(u.getName());
                            }
                        }

                    }

                } else {
                    final DatabaseReference ref = database.getReference().child("Groups");

                    String recordID = ref.push().getKey();
                    ref.child(recordID).child("GroupID").setValue(recordID);
                    ref.child(recordID).child("Topic").setValue(topic);
                    ref.child(recordID).child("Group_Owner_id").setValue(useremail);
                    ref.child(recordID).child("Group_Owner_name").setValue(userName);
                    ref.child(recordID).child("Member").setValue(useremail);
                    ref.child(recordID).child("Member_name").setValue(userName);
                }
                Toast.makeText(getApplicationContext(), "Study group cretated", Toast.LENGTH_LONG).show();
                //finish();
            }
        });
    }

}
