package com.bio4554.fam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by bio4554 on 6/19/2016.
 */

public class AddMemberFragment extends Activity{
    private String adduseruname;
    private String addusergroup;
    private Boolean useraddcheck;
    private TextView addUserConfirm;
    private EditText addUserEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_screen);
        TextView addUserExit = (TextView)findViewById(R.id.textViewBackAddUser);
        addUserExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchNext = new Intent(getApplicationContext(), ToolbarActivity.class);
                launchNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(launchNext);
            }
        });
        addUserEditText = (EditText)findViewById(R.id.editAddUser);
        addUserConfirm = (TextView)findViewById(R.id.textViewAddUserConfirm);
        addUserConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ucheck = addUserEditText.getText().toString();
                if(!ucheck.isEmpty()) {
                    addUserToGroupCheck(ucheck, FirebaseUtil.getUser().getGroup());
                } else {
                    Toast.makeText(getApplicationContext(), "You must enter a name!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addUserToGroupCheck(String uname, String group) {
        adduseruname = uname;
        addusergroup = group;
        useraddcheck = true;
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user object and use the values to update the return object
                if(dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    System.out.println("SUCCESSFULLY GOT USER " + adduseruname);
                    System.out.println("USER ADD CHECK: " + useraddcheck);
                    if(!user.getIngroup() && useraddcheck) {
                        System.out.println("USER EXISTS FOR ADD USER CHECK AND IS NOT IN GROUP: " + adduseruname);
                        addUserToGroup(adduseruname, addusergroup);
                        useraddcheck = false;
                    } else if(useraddcheck && user.getIngroup()) {
                        useraddcheck = false;
                        Toast.makeText(AddMemberFragment.this, "That user is already in a group!",
                                Toast.LENGTH_SHORT).show();
                    } else if(useraddcheck){
                        useraddcheck = false;
                        Toast.makeText(AddMemberFragment.this, "That user does not exist!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if(!dataSnapshot.exists()){
                    System.out.println("CANNOT GET USER FOR ADD USER " + adduseruname);
                    Toast.makeText(AddMemberFragment.this, "That user does not exist!",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
            }
        };
        DatabaseReference tref = FirebaseDatabase.getInstance().getReference("users").child(uname);
        tref.addValueEventListener(userListener);
    }

    private void addUserToGroup(String uname, String group) {
        FirebaseDatabase.getInstance().getReference("users").child(uname).child("group").setValue(group);
        FirebaseDatabase.getInstance().getReference("users").child(uname).child("ingroup").setValue(true);
        DatabaseReference tempgroup = FirebaseDatabase.getInstance().getReference("groups").child(group);
        onNewMember(tempgroup.child("members"));
        System.out.println("ADDED USER " + uname + " TO GROUP " + group);
        Toast.makeText(AddMemberFragment.this, "Added " + uname + " to your fam",
                Toast.LENGTH_SHORT).show();
        Intent launchNext = new Intent(getApplicationContext(), ToolbarActivity.class);
        launchNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(launchNext);
    }

    private void onNewMember(DatabaseReference groupRef) {
        groupRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                int g = mutableData.getValue(Integer.class);
                if (g == 0) {
                    return Transaction.success(mutableData);
                }

                g = g+1;
                System.out.println("Added member +1");

                // Set value and report transaction success
                mutableData.setValue(g);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "groupMemberTransaction:onComplete:" + databaseError);
            }
        });
    }
}
