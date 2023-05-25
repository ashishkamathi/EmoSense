package com.example.emosense;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PlayerList extends AppCompatActivity {
    ArrayList<DataItem> object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list2);
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        object= new ArrayList<>();
        object = (ArrayList<DataItem>) args.getSerializable("ARRAYLIST");


        RecyclerView recyclerView = findViewById(R.id.rView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                return new ViewHolder(view, PlayerList.this);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                DataItem dataItem = object.get(position);
                ViewHolder viewHolder = (ViewHolder) holder;
                viewHolder.songName.setText(dataItem.getTrackName());
                viewHolder.artistName.setText(String.valueOf(dataItem.getArtistName()));
            }

            @Override
            public int getItemCount() {
                return object.size();
            }


        });
    }


    // Define a ViewHolder for the RecyclerView items
    private class ViewHolder extends RecyclerView.ViewHolder  {
        TextView songName;
        TextView artistName;
         Context context;


        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context= context;
            songName = itemView.findViewById(R.id.SongName);
            artistName = itemView.findViewById(R.id.ArtistName);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    // get position
                    int pos = getAdapterPosition();

                    // check if item still exists
                    if(pos != RecyclerView.NO_POSITION){
                       //RvDataItem clickedDataItem = dataItems.get(pos);
                        DataItem o = object.get(pos);
                        String url = o.getUrl();
                        Log.e("URL",url);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);                    }
                }
            });


        }


    }
}



