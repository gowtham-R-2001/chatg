package com.example.cloudmessagingappg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersFragment extends Fragment
{
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private ArrayList<String> arrayList;
    private ArrayList<String> ImagearrayList;
    private ArrayList<String> idArrayList ;
    private ProgressBar MainProgressBAr;
    private TextView messageTv;
    private RecyclerView recyclerView;
    MyAdapter myAdapter;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);

        MainProgressBAr = v.findViewById( R.id.MainProgressBar );
        messageTv = v.findViewById( R.id.messageTv );

        init();

        // Inflate the layout for this fragment
        return v;
    }

    private void init()
    {
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        arrayList = new ArrayList<>();
        ImagearrayList = new ArrayList<>();
        idArrayList = new ArrayList<>();

        MainProgressBAr.setVisibility(View.VISIBLE);
        messageTv.setVisibility( View.VISIBLE );


        FirebaseDatabase.getInstance().getReference("users").child("userId").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        arrayList.clear();
                        ImagearrayList.clear();
                        idArrayList.clear();

                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            userData userData = dataSnapshot.getValue(userData.class);
                            System.out.println( "NAMES : " + userData.getName() );

                            if( ! (userData.getId().equals(firebaseUser.getUid())) ){
                                arrayList.add( userData.getName() );
                                ImagearrayList.add( userData.getImageUrl() );
                                idArrayList.add( userData.getId() );
                            }
                        }

                        myAdapter = new MyAdapter(getContext(),arrayList,ImagearrayList,idArrayList);
                        LinearLayoutManager layoutManager = new LinearLayoutManager( getContext() );
                        recyclerView.setLayoutManager( layoutManager );
                        recyclerView.setAdapter(myAdapter);

                        MainProgressBAr.setVisibility( View.GONE );
                        messageTv.setVisibility( View.GONE );
                        MainProgressBAr.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

}