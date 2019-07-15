package com.example.hyferion.spotifyrec.SpotifyConnect;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hyferion.spotifyrec.Model.EndPoints;
import com.example.hyferion.spotifyrec.Model.Playlist;
import com.example.hyferion.spotifyrec.Model.Song;
import com.example.hyferion.spotifyrec.Model.User;
import com.example.hyferion.spotifyrec.VolleyCallBack;
import com.google.gson.Gson;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SpotifyConnector {

    private HashSet<Song> songs = new HashSet<Song>();
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;
    private static final String CLIENT_ID = "3a7b0154a0fd4a868e41d59834f97bd5";
    private static final String REDIRECT_URI = "com.example.hyferion.spotifyrec://callback";
    public SpotifyAppRemote mSpotifyAppRemote;
    public Context mcontext;


    public SpotifyConnector(Context context) {
        sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(context);
        mcontext = context;

        getSpotifyAppRemote();
    }


    public void addSongToDislikedPlaylist(Song song) {
        PlaylistService playlistService = new PlaylistService(queue, sharedPreferences);
        playlistService.put(song);
    }

    public void removeSongFromDislikedPlaylist(Song song) {
        PlaylistService playlistService = new PlaylistService(queue, sharedPreferences);
        playlistService.delete(song);
    }

    public void saveSongToLibrary(Song song) {
        TracksService tracksService = new TracksService(queue, sharedPreferences);
        tracksService.put(song);
    }

    public void removeSongFromLibrary(Song song) {
        TracksService tracksService = new TracksService(queue, sharedPreferences);
        tracksService.delete(song);
    }


    public HashSet<Song> getRecentlyPlayedTracks() {
        String endpoint = EndPoints.RECENTLY_PLAYED.toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    Gson gson = new Gson();
                    JSONArray jsonArray = response.optJSONArray("items");
                    for (int n = 0; n < jsonArray.length(); n++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(n);
                            object = object.optJSONObject("track");
                            Song song = gson.fromJson(object.toString(), Song.class);
                            song.setArtist(object.optJSONArray("artists").optJSONObject(0).getString("name"));
                            try {
                                String imgUrl = object.optJSONObject("album").optJSONArray("images").optJSONObject(0).getString("url");
                                song.setImageURL(imgUrl);
                            } catch (NullPointerException e) {
                                song.setImageURL("");
                            }

                            songs.add(song);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, error -> {
                    // TODO: Handle error

                }) {


            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("limit", "50");
                return params;
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);

        return songs;

    }

    public void playSong(Song song) {
        String songToPlay = "spotify:track:" + song.getId();
        mSpotifyAppRemote.getPlayerApi().play(songToPlay);
    }

    private void getSpotifyAppRemote() {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(mcontext, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");
                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }


}
