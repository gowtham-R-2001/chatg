package com.example.cloudmessagingappg;

import android.content.Context;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class callingActivity extends AppCompatActivity
{
    private ImageView userImage, userImage2;
    private TextView userName;
    private Button finHangupBtn, micBtn, speakerBtn;

    private Button attendBtn, hangupBtn;

    private FirebaseUser firebaseUser;
    Call call;
    SinchClient sinchClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outgoing_call);

        getSupportActionBar().hide();

        int input = Integer.parseInt( getIntent().getStringExtra("input") ) ;

        String id = getIntent().getStringExtra("id");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        sinchClient = Sinch.getSinchClientBuilder()
                .context(getApplicationContext())
                .userId(firebaseUser.getUid())
                .applicationKey("8889c52d-789d-405d-8c4c-173c13c88e44")
                .applicationSecret("Ley6jjR3KEuvP4zRuWf7mQ==")
                .environmentHost("clientapi.sinch.com")
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();

        sinchClient.start();

        if(input == 0)
        {
            setContentView(R.layout.outgoing_call);
            call = null;
            Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
            System.out.println( "id :::::: " + id );

            call = sinchClient.getCallClient().callUser(id);
            call.addCallListener(new sinchCallListener());
        }

        else if(input == 1)
        {
            setContentView(R.layout.incoming_call);

            attendBtn = findViewById(R.id.attendBtn);
            hangupBtn = findViewById(R.id.hangupBtn);

            attendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(callingActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    if( ( callingService.mediaPlayer != null ) && ( callingService.mediaPlayer.isPlaying() ) )
                        callingService.mediaPlayer.stop();
                    call.answer();
                    attendBtn.setVisibility(View.GONE);
                    hangupBtn.setVisibility(View.GONE);
                    finHangupBtn.setVisibility(View.VISIBLE);
                    call.addCallListener(new sinchCallListener());
                }
            });

            hangupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(callingActivity.this, "ended", Toast.LENGTH_SHORT).show();
                    if( ( callingService.mediaPlayer != null ) && ( callingService.mediaPlayer.isPlaying() ) )
                        callingService.mediaPlayer.stop();
                    call.hangup();
                    finish();
                }
            });

            SinchCallClientListener sinchCallClientListener = new SinchCallClientListener();
            try{
                sinchCallClientListener.onIncomingCall( callerDetails.getXcallClient(),callerDetails.getXcall() );
//                Toast.makeText(this, "client : "+callerDetails.getXcallClient(), Toast.LENGTH_SHORT).show();
//                Toast.makeText(this, "cller : " + callerDetails.getXcall(), Toast.LENGTH_SHORT).show();
            }
            catch (Exception e){
                Toast.makeText(this, "Error occurs !!!!!", Toast.LENGTH_SHORT).show();
            }
        }

        else
            Toast.makeText(this, "unknown error", Toast.LENGTH_SHORT).show();

        userImage = findViewById(R.id.userImage);
        userImage2 = findViewById(R.id.userImage2);

        userName = findViewById(R.id.userName);

        finHangupBtn = findViewById(R.id.finHangupBtn);
        micBtn =  findViewById(R.id.micBtn);
        speakerBtn = findViewById(R.id.speakerBtn);

        Query query = FirebaseDatabase.getInstance().getReference("users").child("userId").orderByChild("id")
                .equalTo(id);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        String image = dataSnapshot.child("imageUrl").getValue(String.class);
                        String xusername = dataSnapshot.child("name").getValue(String.class);

                        userName.setText(xusername);

                        if(image.equalsIgnoreCase("default"))
                            userImage.setImageResource(R.drawable.personimage);
                        else{

                            Glide.with(getApplicationContext())
                                    .load(image)
                                    .into(userImage);

                            Glide.with(getApplicationContext())
                                    .load(image)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(userImage2);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

            micBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);

                    if( !(audioManager.isMicrophoneMute()) )
                    {
                        audioManager.setMicrophoneMute(true);
                        micBtn.getBackground().setColorFilter(getResources().getColor(R.color.lightblack), PorterDuff.Mode.SRC_ATOP);
                    }
                    else
                    {
                        audioManager.setMicrophoneMute(false);
                        micBtn.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    }
                }
            });

            speakerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);

                    if( !(audioManager.isSpeakerphoneOn()) )
                    {
                        speakerBtn.getBackground().setColorFilter(getResources().getColor(R.color.lightblack), PorterDuff.Mode.SRC_ATOP);
                        audioManager.setSpeakerphoneOn(true);
                    }
                    else
                    {
                        speakerBtn.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                        audioManager.setSpeakerphoneOn(false);
                    }
                }
            });

        finHangupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( ( callingService.mediaPlayer != null ) && ( callingService.mediaPlayer.isPlaying() ) )
                    callingService.mediaPlayer.stop();
                call.hangup();
                finish();
            }
        });
    }

    public class SinchCallClientListener implements CallClientListener
    {
        @Override
        public void onIncomingCall(CallClient callClient, final com.sinch.android.rtc.calling.Call incomingCall)
        {
            try
            {
                Toast.makeText(callingActivity.this, "incoming call", Toast.LENGTH_SHORT).show();

                System.out.println( "incomingCall::::" + incomingCall );
                System.out.println( "callClient::::" + callClient );

                call = incomingCall;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class sinchCallListener implements CallListener
    {
        @Override
        public void onCallProgressing(com.sinch.android.rtc.calling.Call call) { }

        @Override
        public void onCallEstablished(com.sinch.android.rtc.calling.Call call)
        {
            if( ( callingService.mediaPlayer != null ) && ( callingService.mediaPlayer.isPlaying() ) )
                callingService.mediaPlayer.stop();
        }

        @Override
        public void onCallEnded(com.sinch.android.rtc.calling.Call call)
        {
            if( ( callingService.mediaPlayer != null ) && ( callingService.mediaPlayer.isPlaying() ) )
                callingService.mediaPlayer.stop();
            call.hangup();
            finish();
        }

        @Override
        public void onShouldSendPushNotification(com.sinch.android.rtc.calling.Call call, List<PushPair> list) {
            System.out.println( " List<PushPair> list : " +  list  );
        }
    }
}