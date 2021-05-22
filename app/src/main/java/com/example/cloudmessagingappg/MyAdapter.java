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

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>
{
    private ArrayList<String> arrayList =  new ArrayList<>(); ;
    private ArrayList<String> ImagearrayList = new ArrayList<>() ;
    private ArrayList<String> idArrayList = new ArrayList<>();
    private Context context;

    public MyAdapter(Context context, ArrayList<String> arrayList, ArrayList<String> ImagearrayList,ArrayList<String> idArrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.ImagearrayList = ImagearrayList;
        this.idArrayList = idArrayList;
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_layout,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
        holder.UsersTextView.setText( arrayList.get(position) );
        if( ImagearrayList.get(position).equals("default") )
            holder.UsersImageView.setBackgroundResource( R.drawable.personimage);
        else{
            Glide.with(context)
            .load( ImagearrayList.get(position) )
            .apply(RequestOptions.circleCropTransform())
            .into(holder.UsersImageView);
        }
    }

    @Override
    public int getItemCount() {
        return (arrayList == null) ? 0 : arrayList.size() ;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView UsersImageView;
        private TextView UsersTextView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            UsersImageView = itemView.findViewById( R.id.UsersImageView );
            UsersTextView = itemView.findViewById( R.id.UsersTextView );

                    itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println( "CLICKED" );

                    Intent intent = new Intent(itemView.getContext(),MessageActivity.class);
                    intent.putExtra("name",arrayList.get(getLayoutPosition()));
                    intent.putExtra( "id",idArrayList.get( getLayoutPosition() ) );
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
