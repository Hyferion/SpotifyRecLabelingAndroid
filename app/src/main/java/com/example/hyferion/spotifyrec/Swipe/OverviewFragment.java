package com.example.hyferion.spotifyrec.Swipe;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hyferion.spotifyrec.Model.Playlist;
import com.example.hyferion.spotifyrec.Model.Song;
import com.example.hyferion.spotifyrec.Overview.SongAdapter;
import com.example.hyferion.spotifyrec.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OverviewFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Song> songs = new ArrayList<>();
    private DatabaseReference mDatabase;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_overview, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        getSwipedTracks();
        mAdapter = new SongAdapter(songs, getContext());
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SPOTIFY", 0);
        mDatabase = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("userid", "")).child("Tracks");
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null) {
            if (isVisibleToUser) {
                songs.clear();
                getSwipedTracks();
            } else {
            }
        }
    }


    private void getSwipedTracks() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    collectTracks((Map<String, Object>) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void collectTracks(Map<String, Object> tracks) {
        for (Map.Entry<String, Object> entry : tracks.entrySet()) {
            Map value = (Map) entry.getValue();
            Song song = new Song(entry.getKey(), value.get("name").toString());
            song.setLiked((Boolean) value.get("liked"));
            song.setImageURL(value.get("img").toString());
            song.setTimestamp((Long) value.get("time"));
            if (!song.getLiked()) {
                Object object = (HashMap) value.get("playlist");
                song.setPlaylist(new Playlist(((HashMap) object).get("id").toString(), ((HashMap) object).get("name").toString()));
            }
            songs.add(song);

            Collections.sort(songs, (o1, o2) -> (int) (o2.getTimestamp() - o1.getTimestamp()));

            mAdapter.notifyDataSetChanged();
        }
    }
}
