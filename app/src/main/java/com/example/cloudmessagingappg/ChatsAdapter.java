package com.example.cloudmessagingappg;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder>
{

    private Context context;

    private ArrayList<recentChatsClass> chatsArrayList;

    public ChatsAdapter(Context context,ArrayList<recentChatsClass> chatsArrayList) {
        this.context = context;
        this.chatsArrayList = chatsArrayList;
    }

    @NonNull
    @Override
    public ChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate( R.layout.recent_chats_layout,parent,false );
        return new ChatsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsAdapter.ViewHolder holder, int position) {

        if( chatsArrayList.get(position).getImageUrl().equals("default") )
            holder.rcUsersImageView.setBackgroundResource( R.drawable.personimage );

        else{
            Glide.with(context)
                    .load( chatsArrayList.get(position).getImageUrl() )
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.rcUsersImageView);
        }

        holder.rcUsersTextView.setText( chatsArrayList.get(position).getName() );
        holder.recentChatTv.setText( chatsArrayList.get(position).getRecentchat() );

    }

    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView rcUsersImageView;
        private TextView rcUsersTextView, recentChatTv;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            rcUsersImageView = itemView.findViewById( R.id.rcUsersImageView );
            rcUsersTextView = itemView.findViewById( R.id.rcUsersTextView );
            recentChatTv = itemView.findViewById( R.id.recentChatTv );

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(itemView.getContext(),MessageActivity.class);
                    intent.putExtra("name",chatsArrayList.get(getLayoutPosition()).getName());
                    intent.putExtra( "id",chatsArrayList.get(getLayoutPosition()).getChatId() );
                    itemView.getContext().startActivity(intent);
                }
            });
        }

    }
}
