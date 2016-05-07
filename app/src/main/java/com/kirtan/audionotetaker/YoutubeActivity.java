package com.kirtan.audionotetaker;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class YoutubeActivity extends YouTubeBaseActivity {

    YouTubePlayerView youTubePlayerView;
    YouTubePlayer youTubePlayer;
    YouTubePlayer.OnInitializedListener onInitializedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.YoutubePlayer);
        final String videoId = getIntent().getStringExtra("VideoID");
        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer yPlayer, boolean b) {
                youTubePlayer = yPlayer;
                youTubePlayer.loadVideo(videoId);
                youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                youTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener(){
                    @Override
                    public void onFullscreen(boolean arg0) {
                    }});
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };
        youTubePlayerView.initialize(String.valueOf(R.string.YAPI), onInitializedListener);
    }
}
