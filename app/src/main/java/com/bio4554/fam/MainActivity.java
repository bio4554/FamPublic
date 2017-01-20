//
//  TODO TODO TODO SPLIT UP THIS NASTY ACTIVITY INTO MULTIPLE FILES
//
// TODO Bug: When autocompleting form, user does not exist
// TODO Add profile page to home
// TODO Profile pictures?

package com.bio4554.fam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;
import static com.google.android.gms.internal.zzs.TAG;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    public static final String PREFS_NAME = "logsets";
    private EditText loginMailText;
    private EditText loginPassText;
    private EditText loginNameText;
    private ProgressBar loginProgress;
    public FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private Boolean registering = false;
    private ListView groupListView;
    private TextView groupname;
    private String currentgroup;
    private DatabaseReference ref;
    private GoogleApiClient mGoogleApiClient;
    Boolean usere;
    Boolean createchecked;
    Boolean useraddcheck;
    Boolean addingtogroup = false;
    Boolean changed = true;
    Boolean logginginfromprefs = false;

    User tempuser;
    String checkuname;
    String adduseruname;
    String addusergroup;

    String[] items = {"Test 1", "Test 2"};


    ArrayAdapter<String> itemsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        mAuth = FirebaseAuth.getInstance();
        FirebaseAuth.getInstance().signOut();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");
        ref = database.getReference();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        loginMailText = (EditText)findViewById(R.id.editEmail);
        loginPassText = (EditText)findViewById(R.id.editPassword);
        loginNameText = (EditText)findViewById(R.id.editUsername);
        loginProgress = (ProgressBar) findViewById(R.id.progressBarLogin);
        loginProgress.setVisibility(View.GONE);
        loginProgress.setIndeterminate(true);
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if(registering) {
                        createDbUser(loginNameText.getText().toString(), loginMailText.getText().toString());
                    }
                    ValueEventListener userListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get Post object and use the values to update the UI
                            User user = dataSnapshot.getValue(User.class);
                            if(user != null && user.getUsername() != null && changed) {
                                FirebaseUtil.user.setIngroup(!user.getIngroup());
                                changed = false;
                            }
                            if(user != null && user.getUsername() != null && FirebaseUtil.getUser().getIngroup() != user.getIngroup()) {
                                System.out.println("USER CHANGE");
                                System.out.println("USERNAME: " + user.getUsername());
                                FirebaseUtil.setUser(user);
                                changed = false;
                                System.out.println("CHECKING USER GROUP: " + FirebaseUtil.getUser().getGroup());
                                if (!FirebaseUtil.getUser().getIngroup()) {
                                    System.out.println("NO GROUP, SHOWING HOME SCREEN");
                                    showHome();
                                } else {
                                    System.out.println("FOUND GROUP, SHOWING GROUP PAGE");
                                    showHome();
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


                    DatabaseReference uref = FirebaseDatabase.getInstance().getReference("users").child(loginNameText.getText().toString());
                    uref.addValueEventListener(userListener);



                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String setpassword = settings.getString("mPass", null);
        String setusername = settings.getString("mUser", null);
        String setemail = settings.getString("mEmail", null);
        Boolean setloggedin = settings.getBoolean("mLoggedin", false);
        System.out.println(setpassword + ", " + setemail + ", " + setusername);
        if(setemail != null && setpassword != null && setusername != null && setloggedin) {
            logginginfromprefs = true;
            setContentView(R.layout.splash_screen);
            ProgressBar splashProgress = (ProgressBar)findViewById(R.id.progressBarSplash);
            splashProgress.setIndeterminate(true);
            loginNameText.setText(setusername);
            loginUser(setemail, setpassword);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        finish();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
    }

    public void buttonRegister(View view) {
        System.out.println("Register clicked");
        if(!loginNameText.getText().toString().isEmpty() && !loginMailText.getText().toString().isEmpty() && !loginPassText.getText().toString().isEmpty()) {
            createUser(loginNameText.getText().toString());
        } else {
            System.out.println("ERROR: ONE OR MORE FIELDS ARE BLANK");
            showToast("All fields must be filled in");
        }
    }

    public void buttonBackToHome(View view) {
        showHome();
    }

    public void buttonLogin(View view) {
        System.out.println("Login clicked");
        if(!loginNameText.getText().toString().isEmpty() && !loginMailText.getText().toString().isEmpty() && !loginPassText.getText().toString().isEmpty()) {
            loginUser(loginMailText.getText().toString(), loginPassText.getText().toString());
        } else {
            System.out.println(); // TODO Error here, says fields blank when they aren't
            System.out.println("ERROR: ONE OR MORE FIELDS ARE BLANK");
            showToast("All fields must be filled in");
        }
    }

    public void buttonLogout(View view) {
        SharedPreferences logininfo = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = logininfo.edit();
        editor.putString("mEmail", null);
        editor.putString("mPass", null);
        editor.putString("mUser", null);
        editor.putBoolean("mLoggedin", false);
        editor.commit();
        mAuth.signOut();
        changed = true;
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    public void buttonShowHome(View view) {
        showHome();
    }

    public void buttonShowGroup(View view) {
        showGroup();
    }

    public void buttonShowProfile(View view) {
        showProfile();
    }

    public void loginScreenUser() {
        if(FirebaseUtil.getUser().getIngroup()) {
            showGroup();
        } else {
            showHome();
        }
    }

    /* public void showHome() {
        Intent intent = new Intent(this, testAct.class);
        startActivity(intent);
    } */

    public void showHome() {
        System.out.println("CALLING showHome()");
        Intent intent = new Intent(this, ToolbarActivity.class);
        finish();
        startActivity(intent);
        /* if(!FirebaseUtil.getUser().getIngroup()) {
            setContentView(R.layout.home_screen);
            TextView test = (TextView) findViewById(R.id.textViewGreet);
            test.setText("welcome to the Fam open beta test, " + FirebaseUtil.getUser().getUsername());
        } else {
            setContentView(R.layout.home_screen_infam);
            TextView test = (TextView) findViewById(R.id.textViewGreetInfam);
            test.setText("welcome to the Fam open beta test, " + FirebaseUtil.getUser().getUsername());
        } */
    }

    public void showGroup() {
        System.out.println("CALLING showGroup()");
        DatabaseReference uref;
        DatabaseReference gref;
        FirebaseListAdapter mAdapter;
        setContentView(R.layout.group_screen);
        groupListView = (ListView)findViewById(R.id.groupScreenListView);
        ref = FirebaseDatabase.getInstance().getReference();
        uref = FirebaseDatabase.getInstance().getReference("groups").child(FirebaseUtil.getUser().getGroup());

        ValueEventListener groupListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Group group = dataSnapshot.getValue(Group.class);
                if (group != null) {
                    System.out.println("GROUP: " + group.getName());
                    FirebaseUtil.setGroup(group);
                } else {
                    FirebaseUtil.setGroup(null);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };

        uref.addValueEventListener(groupListener);
        gref = FirebaseDatabase.getInstance().getReference("groups").child(FirebaseUtil.getUser().getGroup()).child("posts");

        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        mAdapter = new FirebaseListAdapter<Post>(MainActivity.this, Post.class, R.layout.item_post, gref) {
            @Override
            protected void populateView(View view, Post chatMessage, int position) {
                System.out.println("POST: " + chatMessage.getName() + "\nMESSAGE: " + chatMessage.getMessage());
                ((TextView)view.findViewById(R.id.textViewName)).setText(chatMessage.getName());
                ((TextView)view.findViewById(R.id.textViewMessage)).setText(chatMessage.getMessage());
            }
        };

        groupListView.setAdapter(mAdapter);
    }

    private void showProfile() {
        setContentView(R.layout.profile_screen);
        TextView profileUname = (TextView)findViewById(R.id.textViewProfileName);
        TextView profileEmail = (TextView)findViewById(R.id.textViewProfileEmail);
        ImageView profileImage = (ImageView)findViewById(R.id.imageViewProfileImage);
        profileEmail.setText(FirebaseUtil.getUser().getEmail());
        profileUname.setText(FirebaseUtil.getUser().getUsername());
    }

    /* public void buttonCreateGroup(View view) {
        EditText ngroupname = (EditText)findViewById(R.id.editFamName);
        if(!ngroupname.getText().toString().isEmpty()) {
            System.out.println("MADE GROUP " + ngroupname.getText().toString());
            createGroup(ngroupname.getText().toString(), FirebaseUtil.getUser().getUsername());
            Toast.makeText(MainActivity.this, "Made group " + ngroupname.getText().toString(),
                    Toast.LENGTH_SHORT).show();
            showGroup();
        } else {
            Toast.makeText(MainActivity.this, "Group name is blank!",
                    Toast.LENGTH_SHORT).show();
        }
    } */

    public void buttonSend(View view) {
        EditText mMessage = (EditText)findViewById(R.id.editUpdateMessage);
        if(FirebaseUtil.getUser().getUsername() != null) {
            System.out.println("ATTEMPTING TO POST " + mMessage.getText().toString().length() + " CHARACTERS");
            if(mMessage.getText().toString().length() == 0) {
                System.out.println("ERROR: POST EMPTY");
                showToast("Message empty");
            } else if(mMessage.getText().toString().length() <= 180) {
                System.out.println("POST SUCCESS");
                createPost(FirebaseUtil.getUser().getGroup(), FirebaseUtil.getUser().getUsername(), mMessage.getText().toString(), mAuth.getCurrentUser().getUid());
                mMessage.setText("");
                mMessage.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mMessage, InputMethodManager.SHOW_IMPLICIT);
            } else {
                System.out.println("ERROR: POST TOO LONG");
                Toast.makeText(MainActivity.this, "You have " + (mMessage.getText().toString().length() - 180) + " too many characters (Maximum is 180)",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "ERROR: NO USER LOGGED IN!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void buttonLeaveGroup(View view) {
        //Check if user is creator of group. If they are, delete the group
        if(FirebaseUtil.getGroup().getMembers() <= 1) {
            FirebaseUtil.user.setIngroup(false);
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseUtil.getUser().getUsername()).setValue(FirebaseUtil.getUser());
            FirebaseDatabase.getInstance().getReference("groups").child(FirebaseUtil.getUser().getGroup()).removeValue();
            Toast.makeText(MainActivity.this, "Last member left the fam, deleting",
                    Toast.LENGTH_SHORT).show();
            showHome();
        } else { //Don't delete
            FirebaseUtil.user.setIngroup(false);
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseUtil.getUser().getUsername()).setValue(FirebaseUtil.getUser());
            FirebaseDatabase.getInstance().getReference("groups").child(FirebaseUtil.getUser().getGroup()).child("posts").child(FirebaseUtil.getUser().getUsername()).removeValue();
            onLeftMember(FirebaseDatabase.getInstance().getReference("groups").child(FirebaseUtil.getUser().getGroup()).child("members"));
            Toast.makeText(MainActivity.this, "Left fam",
                    Toast.LENGTH_SHORT).show();
            showHome();
        }
    }

    public void buttonAddUser(View view) {
        setContentView(R.layout.add_user_screen);
    }

    public void buttonConfirmAddUser(View view) {
        EditText mEditText = (EditText)findViewById(R.id.editAddUser);
        addUserToGroupCheck(mEditText.getText().toString(), FirebaseUtil.getUser().getGroup());
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
                    if(user.getIngroup() != true && useraddcheck) {
                        System.out.println("USER EXISTS FOR ADD USER CHECK AND IS NOT IN GROUP: " + adduseruname);
                        addUserToGroup(adduseruname, addusergroup);
                        useraddcheck = false;
                    } else if(useraddcheck){
                        useraddcheck = false;
                        showToast("That user is already in a group");
                    }
                } else if(!dataSnapshot.exists()){
                    System.out.println("CANNOT GET USER FOR ADD USER " + adduseruname);
                    Toast.makeText(MainActivity.this, "That user does not exist!",
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
        Toast.makeText(MainActivity.this, "Added " + uname + " to your fam",
                Toast.LENGTH_SHORT).show();
        showGroup();
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

    private void onLeftMember(DatabaseReference groupRef) {
        groupRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                int g = mutableData.getValue(Integer.class);
                if (g == 0) {
                    return Transaction.success(mutableData);
                }

                g = g-1;
                System.out.println("Added member -1");

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

    private void createPost(String groupid, String uname, String umessage, String muid) {
        Post newpost = new Post(muid, umessage, uname);
        FirebaseDatabase.getInstance().getReference("groups").child(groupid).child("posts").child(uname).setValue(newpost);
        Toast.makeText(MainActivity.this, "Updated your status",
                Toast.LENGTH_SHORT).show();
        showGroup();
    }


    private void createUser(String uname) {
        createchecked = false;
        loginProgress.setVisibility(View.VISIBLE);
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user object and use the values to update the return object
                if(dataSnapshot.exists() && !createchecked) {
                    System.out.println("SUCCESSFULLY GOT USER " + checkuname);
                    usere = true;
                    System.out.println(loginNameText.getText().toString() + " ALREADY EXISTS IN DATABASE");
                    loginProgress.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Username already exists!",
                            Toast.LENGTH_SHORT).show();
                } else if(!createchecked){
                    System.out.println("ERROR GETTING USER " + checkuname + ", VALUE IS NULL");
                    createchecked = true;
                    usere = false;
                    fbCreateUser(loginMailText.getText().toString(), loginPassText.getText().toString(), loginNameText.getText().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
            }
        };
        DatabaseReference usercheck = FirebaseDatabase.getInstance().getReference("users").child(uname);
        if(usercheck != null) {
            usercheck.addValueEventListener(userListener);
            System.out.println("USERCHECK IS NOT NULL");
        } else {
            System.out.println("USERCHECK IS NULL");
        }
    }

    private void fbCreateUser(String email, String password, String uname) {
        registering = true;
        System.out.println("REGISTERING USER " + uname);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            registering = false;
                            Toast.makeText(MainActivity.this, "User with that E-Mail already exists!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createDbUser(String username, String email) {
        User user = new User(username, email, "0", false);

        FirebaseDatabase.getInstance().getReference("users").child(username).setValue(user);
        SharedPreferences logininfo = getSharedPreferences(PREFS_NAME, 0);
        if(logininfo.getString("mEmail", null) == null) {
            SharedPreferences.Editor editor = logininfo.edit();
            editor.putString("mEmail", loginMailText.getText().toString());
            editor.putString("mPass", loginPassText.getText().toString());
            editor.putString("mUser", loginNameText.getText().toString());
            editor.putBoolean("mLoggedin", true);
            editor.commit();
        }
    }

    public void createGroup(String name, String creatorid) {
        DatabaseReference groupRef = database.getReference("groups");
        DatabaseReference uref = database.getReference("users");
        DatabaseReference turef = database.getReference("users").child(creatorid);
        String key = groupRef.push().getKey();


        Group newgroup = new Group(name, creatorid, 1);
        Post firstpost = new Post(creatorid,"Hello World!", name);
        FirebaseUtil.user.setGroup(key);
        FirebaseUtil.user.setIngroup(true);
        uref.child(creatorid).child("group").setValue(key);
        uref.child(creatorid).child("ingroup").setValue(true);
        groupRef.child(key).setValue(newgroup);
        groupRef.child(key).child("posts").push().setValue(firstpost);
        currentgroup = key;
        showGroup();
    }

    private void loginUser(String username, String password) {
        loginProgress.setVisibility(View.VISIBLE);
        loginProgress.setIndeterminate(true);
        System.out.println("LOGIN ATTEMPT: " + username);
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        loginProgress.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                                Toast.makeText(MainActivity.this, "Connection error, or user does not exist",
                                        Toast.LENGTH_SHORT).show();
                                loginProgress.setVisibility(View.GONE);
                                SharedPreferences logininfo = getSharedPreferences(PREFS_NAME, 0);
                                SharedPreferences.Editor editor = logininfo.edit();
                                editor.putString("mEmail", null);
                                editor.putString("mPass", null);
                                editor.putString("mUser", null);
                                editor.putBoolean("mLoggedin", false);
                                editor.commit();
                            if(logginginfromprefs) {
                                Intent launchNext = new Intent(getApplicationContext(), MainActivity.class);
                                launchNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(launchNext);
                            }
                        }

                        // ...
                    }
                });
        SharedPreferences logininfo = getSharedPreferences(PREFS_NAME, 0);
        if(logininfo.getString("mEmail", null) == null) {
            SharedPreferences.Editor editor = logininfo.edit();
            editor.putString("mEmail", loginMailText.getText().toString());
            editor.putString("mPass", loginPassText.getText().toString());
            editor.putString("mUser", loginNameText.getText().toString());
            editor.putBoolean("mLoggedin", true);
            editor.commit();
        }
    }

    public void showToast(String t) {
        Toast.makeText(MainActivity.this, t,
                Toast.LENGTH_SHORT).show();
    }
}
