package com.example.cloudmessagingappg;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;

import java.util.concurrent.ExecutionException;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

public class callingService extends Service
{
    private String id;
    SinchClient sinchClient;
    FirebaseUser firebaseUser;

    protected static MediaPlayer mediaPlayer = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Toast.makeText(this, "Calling service", Toast.LENGTH_SHORT).show();
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

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener(){});

        sinchClient.start();

        try{
            id =  intent.getStringExtra("id");
        }
        catch(Exception e){
            //Toast.makeText(this, e.getClass() +"--"+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if( id != null )
        {
            Intent intent1 = new Intent(this,callingActivity.class);
            intent1.putExtra("id",id);
            intent1.putExtra("input","0");
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class SinchCallClientListener implements CallClientListener
    {
        @Override
        public void onIncomingCall(CallClient callClient, Call call)
        {
            Toast.makeText(callingService.this, "someone trying to call you", Toast.LENGTH_SHORT).show();

            mediaPlayer =  MediaPlayer.create(getApplicationContext(), R.raw.ringtone);
            //Toast.makeText(this, mediaPlayer.toString(), Toast.LENGTH_SHORT).show();
            mediaPlayer.start();
            mediaPlayer.setLooping(true);

            try {
                boolean foreground = new ForegroundCheckTask().execute(getApplicationContext()).get();

                callerDetails.setXcall(call);
                callerDetails.setXcallClient(callClient);

                if( !(foreground) )
                {
                    String CHANNEL_ID = "CHANNEL_ID ";
                    String CHANNEL_NAME = "CHANNEL_NAME";

//                    NotificationManager notificationManager =
//                            (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//
//                    Intent myIntent = new Intent(getApplicationContext(), callingActivity.class);
//                    PendingIntent pendingIntent = PendingIntent.getActivity(
//                            getApplicationContext(),
//                            0,
//                            myIntent, 0);
//
//                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                    {
//                        Toast.makeText(callingService.this, "highend evice", Toast.LENGTH_SHORT).show();
//                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
//                        notificationManager.createNotificationChannel(channel);
//                    }
//
//                    Notification myNotification = new NotificationCompat.Builder(getApplicationContext())
//                            .setContentTitle("Exercise of Notification!")
//                            .setContentText("Do Something...")
//                            .setTicker("Notification!")
//                            .setWhen(System.currentTimeMillis())
//                            .setContentIntent(pendingIntent)
//                            .setDefaults(Notification.DEFAULT_SOUND)
//                            .setAutoCancel(true)
//                            .setSmallIcon(R.drawable.message)
//                            .build();
//
//
//                    notificationManager.notify(0, myNotification);

                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());

                    Intent myIntent = new Intent(getApplicationContext(), callingActivity.class);
                    myIntent.putExtra("input","1");
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            getApplicationContext(),
                            0,
                            myIntent, 0);




                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        Toast.makeText(callingService.this, "highend evice", Toast.LENGTH_SHORT).show();
                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
                        managerCompat.createNotificationChannel(channel);
                    }

                    Notification notification = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                            .setContentTitle("Incoming call")
                            .setContentText("Tap to open")
                            .setTicker("Notification!")
                            .setWhen(System.currentTimeMillis())
                            .setContentIntent(pendingIntent)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.message)
                            .build();

                    managerCompat.notify(0,notification);
                }
                else
                {
                    Intent intent1 = new Intent(getApplicationContext(),callingActivity.class);
                    intent1.putExtra("input","1");
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
                Toast.makeText(callingService.this, "Error task1", Toast.LENGTH_SHORT).show();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(callingService.this, "Error task2", Toast.LENGTH_SHORT).show();
            }
        }
    }
}