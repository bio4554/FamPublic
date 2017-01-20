package com.bio4554.fam;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by bio4554 on 6/17/2016.
 */

public class FirebaseUtil {

    public static User user = new User("","","",false);
    public static Group group;

    public static FirebaseUser getFbUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            return user;
        }
        return null;
    }

    public static void init(@NonNull String uname) {
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User u = dataSnapshot.getValue(User.class);
                if(u != null && u.getUsername() != null) {
                    System.out.println("GOT USER");
                    if(user != u) {
                        user = u;
                    }
                } else {
                    System.out.println("ERROR: USER IS NULL!!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadUsername:dbError", databaseError.toException());
                // ...
            }
        };
        DatabaseReference uref = FirebaseDatabase.getInstance().getReference("users").child(uname);
        if(uref != null) {
            uref.addValueEventListener(userListener);
            System.out.println("SET LISTENER FOR USER " + uname);
        }
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User u) {
        user = u;
    }

    public static void setGroup(Group g) {
        group = g;
    }

    public static Group getGroup() {
        return group;
    }


}