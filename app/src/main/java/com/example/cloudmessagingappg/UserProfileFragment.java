package com.example.cloudmessagingappg;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


public class UserProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView prfusernameTv, prfusernameeditTv, removePic;
    StorageReference storageReference;
    private Uri imageURI;
    private StorageTask uploadTask;
    private FirebaseUser firebaseUser;
    private final int IMAGE_REQUEST = 1;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);

        profileImage = v.findViewById(R.id.profileImage);
        prfusernameTv = v.findViewById(R.id.prfusernameTv);
        removePic = v.findViewById(R.id.removePic);
        prfusernameeditTv = v.findViewById(R.id.prfusernameeditTv);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        removePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setMessage("Do you want to remove proile picture?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        final ProgressDialog progressDialog1 = new ProgressDialog(getContext());
                        progressDialog1.setMessage("Removing..");
                        progressDialog1.show();

                        FirebaseDatabase.getInstance().getReference("users").child("userId").orderByChild("id")
                                .equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                                {
                                    DatabaseReference imageref = FirebaseDatabase.getInstance().getReference("users").child("userId")
                                            .child(dataSnapshot.getKey());
                                    HashMap<String ,Object> hashMap = new HashMap<>();
                                    hashMap.put("imageUrl","default");
                                    imageref.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                                progressDialog1.dismiss();
                                        }
                                    });
                                    break;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                });
                alertDialog.show();
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        FirebaseDatabase.getInstance().getReference("users").child("userId").orderByChild("id")
                .equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                System.out.println( "I entered again here..." );
                for( DataSnapshot dataSnapshot : snapshot.getChildren() )
                {
                    userData userData = dataSnapshot.getValue(userData.class);
                    prfusernameTv.setText( userData.getName() );

                    if(userData.getImageUrl().equals("default")){
                        profileImage.setImageResource( R.drawable.personimage );
                    }
                    else{
                        Glide.with(getContext())
                                .load(userData.getImageUrl())
                                .apply(RequestOptions.circleCropTransform())
                                .into(profileImage);
                    }
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        implementMethod();

        return (v);
    }

    private void implementMethod()
    {
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,IMAGE_REQUEST);
            }
        });
    }

    private String getFileExtension()
    {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(this.imageURI));
    }

    private void uploadMyPic()
    {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading..");
        progressDialog.show();

        if( imageURI != null )
        {
            final StorageReference fileStorageRef = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension() );

            uploadTask = fileStorageRef.putFile(imageURI);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>> () {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if( !(task.isSuccessful()) )
                        throw task.getException();

                    return fileStorageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if( task.isSuccessful() ){

                        Uri downloaduri = (Uri) task.getResult();
                        final String mUri = downloaduri.toString();

                        FirebaseDatabase.getInstance().getReference("users")
                                .child("userId").orderByChild("id").equalTo(firebaseUser.getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                for( DataSnapshot dataSnapshot : snapshot.getChildren() )
                                {
                                    DatabaseReference chatsListRef1 = FirebaseDatabase.getInstance().getReference("users")
                                            .child( "userId" ).child( dataSnapshot.getKey() );

                                    HashMap<String,Object> map = new HashMap<>();
                                    map.put("imageUrl",mUri);
                                    chatsListRef1.updateChildren(map);
                                    progressDialog.dismiss();
                                    break;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    }
                    else {
                        Toast.makeText(getContext(), "Upload failed :(", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
        else{
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( (requestCode == IMAGE_REQUEST) && (resultCode == RESULT_OK)
          && (data != null) && (data.getData() != null) ){

            imageURI = data.getData();

            if( (uploadTask != null) && (uploadTask.isInProgress()) ) {
                Toast.makeText(getContext(), "Upload in progress..", Toast.LENGTH_SHORT).show();
            }
            else{
                uploadMyPic();
            }
        }

    }
}