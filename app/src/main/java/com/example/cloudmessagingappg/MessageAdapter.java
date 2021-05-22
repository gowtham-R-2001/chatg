package com.example.cloudmessagingappg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloudmessagingappg.Chat.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>
{

    private ArrayList<Chat> chatArrayList ;
    private Context context;
    private static final int MESG_TYPE_RIGHT = 0;
    private static final int MESG_TYPE_lEFT = 1;

    public MessageAdapter(Context context,ArrayList<Chat> chatArrayList) {

        this.context = context;
        this.chatArrayList = chatArrayList;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if( viewType == MESG_TYPE_RIGHT )
        {
            View v = LayoutInflater.from( context ).inflate( R.layout.chat_right_layout,parent,false );
            return new MessageAdapter.ViewHolder(v);
        }
        else
        {
            View v = LayoutInflater.from( context ).inflate( R.layout.chat_left_layout,parent,false );
            return new MessageAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position)
    {
        holder.show_message.setText( chatArrayList.get(position).getMessage() );
        holder.show_time.setText( chatArrayList.get(position).getTime() );
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView show_message;
        private TextView show_time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById( R.id.show_message );
            show_time = itemView.findViewById( R.id.show_time );
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String myID = firebaseUser.getUid();

        if( chatArrayList.get(position).getSender().equals(myID) )
            return MESG_TYPE_RIGHT;

        else
            return MESG_TYPE_lEFT;
    }
}