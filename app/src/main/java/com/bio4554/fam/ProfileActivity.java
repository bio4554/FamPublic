// TODO Finish this up

package com.bio4554.fam;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProfileActivity extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_screen, container, false);
        TextView profileName = (TextView)view.findViewById(R.id.textViewProfileName);
        TextView profileEmail = (TextView)view.findViewById(R.id.textViewProfileEmail);
        profileEmail.setText(FirebaseUtil.getUser().getEmail());
        profileName.setText(FirebaseUtil.getUser().getUsername());
        return view;
    }
}
