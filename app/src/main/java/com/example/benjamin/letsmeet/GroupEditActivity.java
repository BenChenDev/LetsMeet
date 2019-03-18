package com.example.benjamin.letsmeet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GroupEditActivity extends AppCompatActivity {
    private String useremail = null;
    private int num_members = 0;
    Button save;
    EditText topicTextView, membersEditText;

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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = topicTextView.getText().toString();
                String memberString = membersEditText.getText().toString();
                if(TextUtils.isEmpty(topic)){
                    Toast.makeText(getApplicationContext(), "Topic can not be empty", Toast.LENGTH_LONG).show();
                } else if(!TextUtils.isEmpty(memberString)){

                    String[] members = memberString.split(",[ ]*");
                    num_members = members.length;
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference ref = database.getReference().child("Groups");
                    String recordID = ref.push().getKey();
                    String groupid = recordID;
                    ref.child(recordID).child("GroupID").setValue(recordID);
                    ref.child(recordID).child("Topic").setValue(topic);
                    ref.child(recordID).child("Member").setValue(useremail);

                    for(int i = 0; i < num_members; i++){
                        String tempid = ref.push().getKey();
                        ref.child(tempid).child("GroupID").setValue(groupid);
                        ref.child(tempid).child("Topic").setValue(topic);
                        ref.child(tempid).child("Member").setValue(members[i]);
                    }

                } else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference ref = database.getReference().child("Groups");
                    String recordID = ref.push().getKey();
                    ref.child(recordID).child("GroupID").setValue(recordID);
                    ref.child(recordID).child("Topic").setValue(topic);
                    ref.child(recordID).child("Member").setValue(useremail);
                }
                Toast.makeText(getApplicationContext(), "Study group cretated", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
