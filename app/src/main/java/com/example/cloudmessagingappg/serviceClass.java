package com.example.cloudmessagingappg;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public class serviceClass extends Service
{
    private FirebaseUser firebaseUser;

    @Override
    public void onCreate()
    {
        super.onCreate();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firebaseUser =  mAuth.getCurrentUser();
        checkStatus("online");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        Toast.makeText(this, "Destroyed", Toast.LENGTH_SHORT).show();
        super.onTaskRemoved(rootIntent);
        Calendar calendar = Calendar.getInstance() ;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        String time = simpleDateFormat.format(calendar.getTime());
        checkStatus("last seen at " + time );

        Intent intent = new Intent(getApplicationContext(),callingService.class);
        startService(intent);

//        PendingIntent pendingIntent = PendingIntent.getService(
//                getApplicationContext(),
//                1001,
//                new Intent(getApplicationContext(),getClass()),
//                PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,1000,pendingIntent);

        stopSelf();
    }

    public void checkStatus(final String status)
    {
        final DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("userStatus").child(firebaseUser.getUid());

        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("userId",firebaseUser.getUid());
        hashMap.put("status",status);

        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        DatabaseReference statusRef1 = FirebaseDatabase.getInstance().getReference("userStatus")
                                .child(snapshot.getKey()).child(dataSnapshot.getKey());

                        System.out.println( "XdataSnapshot.getValue()  :::::: " + dataSnapshot.getValue() + "   dataSnapshot.getKey() : " + dataSnapshot.getKey() + "   snapshot.getKey() : " + snapshot.getKey() ) ;

                        HashMap<String,Object> hashMap1 = new HashMap<>();
                        hashMap1.put("status",status);
                        statusRef1.updateChildren(hashMap1);
                        break;
                    }
                }
                else
                    statusRef.push().setValue(hashMap);

                statusRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}