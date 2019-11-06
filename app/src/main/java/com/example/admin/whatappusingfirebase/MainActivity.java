package com.example.admin.whatappusingfirebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ViewPager myviewPager;
    private TabLayout mytabLayout;
    private TabsAccessorAdapter mytabsAccessorAdapter;


    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        RootRef = FirebaseDatabase.getInstance().getReference();

        mtoolbar = findViewById(R.id.main_page_toobar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Message Application");

        //set adapter for Fragment and tablayout
        myviewPager = findViewById(R.id.main_tabs_pager);
        mytabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myviewPager.setAdapter(mytabsAccessorAdapter);
        //set adapter for fragment and tablayout

        mytabLayout = findViewById(R.id.main_tabs);
        mytabLayout.setupWithViewPager(myviewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //update status when user online
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {

            updateUserStatus("online");

            VerifyUserExistance();
        }
        //update status when user online
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currendtUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currendtUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())) {
                    //Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                } else {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_option) {

            updateUserStatus("offline");

            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_setting_option) {
            SendUserToSettingsActivity();

        }
        if (item.getItemId() == R.id.main_find_friends_option) {
            SendUserToFindFriendActivity();
        }
        if (item.getItemId() == R.id.main_create_group_option) {
            RequestNewGroup();
        }

        return true;
    }

    //create new group equal dialog
    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Nhập tên nhóm:");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("field");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString().trim();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập tên nhóm", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    //create new group equal dialog

    private void CreateNewGroup(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, groupName + "Tạo nhóm thành công", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToFindFriendActivity() {
        Intent findFriendIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendIntent);
    }

    private void SendUserToSettingsActivity() {
        Intent setttingsIntent = new Intent(MainActivity.this, SetttingsActivity.class);
        startActivity(setttingsIntent);
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }
}
