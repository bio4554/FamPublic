// TODO Finish this up

package com.bio4554.fam;


import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;


import static com.google.android.gms.internal.zzs.TAG;

public class FamActivity extends Fragment {

    private EditText newFamName;
    private TextView newFamCreate;
    private RecyclerView groupListView;
    private FloatingActionButton leaveFab;
    private FloatingActionButton addFab;
    private EditText mMessage;
    private TextView updateTextView;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    private LinearLayoutManager mManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if(!FirebaseUtil.getUser().getIngroup()) {
            view = inflater.inflate(R.layout.new_fam_layout, container, false);
            TextView uNameFam = (TextView)view.findViewById(R.id.textViewCreateFamUserName);
            uNameFam.setText(FirebaseUtil.getUser().getUsername());
            newFamName = (EditText)view.findViewById(R.id.editTextNewFamName);
            newFamCreate = (TextView)view.findViewById(R.id.textViewCreateNewFam);
            newFamCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newfamname = newFamName.getText().toString();
                    if(!newfamname.isEmpty()) {
                        createGroup(newfamname, FirebaseUtil.getUser().getUsername());
                    } else {
                        Toast.makeText(getActivity(), "Group name is blank!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            view = inflater.inflate(R.layout.group_screen, container, false);
            groupListView = (RecyclerView) view.findViewById(R.id.groupScreenListView);
            groupListView.setHasFixedSize(false);
            mManager = new LinearLayoutManager(getActivity());
            groupListView.setLayoutManager(mManager);
            groupListView.setScrollContainer(true);
            groupListView.setItemAnimator(null);
            //groupListView.getItemAnimator().setChangeDuration(0);
            groupListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if ( bottom < oldBottom) {
                        groupListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                groupListView.smoothScrollToPosition(groupListView.getChildCount());
                            }
                        }, 100);
                    }
                }
            });
            mMessage = (EditText)view.findViewById(R.id.editUpdateMessage);
            mMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        sendMessage();
                        handled = true;
                    }
                    return handled;
                }
            });
            leaveFab = (FloatingActionButton)view.findViewById(R.id.floatingActionButtonLeaveGroup);
            leaveFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leaveFam();
                }
            });
            addFab = (FloatingActionButton)view.findViewById(R.id.floatingActionButtonAddMember);
            addFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ToolbarActivity)getActivity()).addNewMember();
                }
            });
            updateTextView = (TextView)view.findViewById(R.id.textViewUpdateSend);
            updateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });
            showGroup();
        }
        return view;
    }

    private void sendMessage() {
        if(FirebaseUtil.getUser().getUsername() != null) {
            System.out.println("ATTEMPTING TO POST " + mMessage.getText().toString().length() + " CHARACTERS");
            if(mMessage.getText().toString().length() == 0) {
                System.out.println("ERROR: POST EMPTY");
                Toast.makeText(getActivity(), "Message empty",
                        Toast.LENGTH_SHORT).show();
            } else if(mMessage.getText().toString().length() <= 180) {
                System.out.println("POST SUCCESS");
                createPost(FirebaseUtil.getUser().getGroup(), FirebaseUtil.getUser().getUsername(), mMessage.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                mMessage.setText("");
                mMessage.requestFocus();
            } else {
                System.out.println("ERROR: POST TOO LONG");
                Toast.makeText(getActivity(), "You have " + (mMessage.getText().toString().length() - 180) + " too many characters (Maximum is 180)",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "ERROR: NO USER LOGGED IN!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showGroup() {
        System.out.println("CALLING showGroup()");
        DatabaseReference uref;
        DatabaseReference gref;
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

        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<Post, ChatHolder>(
                Post.class, R.layout.item_post, ChatHolder.class, gref) {

            @Override
            public void populateViewHolder(ChatHolder chatView, Post chat, int position) {
                chatView.setName(chat.getName());
                chatView.setMessage(chat.getMessage());
            }
        };

        /*mAdapter = new FirebaseRecyclerAdapter<Post>(getActivity(), Post.class, R.layout.item_post, gref) {
            @Override

            @Override
            protected void populateView(View view, Post chatMessage, int position) {
                System.out.println("POST: " + chatMessage.getName() + "\nMESSAGE: " + chatMessage.getMessage());
                ((TextView)view.findViewById(R.id.textViewName)).setText(chatMessage.getName());
                ((TextView)view.findViewById(R.id.textViewMessage)).setText(chatMessage.getMessage());
            }
        };*/

        groupListView.setAdapter(mRecyclerViewAdapter);
    }

    private void createGroup(String name, String creatorid) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups");
        DatabaseReference uref = FirebaseDatabase.getInstance().getReference("users");
        String key = groupRef.push().getKey();
        Group newgroup = new Group(name, creatorid, 1);
        Post firstpost = new Post(creatorid,"Welcome to your new fam!", name);
        FirebaseUtil.user.setGroup(key);
        FirebaseUtil.user.setIngroup(true);
        uref.child(creatorid).child("group").setValue(key);
        uref.child(creatorid).child("ingroup").setValue(true);
        groupRef.child(key).setValue(newgroup);
        groupRef.child(key).child("posts").push().setValue(firstpost);
        ((ToolbarActivity)getActivity()).restartLeftFam();
    }

    private void leaveFam() {
        if(FirebaseUtil.getGroup().getMembers() <= 1) {
            FirebaseUtil.user.setIngroup(false);
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseUtil.getUser().getUsername()).setValue(FirebaseUtil.getUser());
            FirebaseDatabase.getInstance().getReference("groups").child(FirebaseUtil.getUser().getGroup()).removeValue();
            Toast.makeText(getActivity(), "Last member left the fam, deleting",
                    Toast.LENGTH_SHORT).show();
            ((ToolbarActivity)getActivity()).restartLeftFam();
        } else { //Don't delete
            FirebaseUtil.user.setIngroup(false);
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseUtil.getUser().getUsername()).setValue(FirebaseUtil.getUser());
            FirebaseDatabase.getInstance().getReference("groups").child(FirebaseUtil.getUser().getGroup()).child("posts").child(FirebaseUtil.getUser().getUsername()).removeValue();
            onLeftMember(FirebaseDatabase.getInstance().getReference("groups").child(FirebaseUtil.getUser().getGroup()).child("members"));
            Toast.makeText(getActivity(), "Left fam",
                    Toast.LENGTH_SHORT).show();
            ((ToolbarActivity)getActivity()).restartLeftFam();
        }
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
        mRecyclerViewAdapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), "Updated your status",
                Toast.LENGTH_SHORT).show();

        showGroup();
    }
}