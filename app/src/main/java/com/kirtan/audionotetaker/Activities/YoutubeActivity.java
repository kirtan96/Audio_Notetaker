package com.kirtan.audionotetaker.Activities;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.kirtan.audionotetaker.Fragments.NoteFragment;
import com.kirtan.audionotetaker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class YoutubeActivity extends YouTubeBaseActivity implements NoteFragment.OnClickedListener {

    private YouTubePlayerView youTubePlayerView;
    private NoteFragment noteFragment;
    private YouTubePlayer youTubePlayer;
    private YouTubePlayer.OnInitializedListener onInitializedListener;
    private FloatingActionButton add;
    private ListView ytnList;
    private FragmentManager fragmentManager;
    private SharedPreferences myPrefs;
    private SharedPreferences.Editor editor;
    private boolean fragmentVisible;
    private int currentNotePos;
    private NoteListAdapter nla;
    private ArrayList<String> noteList;
    private final String
            MY_YOUTUBE_FILES = "myYouTubeFiles",
            MY_YOUTUBE_URLS = "myYouTubeURLS";
    private String cTime, videoId;
    private String splitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.YoutubePlayer);
        ytnList = (ListView) findViewById(R.id.ytnList);
        add = (FloatingActionButton) findViewById(R.id.fab);
        noteFragment = new NoteFragment();
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();
        videoId = getIntent().getStringExtra("VideoID");
        splitter = "///" + videoId + "///";
        fragmentVisible = false;
        currentNotePos = -1;
        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer yPlayer, boolean b) {
                youTubePlayer = yPlayer;
                youTubePlayer.loadVideo(videoId);
                /*youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                youTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener(){
                    @Override
                    public void onFullscreen(boolean arg0) {
                    }});*/
                youTubePlayer.setShowFullscreenButton(false);
                youTubePlayer.setManageAudioFocus(false);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };
        youTubePlayerView.initialize(String.valueOf(R.string.YAPI), onInitializedListener);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fragmentVisible)
                {
                    int n = youTubePlayer.getCurrentTimeMillis();
                    cTime = String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes((long) n),
                            TimeUnit.MILLISECONDS.toSeconds((long) n) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) n))) + ": ";
                    showFragment();
                }
                else
                {
                    hideFragment();
                }
            }
        });

        generateList();

        ytnList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String temp = noteList.get(position).substring(0, noteList.get(position).indexOf(" "));
                playFrom(temp);
            }
        });

        //TODO: listview on long click listener
    }

    private void playFrom(String temp) {
        int min = Integer.parseInt(temp.substring(0,2));
        int sec = Integer.parseInt(temp.substring(3,5));
        int t = (int) (TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
        youTubePlayer.seekToMillis(t);
        if(!youTubePlayer.isPlaying())
            youTubePlayer.play();
    }

    private void generateList() {
        noteList = new ArrayList<>();
        for(String s: myPrefs.getString(videoId, "").split(splitter))
        {
            if(!s.trim().equals(""))
                noteList.add(s);
        }
        Collections.sort((List)noteList);
        nla = new NoteListAdapter(noteList);
        ytnList.setAdapter(nla);
    }

    private void hideFragment() {
        if(fragmentVisible)
        {
            fragmentManager.beginTransaction()
                    .remove(noteFragment)
                    .commit();
            fragmentVisible = false;
            add.setVisibility(View.VISIBLE);
            if(!youTubePlayer.isPlaying())
            {
                youTubePlayer.play();
            }
        }
    }

    private void showFragment() {
        if(!fragmentVisible)
        {
            fragmentManager = getFragmentManager();
            noteFragment = new NoteFragment();
            fragmentManager.beginTransaction().
                    add(R.id.ytlayout, noteFragment).
                    commit();
            fragmentVisible = true;
            add.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onCloseClicked() {
        hideFragment();
    }

    @Override
    public void onOKClicked() {
        String n = cTime + NoteFragment.note;
        saveNote(n);
        hideFragment();
    }

    private void saveNote(String n) {
        noteList.add(n);
        Collections.sort((List)noteList);
        String temp = "";
        for(String s: noteList)
        {
            temp += s+splitter;
        }
        editor.putString(videoId, temp);
        editor.apply();
        nla = new NoteListAdapter(noteList);
        ytnList.setAdapter(nla);
    }

    /**
     * Private Class for listView
     */
    private class NoteListAdapter extends BaseAdapter
    {

        ArrayList<String> s;
        protected NoteListAdapter(ArrayList<String> s1)
        {
            s = s1;
        }
        /**
         * gets the size of conversatrion list
         * @return size of conversation list
         */
        @Override
        public int getCount()
        {
            return s.size();
        }

        /**
         * gets the selected conversation
         * @param arg0 the position on the list
         * @return the conversation at a selected position
         */
        @Override
        public String getItem(int arg0)
        {
            return s.get(arg0);
        }

        /**
         * gets the id for a selected positon
         * @param arg0 the position on the list
         * @return the id for the position
         */
        @Override
        public long getItemId(int arg0)
        {
            return arg0;
        }

        /**
         * gets the layout for a conversation
         * @param pos the psoition of the conversation
         * @param v the view for how the conversation is laid out
         * @param arg2 the view group
         * @return the overall layout of a conversation
         */
        @Override
        public View getView(int pos, View v, ViewGroup arg2)
        {
            if (pos != currentNotePos)
                v = getLayoutInflater().inflate(R.layout.player_list, null);
            else
                v = getLayoutInflater().inflate(R.layout.player_current_list, null);

            TextView lbl = (TextView) v.findViewById(R.id.note);
            TextView ts = (TextView) v.findViewById(R.id.timeStamp);
            String temp = s.get(pos);
            String n = temp.substring(temp.indexOf(" ") + 1);
            String t = temp.substring(0, temp.indexOf(" "));
            ts.setText(t);
            lbl.setText(n);

            return v;
        }

    }
}
