package com.zybooks.universityconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MessageActivity extends AppCompatActivity {

    public static final String EXTRA_SIGNING_OUT = "com.example.EXTRA_SIGNING_OUT";
//    public static final String EXTRA_USERNAME = "com.example.EXTRA_USERNAME";

    private FloatingActionButton fab;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private String currentChat;
    private ArrayList<DocumentSnapshot> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentChat = extras.getString("com.example.EXTRA_MESSAGE");
            //The key argument here must match that used in the other activity
        } else {
            currentChat = "";
        }
        setContentView(R.layout.activity_message);
        firestore = FirebaseFirestore.getInstance();
        setRepeatingAsyncTask();
        //displayChatMessages();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.input);

                Map<String, Object> message = new HashMap<>();
                message.put("Text", input.getText().toString());
                message.put("Username", currentUser.getDisplayName());
                message.put("Uid", currentUser.getUid());
                message.put("TimeSent", new Date().getTime());
                firestore.collection("chat").document(currentChat)
                        .collection("message").add(message);
                input.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_out:
                Intent signIn = new Intent(this, SignInActivity.class);
                signIn.putExtra(EXTRA_SIGNING_OUT, true);
                startActivity(signIn);
                return true;

            case R.id.menu_chats:
                Intent chat = new Intent(this, ChatActivity.class);
                startActivity(chat);
                return true;

            case R.id.menu_map:
                Intent maps = new Intent(this, MapsActivity.class);
                startActivity(maps);
                return true;

            case R.id.menu_profile:
                Intent profile = new Intent(this, ProfileActivity.class);
                startActivity(profile);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayChatMessages() {
        firestore.collection("chat").document(currentChat)
                .collection("message").limit(200).orderBy("TimeSent").get().addOnSuccessListener(
                new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        messages = (ArrayList<DocumentSnapshot>) queryDocumentSnapshots.getDocuments();
                        MessageAdapter adapter = new
                                MessageAdapter(MessageActivity.this, messages);

                        ListView listView = (ListView) findViewById(R.id.listview_message);
                        listView.setAdapter(adapter);

//                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                                String username = (String) messages.get(i).get("Username"); //TODO: I think if we want
//                                 TODO:to be able to look at individual users pages through the
//                                  TODO: chat this is the best place to do it, I'll write the code
//                                   TODO: for it and just leave it commented for you uncomment
//                                    TODO: line 37 for EXTRA_USERNAME
//
//                                Intent user = new Intent(MessageActivity.this,
//                                        UserActivity.class);
//                                user.putExtra(EXTRA_USERNAME, username);
//                                startActivity(user);
                    }
                });
    }


    private void setRepeatingAsyncTask() {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            displayChatMessages();
                        } catch (Exception e) {
                            // error, do something
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 1000);  // interval of 1 second
    }
}