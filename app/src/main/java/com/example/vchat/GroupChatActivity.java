package com.example.vchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vchat.Adapter.ChatAdapter;
import com.example.vchat.Models.MessageModel;
import com.example.vchat.databinding.ActivityGroupChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    ProgressDialog progressDialog;

    private ImageView summarizer;
    String senderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChatActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ArrayList<MessageModel> messageModels = new ArrayList<>();


        final String senderId = FirebaseAuth.getInstance().getUid();
        binding.userName.setText("Group chat");


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderName = snapshot.child(senderId).child("userName").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final ChatAdapter adapter = new ChatAdapter(messageModels,this);
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);


        database.getReference().child("Group chat")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                            MessageModel model = dataSnapshot.getValue(MessageModel.class);
                            messageModels.add(model);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = binding.enterMessage.getText().toString();
                final MessageModel model = new MessageModel(senderId,message);
                Long timestamp = new Date().getTime();
                model.setTimestamp(new Date().getTime());
                SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat sfd2 = new SimpleDateFormat("HH:mm:ss");
                model.setDate(sfd.format(new Date(timestamp)));
                model.setTime(sfd2.format(new Date(timestamp)));

                model.setUserName(senderName);



                binding.enterMessage.setText("");
                database.getReference().child("Group chat")
                        .push()
                        .setValue(model)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(GroupChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        summarizer = findViewById(R.id.summarize);


        summarizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(GroupChatActivity.this, "button clicked", Toast.LENGTH_SHORT).show();
                //showDialog();
                Intent intent = new Intent(GroupChatActivity.this,Summary.class);
                startActivity(intent);
            }
        });
    }

//    private  void showDialog(){
//
//
//
//        Dialog dialog = new Dialog(this, R.style.DialogStyle);
//        dialog.setContentView(R.layout.activity_summary);
//        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_et_message);
//
//        ImageView btnclose = dialog.findViewById(R.id.close);
//        btnclose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//                dialog.dismiss();
//            }
//        });
//        dialog.show();
//    }
}