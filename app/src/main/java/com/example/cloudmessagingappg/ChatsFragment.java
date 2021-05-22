package com.example.cloudmessagingappg;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.service.autofill.UserData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatsFragment extends Fragment
{
    private DatabaseReference chatsRef;
    private RecyclerView rcRecyclerView;
    private ArrayList<String> namearrayList;
    private ArrayList<String> imageArrayList;
    private ArrayList<String> idArrayList;
    private ChatsAdapter chatsAdapter;
    private ArrayList<recentChatsClass> chatsArrayList;
    private String xImageUrl, xName;
    private ArrayList<String> IdsList;
    private String xID;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsRef = FirebaseDatabase.getInstance().getReference("chatsList");
        rcRecyclerView = v.findViewById( R.id.rcRecyclerView );

        init();

        FirebaseDatabase.getInstance().getReference("users").child("userId")
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                //Toast.makeText(getActivity(), "list updated", Toast.LENGTH_LONG).show();

                System.out.println("dataSnapshot.getValuex : " + snapshot.getValue());

                xImageUrl = snapshot.child("imageUrl").getValue(String.class);
                xName = snapshot.child("name").getValue(String.class);
                System.out.println("xName" + xName);
                System.out.println("Ximageurl : " + xImageUrl);

                final DatabaseReference chatsListRef = FirebaseDatabase.getInstance().getReference("chatsList");
                //final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

                FirebaseDatabase.getInstance().getReference("chatsList")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                IdsList.clear();
                                System.out.println( "DataSnapshot.... : " + snapshot );

                                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                                {
                                    System.out.println( "dataSnapshot2... :" + dataSnapshot );
                                    System.out.println( "dataSnapshot3... :" + dataSnapshot.getKey() );
                                    IdsList.add( dataSnapshot.getKey() );
                                }
                                System.out.println( "IdsList : " + IdsList );

                                for(int i = 0 ; i < IdsList.size() ; i++) {
                                    System.out.println("IdsList.get(i) :   " + IdsList.get(i));
                                    xID = IdsList.get(i);

                                    System.out.println("xID = IdsList.get(i) : " + xID);

                                    chatsListRef.child(IdsList.get(i)).orderByChild("name").equalTo(xName)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists())
                                                    {
                                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                            System.out.println("xName2 : " + xName + " Ximageurl2 : " + xImageUrl + " dataSnapshot.getKey() : " + dataSnapshot.getKey() + " xID   :: " + snapshot.getKey() + "\n\n");

                                                            DatabaseReference chatsListRef1 = FirebaseDatabase.getInstance().getReference("chatsList")
                                                                    .child(snapshot.getKey()).child(dataSnapshot.getKey());

                                                            System.out.println( "snapshot getvalue : "  + dataSnapshot.getValue() );

                                                            HashMap<String, Object> map = new HashMap<>();
                                                            map.put("imageUrl", xImageUrl);
                                                            chatsListRef1.updateChildren(map);
                                                            //Toast.makeText(getContext(), "Data exists", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    System.out.println("Error message 1 : " + error.getMessage());
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        return(v);
    }

    private void init()
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        namearrayList = new ArrayList<>();
        imageArrayList = new ArrayList<>();
        idArrayList = new ArrayList<>();
        IdsList = new ArrayList<>();

        chatsArrayList = new ArrayList<>();

        chatsRef.child( firebaseUser.getUid() ).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                chatsArrayList.clear();
                System.out.println( "Entered in a chats fragment" );

                for( DataSnapshot dataSnapshot : snapshot.getChildren() )
                {
                    System.out.println( "Entered in a chast fragment2" );
                    recentChatsClass recentChatsClass = dataSnapshot.getValue(recentChatsClass.class);

                    System.out.println( "recentChatsClass : " + recentChatsClass.getName() );
                    System.out.println( "recentChatsClass : " + recentChatsClass.getImageUrl() );
                    System.out.println( "recentChatsClass : " + recentChatsClass.getChatId() );
                    System.out.println( "recentChatsClass : " + recentChatsClass.getRecentchat() );
                    System.out.println( "snapshot.getChildren() : " + dataSnapshot );

                    chatsArrayList.add(recentChatsClass);
                }

                chatsAdapter = new ChatsAdapter(getContext(),chatsArrayList);
                LinearLayoutManager layoutManager = new LinearLayoutManager( getContext() );
                rcRecyclerView.setLayoutManager( layoutManager );
                rcRecyclerView.setAdapter(chatsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}