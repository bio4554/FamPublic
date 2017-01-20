package com.bio4554.fam;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by bio4554 on 6/16/2016.
 */

public class testAct extends Activity{
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        TextView testtext = (TextView)findViewById(R.id.textViewTestLogin);
        testtext.setText(mAuth.getCurrentUser().getEmail());
    }
}
