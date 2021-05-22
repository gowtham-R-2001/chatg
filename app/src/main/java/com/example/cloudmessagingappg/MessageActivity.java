package com.example.cloudmessagingappg;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cloudmessagingappg.Chat.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity
{
    private TextView nameTv, backTv;
    private ImageView userImage;
    private String name, id, status;
    private FirebaseUser firebaseUser;
    private TextView sendMessageTv;
    private Button sendBtn, callBtn;
    public int flg = 0 ;
    private String image;
    private TextView statusTv;
    private Button onoff;
    private Query query, query1 ;
    SinchClient sinchClient;
    Call call;


    private ArrayList<Chat> chatArrayList ;
    private RecyclerView recyclerView;

    HashMap<String,String> hashMap1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        getSupportActionBar().hide();

        //startService(new Intent(MessageActivity.this, serviceClass.class));

        name = getIntent().getStringExtra("name");
        id = getIntent().getStringExtra("id");

        chatArrayList = new ArrayList<>();

        nameTv = findViewById( R.id.nameTv );
        userImage = findViewById( R.id.userImage );
        backTv = findViewById(R.id.backTv);
        statusTv = findViewById( R.id.statusTv );
        onoff = findViewById(R.id.onoff);
        callBtn = findViewById(R.id.callBtn);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //call = null;
                AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
                alertDialog.setMessage("Do you want to call?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Call", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Intent intent = new Intent(MessageActivity.this,callingService.class);
                        intent.putExtra("id",id);
                        startService(intent);







//                        call = sinchClient.getCallClient().callUser(id);
//                        call.addCallListener(new sinchCallListener());
//
//                            AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
//                            alertDialog.setMessage("calling...");
//                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Hangup", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i)
//                                {
//                                    dialogInterface.dismiss();
//                                    call.hangup();
//                                    call = null;
//                                }
//                            });
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                });
                alertDialog.show();
            }
        });

