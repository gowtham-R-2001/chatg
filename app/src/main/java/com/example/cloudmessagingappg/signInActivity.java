package com.example.cloudmessagingappg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class signInActivity extends AppCompatActivity implements View.OnClickListener  {

    private TextView usernameInput, passwordInput, signUpTv;
    private Button loginBtn;
    private ProgressBar signInProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().hide();

        if( getIntent().getStringExtra("logout") != null ) {
            Toast.makeText(this, "logged out", Toast.LENGTH_SHORT).show();
        }

        String manufacturer = android.os.Build.MANUFACTURER;

        try {
            AlertDialog alertDialog = new AlertDialog.Builder(signInActivity.this).create();
            alertDialog.setMessage("Please enable 'AutoStart' permission to continue");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    Toast.makeText(signInActivity.this, "please enable the chatG autoStart permission", LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    if ( ("xiaomi".equalsIgnoreCase(manufacturer) ) || ("xiaomi".equalsIgnoreCase(manufacturer)) ) {
                        intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                    } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                        intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                    } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                        intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                    } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                        intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
                    } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                    }
                    List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (list.size() > 0) {
                        startActivity(intent);
                    }
                }
            });
            //alertDialog.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);*/

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                  // to make the view portrait

        init();                                                                             // initialize method call
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if( firebaseUser != null )
        {
            Intent intent = new Intent(signInActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void init()                                                                     // method to initialize the UI objects
    {
        usernameInput = findViewById( R.id.usernameInput );
        passwordInput = findViewById( R.id.passwordInput );
        signUpTv = findViewById( R.id.signUpTv );
        loginBtn = findViewById( R.id.loginBtn );
        signInProgressBar = findViewById( R.id.signInProgressBar );
        mAuth = FirebaseAuth.getInstance();

        setClickListenerMethod();                                                           // click listener method call
    }

    private void setClickListenerMethod()                                                   // click listener method
    {
        signUpTv.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    @Override                                                                               // onclick listener method
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.signUpTv:

                final Intent intent = new Intent(signInActivity.this,signUpActivity.class);
                startActivity(intent);
                break;

            case R.id.loginBtn:

                final String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                loginBtn.setEnabled( false );
                signInProgressBar.setVisibility(View.VISIBLE);

                if( !((TextUtils.isEmpty(username)) && (TextUtils.isEmpty(password))) )
                {
                    usernameInput.setError(null);
                    passwordInput.setError(null);

                    Query query = FirebaseDatabase.getInstance().getReference("users").child("userId").orderByChild("name").equalTo( username );
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if( snapshot.getChildrenCount() > 0 )
                                    {
                                        String pass = "", email = "";
                                        for(DataSnapshot dataSnapshot: snapshot.getChildren())
                                        {
                                            userData userData = dataSnapshot.getValue(com.example.cloudmessagingappg.userData.class);
                                            pass = userData.getPassword();
                                            email = userData.getEmail();
                                        }

                                        if( passwordInput.getText().toString().equals(pass) )
                                        {
                                            signInProgressBar.setVisibility(View.GONE);
                                            signInProgressBar.invalidate();

                                            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(signInActivity.this, "Login success", Toast.LENGTH_SHORT).show();
                                                        Intent intent1 = new Intent(signInActivity.this,MainActivity.class);
                                                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent1);
                                                        finish();
                                                    }
                                                    else
                                                        Toast.makeText(signInActivity.this, task.getException().toString() , Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        else {
                                            signInProgressBar.setVisibility(View.GONE);
                                            signInProgressBar.invalidate();
                                            loginBtn.setEnabled(true);
                                            Toast.makeText(signInActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        signInProgressBar.setVisibility(View.GONE);
                                        signInProgressBar.invalidate();
                                        loginBtn.setEnabled(true);
                                        Toast.makeText(signInActivity.this, "No user exists", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                }
                else{
                    signInProgressBar.setVisibility(View.GONE);
                    signInProgressBar.invalidate();
                    loginBtn.setEnabled(true);
                    if( TextUtils.isEmpty(usernameInput.toString()) )
                        usernameInput.setError("please fill this field");
                    else if( TextUtils.isEmpty(usernameInput.toString()) )
                        passwordInput.setError("Please fill the field");
                    else{
                        usernameInput.setError("please fill this field");
                        passwordInput.setError("Please fill the field");
                    }
                }

                break;

            default:
                break;
        }
    }
}