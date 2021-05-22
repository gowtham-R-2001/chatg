package com.example.cloudmessagingappg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;


public class signUpActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView emailInput, regUsernameInput, regPassInput, regConPassInput;
    private Button regBtn;
    private TextView loginTV;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference userReference;
    private String emailRegex = "^[a-zA-z0-9]{5,}(@)(.){2,}([.])(.*)$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String manufacturer = android.os.Build.MANUFACTURER;

        try {
            AlertDialog alertDialog = new AlertDialog.Builder(signUpActivity.this).create();
            alertDialog.setMessage("Please enable 'AutoStart' permission to continue");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    Toast.makeText(signUpActivity.this, "please enable the chatG autoStart permission", LENGTH_SHORT).show();
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
            alertDialog.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        init();                                                                            // method call to initialize the UI objects

        setClickListenerMethod();                                                          // click listener method call
    }

    private void init()                                                                    // method to initialize the UI objects
    {
        emailInput = findViewById(R.id.emailInput);
        regUsernameInput = findViewById(R.id.regUsernameInput);
        regPassInput = findViewById(R.id.regPassInput);
        regConPassInput = findViewById(R.id.regConPassInput);
        regBtn = findViewById(R.id.regBtn);
        loginTV = findViewById(R.id.loginTv);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference("users").child("userId");
    }

    private void setClickListenerMethod()                                                   // click listener method
    {
        loginTV.setOnClickListener(this);
        regBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)                                                          // onclick listener method
    {
        switch (view.getId())
        {
            case R.id.regBtn:
                int check = checkinput();

                if(check == -1)
                    Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();

                else if(check == 1)
                    Toast.makeText(this, "Passwords didn't match", Toast.LENGTH_SHORT).show();

                else if(check == -2)
                    Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();

                else
                {
                    String userName = regUsernameInput.getText().toString().trim();

                    progressBar.setVisibility(View.VISIBLE);

                    Query checkUsername = FirebaseDatabase.getInstance().getReference("users").child("userId").orderByChild("name").equalTo(userName);
                        checkUsername.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if (snapshot.getChildrenCount() > 0)
                                {
                                    Toast.makeText(signUpActivity.this, "username already exists", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    progressBar.invalidate();
                                }

                                else
                                {
                                    if ( !(regPassInput.getText().toString().trim().length() >= 6) )
                                    {
                                        Toast.makeText(signUpActivity.this, "password must contains above 6 characters", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                        progressBar.invalidate();
                                    }

                                    else {
                                        System.out.println("I entered here");
                                        String email = emailInput.getText().toString().trim();
                                        String password = regPassInput.getText().toString().trim();
                                        regBtn.setEnabled(false);

                                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful())
                                                {
                                                    String userId = mAuth.getUid();

                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.clear();
                                                    hashMap.put("id", userId);
                                                    hashMap.put("name", regUsernameInput.getText().toString().trim());
                                                    hashMap.put("imageUrl", "default");
                                                    hashMap.put("password", regPassInput.getText().toString().trim());
                                                    hashMap.put("email",emailInput.getText().toString().trim());

                                                    userReference.push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                progressBar.invalidate();
                                                                Toast.makeText(signUpActivity.this, "Registration Success", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(signUpActivity.this, MainActivity.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                            else {
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                progressBar.invalidate();
                                                                System.out.println("hashmap error : " + task.getException().getMessage());
                                                                regBtn.setEnabled(true);
                                                            }
                                                        }
                                                    });
                                                }
                                                else {
                                                    progressBar.setVisibility(View.GONE);
                                                    progressBar.invalidate();
                                                    Toast.makeText(signUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    regBtn.setEnabled(true);
                                                }
                                            }
                                        });
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(signUpActivity.this, "Database errro", Toast.LENGTH_SHORT).show();
                            }
                        });
                }

                break;

            case R.id.loginTv:
                Intent intent = new Intent(signUpActivity.this,signInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;

            default:
                break;
        }};

    private int checkinput()
    {
        String pass = regPassInput.getText().toString().trim();
        String Conpass = regConPassInput.getText().toString().trim();
        String name = regUsernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();

        if(TextUtils.isEmpty(pass) || TextUtils.isEmpty(Conpass) || TextUtils.isEmpty(name) || TextUtils.isEmpty(email) )
            return (-1);

        else
        {
            if( !(pass.equals(Conpass)) )
                return (1);

            else
            {
                Pattern p = Pattern.compile(emailRegex);
                Matcher m = p.matcher(email);
                boolean result = m.matches();

                if(result)
                    return(0);
                else
                    return(-2);
            }
        }
    }
}