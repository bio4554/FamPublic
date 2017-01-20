// TODO Fix buttons here

package com.bio4554.fam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import butterknife.ButterKnife;



public class HomeActivity extends android.support.v4.app.Fragment {


    String PREFS_NAME = "logsets";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_screen, container, false);
        ButterKnife.bind(this, view);
        TextView greetingText = (TextView)view.findViewById(R.id.textViewGreet);
        greetingText.setText("welcome to the Fam open beta, " + FirebaseUtil.getUser().getUsername());
        TextView tLogout = (TextView)view.findViewById(R.id.textViewLogout);
        tLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences logininfo = getActivity().getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = logininfo.edit();
                editor.putString("mEmail", null);
                editor.putString("mPass", null);
                editor.putString("mUser", null);
                editor.putBoolean("mLoggedin", false);
                editor.commit();
                FirebaseAuth.getInstance().signOut();
                Intent launchNext = new Intent(getActivity(), MainActivity.class);
                launchNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(launchNext);
            }
        });
        return view;
    }
}
