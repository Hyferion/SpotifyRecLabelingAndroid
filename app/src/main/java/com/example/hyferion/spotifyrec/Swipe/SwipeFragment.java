package com.example.hyferion.spotifyrec.Swipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.hyferion.spotifyrec.Model.Song;
import com.example.hyferion.spotifyrec.Overview.OverviewActivity;
import com.example.hyferion.spotifyrec.R;
import com.example.hyferion.spotifyrec.Settings.SettingsActivity;
import com.example.hyferion.spotifyrec.SpotifyConnect.SpotifyConnector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SwipeFragment extends Fragment {

    private List<Song> rowItems;
    private arrayAdapter arrayAdapter;
    private DatabaseReference songDB;

    private SpotifyConnector spotifyConnector;
    private SharedPreferences sharedPreferences;

    private View rootView;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.swipe_fragment, container, false);

        sharedPreferences = getActivity().getSharedPreferences("SPOTIFY", 0);

        ImageButton reloadBtn = (ImageButton) rootView.findViewById(R.id.reload);
        ImageButton settingsBtn = (ImageButton) rootView.findViewById(R.id.settings);

        reloadBtn.setOnClickListener(reloadListener);
        settingsBtn.setOnClickListener(settingsListener);

        songDB = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("userid", "")).child("Tracks");

        rowItems = new ArrayList<>();
        setUpSwipeCards();


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        spotifyConnector = new SpotifyConnector(getContext());
        getTracks();

    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(spotifyConnector.mSpotifyAppRemote);
    }


    private void setUpSwipeCards() {
        SwipeFlingAdapterView flingAdapterView = (SwipeFlingAdapterView) rootView.findViewById(R.id.frame);
        arrayAdapter = new arrayAdapter(getContext(), R.layout.item, rowItems);
        flingAdapterView.setAdapter(arrayAdapter);
        flingAdapterView.setFlingListener(flingListener);
        flingAdapterView.setOnItemClickListener(clickListener);
    }


    private void getTracks() {
        HashSet<Song> recentlyPlayedTracks = spotifyConnector.getRecentlyPlayedTracks();

        songDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (Song s : recentlyPlayedTracks) {
                    if (!dataSnapshot.hasChild(s.getId())) {
                        System.out.print(s.getId() + " // " + s.getName());
                        if (!rowItems.contains(s)) {
                            rowItems.add(s);
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void saveSongToFirebase(Song song, Boolean liked) {
        Long tsLong = System.currentTimeMillis() / 1000;
        String id = song.getId();

        songDB.child(id).child("id").setValue(song.getId());
        songDB.child(id).child("name").setValue(song.getName());
        songDB.child(id).child("img").setValue(song.getImageURL());
        songDB.child(id).child("time").setValue(tsLong);
        if (liked) {
            songDB.child(id).child("liked").setValue(true);
        } else {
            songDB.child(id).child("liked").setValue(false);
            songDB.child(id).child("playlist").child("id").setValue(sharedPreferences.getString("playlist", null));
            songDB.child(id).child("playlist").child("name").setValue(sharedPreferences.getString("playlistname", null));
        }
    }

    private SwipeFlingAdapterView.onFlingListener flingListener = new SwipeFlingAdapterView.onFlingListener() {
        @Override
        public void removeFirstObjectInAdapter() {
            Log.d("LIST", "removed object!");
            rowItems.remove(0);
            arrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLeftCardExit(Object o) {
            Toast.makeText(getContext(), "Disliked", Toast.LENGTH_SHORT).show();
            Song song = (Song) o;
            saveSongToFirebase(song, false);
            //spotifyConnector.addSongToDislikedPlaylist(song);

        }

        @Override
        public void onRightCardExit(Object o) {
            Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
            Song song = (Song) o;
            saveSongToFirebase(song, true);
            //spotifyConnector.saveSongToLibrary(song);
        }

        @Override
        public void onAdapterAboutToEmpty(int i) {
        }

        @Override
        public void onScroll(float v) {

        }
    };

    private SwipeFlingAdapterView.OnItemClickListener clickListener = new SwipeFlingAdapterView.OnItemClickListener() {

        @Override
        public void onItemClicked(int i, Object o) {
            Toast.makeText(getActivity(), "Playing Song", Toast.LENGTH_SHORT).show();
            Song song = (Song) o;
            String songToPlay = "spotify:track:" + song.getId();
            spotifyConnector.mSpotifyAppRemote.getPlayerApi().play(songToPlay);
        }

    };


    private View.OnClickListener settingsListener = v -> {
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
    };

    private View.OnClickListener reloadListener = v -> getTracks();





}