//        sinchClient = Sinch.getSinchClientBuilder()
//                .context(getApplicationContext())
//                .userId(firebaseUser.getUid())
//                .applicationKey("8889c52d-789d-405d-8c4c-173c13c88e44")
//                .applicationSecret("Ley6jjR3KEuvP4zRuWf7mQ==")
//                .environmentHost("clientapi.sinch.com")
//                .build();
//
//        sinchClient.setSupportCalling(true);
//        sinchClient.startListeningOnActiveConnection();
//
//        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener(){});
//
//        sinchClient.start();

        hashMap1 = new HashMap<>();

        backTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageActivity.super.onBackPressed();
            }
        });

        sendBtn = findViewById( R.id.sendBtn );
        sendMessageTv = findViewById( R.id.sendMessageTv );

        //Recycler view

        recyclerView = findViewById(R.id.messageRecyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MessageActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String message = sendMessageTv.getText().toString() ;
                sendMessageTv.setText("");

                if( !(message.equals("")) )
                    sendMsg( firebaseUser.getUid(),message,id) ;
            }
        });

        dataSettingMethod();
    }

    private void sendMsg(String uid, final String message, final String id)
    {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference() ;

        Calendar calendar = Calendar.getInstance() ;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        String time = simpleDateFormat.format(calendar.getTime());
        System.out.println( "TIME : " + time );

        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put( "sender",uid ) ;
        hashMap.put( "receiver", id) ;
        hashMap.put( "message",message ) ;
        hashMap.put( "time",time ) ;

        chatsRef.child("chats").push().setValue(hashMap);

        final DatabaseReference chatsListRef = FirebaseDatabase.getInstance().getReference("chatsList");
        hashMap1.put("chatId",id);
        hashMap1.put("name",name);
        hashMap1.put("imageUrl",image);
        hashMap1.put("recentchat",message);

        chatsListRef.child(firebaseUser.getUid()).orderByChild("chatId").equalTo(id)
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if( !(snapshot.exists()) )
                        {
                            chatsListRef.child( firebaseUser.getUid() ).push().setValue(hashMap1);

                            query = FirebaseDatabase.getInstance().getReference("users").child("userId").orderByChild("id").equalTo(firebaseUser.getUid());

                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            if(snapshot.exists())
                                            {
                                                for( DataSnapshot dataSnapshot : snapshot.getChildren() )
                                                {
                                                    String Xname = dataSnapshot.child("name").getValue( String.class ) ;
                                                    String XimageUrl = dataSnapshot
                                                            .child("imageUrl").getValue(String.class);

                                                    HashMap<String,String> hashMap3 = new HashMap<>();
                                                    hashMap3.put("chatId",firebaseUser.getUid());
                                                    hashMap3.put("name",Xname);
                                                    System.out.println(  "Name : " + name + "  XimageUrlXX" + XimageUrl );
                                                    hashMap3.put("imageUrl",XimageUrl);
                                                    hashMap3.put("recentchat",message);

                                                    chatsListRef.child( id ).push().setValue( hashMap3 );
                                                    break;
                                                }
                                            }
                                                query.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
                        }
                        else {
                            System.out.println( "You are the sender..." );

                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                DatabaseReference chatsListRef1 = FirebaseDatabase.getInstance().getReference("chatsList")
                                        .child( firebaseUser.getUid() ).child( dataSnapshot.getKey() );

                                HashMap<String,Object> map = new HashMap<>();
                                map.put("recentchat",message);
                                chatsListRef1.updateChildren(map);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });


                query1 = chatsListRef.child(id).orderByChild("chatId").equalTo(firebaseUser.getUid());

                query1.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if( snapshot.exists() )
                        {
                            System.out.println( "PRINT ID : " + id );
                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                DatabaseReference chatsListRef1 = FirebaseDatabase.getInstance().getReference("chatsList")
                                        .child( id ).child( dataSnapshot.getKey() );

                                HashMap<String,Object> map = new HashMap<>();
                                map.put("recentchat",message);
                                chatsListRef1.updateChildren(map);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    public void readMessages()
    {
        FirebaseDatabase.getInstance().getReference("chats").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if( snapshot.exists() )
                        {
                            chatArrayList.clear();
                            for(DataSnapshot dataSnapshot: snapshot.getChildren())
                            {
                                Chat chat = dataSnapshot.getValue(Chat.class);

                                System.out.println( "chat.getSender() : " + chat.getSender() );
                                System.out.println( "firebaseUser.getUid() : " + firebaseUser.getUid() );
                                System.out.println( "chat.getReceiver() : " + chat.getReceiver() );
                                System.out.println( "receiver : " + id );

                                if( chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(id) ||
                                    chat.getSender().equals(id) && chat.getReceiver().equals(firebaseUser.getUid()) )
                                {
                                    chatArrayList.add(chat);
                                }
                                MessageAdapter messageAdapter = new MessageAdapter(MessageActivity.this,chatArrayList);
                                recyclerView.setAdapter(messageAdapter);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void dataSettingMethod()
    {
        nameTv.setText( name );

        FirebaseDatabase.getInstance().getReference("userStatus").child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            status = "" + dataSnapshot.child("status").getValue().toString();
                            System.out.println( "status  :::: " + status );
                            statusTv.setText( ""+status );
                            if(status.equals("online")) {
                                onoff.setBackgroundResource(R.drawable.online);
                            }
                            else
                                onoff.setBackgroundResource(R.drawable.offline);

                            break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

        FirebaseDatabase.getInstance().getReference("users").child("userId").orderByChild("name").equalTo(name)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        for( DataSnapshot dataSnapshot : snapshot.getChildren() )
                        {
                               image = dataSnapshot.child("imageUrl").getValue(String.class);

                                if( image.equals("default") )
                                    userImage.setImageResource(R.drawable.personimage);
                                else
                                {
                                    Glide.with(getApplicationContext() )
                                            .load(image)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into( userImage );
                                }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

        readMessages();
    }

//    private class sinchCallListener implements CallListener
//    {
//        @Override
//        public void onCallProgressing(com.sinch.android.rtc.calling.Call call) {
//            Toast.makeText(MessageActivity.this, "Ringing...", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onCallEstablished(com.sinch.android.rtc.calling.Call call)
//        {
//            Toast.makeText(MessageActivity.this, "connected", Toast.LENGTH_SHORT).show();
//            AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
//
//            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Hangup", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i)
//                {
//                    dialogInterface.dismiss();
//                    call.hangup();
//                }
//            });
//            alertDialog.show();
//        }
//
//        @Override
//        public void onCallEnded(com.sinch.android.rtc.calling.Call call)
//        {
//            call.hangup();
//            Toast.makeText(MessageActivity.this, "call ended", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onShouldSendPushNotification(com.sinch.android.rtc.calling.Call call, List<PushPair> list) { }
//    }

//    private class SinchCallClientListener implements CallClientListener
//    {
//        @Override
//        public void onIncomingCall(CallClient callClient, final com.sinch.android.rtc.calling.Call incomingCall)
//        {
//            try {
//                call = incomingCall;
//                Toast.makeText(MessageActivity.this, String.valueOf(callClient), Toast.LENGTH_SHORT).show();
//                AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
//                alertDialog.setMessage("incoming call");
//                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "pick", new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i)
//                    {
//                        call.answer();
//                        call.addCallListener(new sinchCallListener());
//                        Toast.makeText(MessageActivity.this, "call is started", Toast.LENGTH_SHORT).show();
//
//                        AlertDialog alertDialog1 = new AlertDialog.Builder(MessageActivity.this).create();
//                        alertDialog1.setButton(AlertDialog.BUTTON_NEGATIVE, "hangup", new DialogInterface.OnClickListener()
//                        {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                call.hangup();
//                            }
//                        });
//                        alertDialog1.show();
//                    }
//                });
//
//                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i)
//                    {
//                        dialogInterface.dismiss();
//                        call.hangup();
//                    }
//                });
//                alertDialog.show();
//            }
//            catch (Exception e){
//                System.out.println(  "ERROR OCCURED DUDE : " + e.getClass() + " - " + e.getMessage());
//            }
//        }
//    }
}