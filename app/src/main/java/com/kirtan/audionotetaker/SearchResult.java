package com.kirtan.audionotetaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SearchResult extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    Button pause;
    TextView currentTime;
    TextView finalTime;
    SeekBar seekBar;
    TextView title;
    Button add;
    ListView note;
    Button edit;
    double startTime;
    private Handler myHandler = new Handler();
    String n = "";
    String uri = "";
    ArrayList<String> noteList;
    String search = "";
    String sTime;
    SharedPreferences myPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pause = (Button) findViewById(R.id.pauseButton);
        currentTime = (TextView) findViewById(R.id.currentTime);
        finalTime = (TextView) findViewById(R.id.finalTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        title = (TextView) findViewById(R.id.title);
        note = (ListView) findViewById(R.id.note);
        mediaPlayer = new MediaPlayer();
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleText();
            }

            private void toggleText() {
                if (pause.getText().toString().equals("Pause")) {
                    pause.setText("Play");
                    mediaPlayer.pause();
                } else {
                    pause.setText("Pause");
                    mediaPlayer.start();
                    myHandler.postDelayed(UpdateSongTime, 100);
                }
            }
        });

        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final String current = currentTime.getText().toString();
                mediaPlayer.pause();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchResult.this);
                alertDialog.setTitle("Notes");
                alertDialog.setMessage("Insert notes here:");

                final EditText input = new EditText(SearchResult.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mediaPlayer.start();
                                myHandler.postDelayed(UpdateSongTime, 100);
                                if (!n.equals("")) {
                                    n = n + "\n" + current + ": " +
                                            input.getText().toString();
                                } else {
                                    n = current + ": " + input.getText().toString();
                                }
                                Scanner in  = new Scanner(n);
                                noteList = new ArrayList<>();
                                while(in.hasNextLine())
                                {
                                    noteList.add(in.nextLine());
                                }
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SearchResult.this,
                                        android.R.layout.simple_list_item_1, noteList);
                                note.setAdapter(arrayAdapter);
                                SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor e = myPrefs.edit();
                                e.putString(uri,
                                        n);
                                e.commit();
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mediaPlayer.start();
                                myHandler.postDelayed(UpdateSongTime, 100);
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });

        /*edit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get prompts.xml view
                mediaPlayer.pause();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchResult.this);
                alertDialog.setTitle("Notes");
                alertDialog.setMessage("Edit notes here:");

                final EditText input = new EditText(SearchResult.this);
                input.setText(n);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mediaPlayer.start();
                                myHandler.postDelayed(UpdateSongTime, 100);
                                n = input.getText().toString();
                                Scanner in  = new Scanner(n);
                                noteList = new ArrayList<>();
                                while(in.hasNextLine())
                                {
                                    noteList.add(in.nextLine());
                                }
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SearchResult.this,
                                        android.R.layout.simple_list_item_1, noteList);
                                note.setAdapter(arrayAdapter);

                                SharedPreferences.Editor e = myPrefs.edit();
                                e.putString(uri,
                                        n);
                                e.commit();
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mediaPlayer.start();
                                myHandler.postDelayed(UpdateSongTime, 100);
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });*/

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchResult.this);
                builder.setTitle("Choose an option:");
                builder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0)
                        {
                            Toast.makeText(SearchResult.this, "You can now edit...", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(SearchResult.this, "The note will be deleted...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.show();
                return true;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(progress);
                    mediaPlayer.start();
                    myHandler.postDelayed(UpdateSongTime, 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Intent intent = getIntent();
        String file = intent.getStringExtra("file");
        title.setText(file);
        Uri myUri = Uri.parse(myPrefs.getString(file, ""));
        search = intent.getStringExtra("search");
        uri = myUri.toString();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            if (myPrefs.contains(uri)) {
                n = myPrefs.getString(uri, "");
                Scanner in  = new Scanner(n);
                noteList = new ArrayList<>();
                while(in.hasNextLine())
                {
                    String temp = in.nextLine();
                    noteList.add(temp);
                    if(temp.contains(search))
                    {
                        temp = temp.substring(0, 5);
                        int min = Integer.parseInt(temp.substring(0,2));
                        int sec = Integer.parseInt(temp.substring(3, 5));
                        int t = (int)(TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(t);
                        mediaPlayer.start();
                        myHandler.postDelayed(UpdateSongTime, 100);
                    }
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SearchResult.this,
                        android.R.layout.simple_list_item_1, noteList);
                note.setAdapter(arrayAdapter);
            }
            double fTime = mediaPlayer.getDuration();
            seekBar.setMax((int) fTime);
            finalTime.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes((long) fTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) fTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) fTime)))
            );

            myHandler.postDelayed(UpdateSongTime, 100);
            Log.d("Test Worked", "Music is Playing");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Test Failed", "Music was not played");
        }

        note.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String time = note.getItemAtPosition(position).toString();
                time = time.substring(0, 5);
                int min = Integer.parseInt(time.substring(0,2));
                int sec = Integer.parseInt(time.substring(3, 5));
                int t = (int)(TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                mediaPlayer.pause();
                mediaPlayer.seekTo(t);
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer != null)
        {
            mediaPlayer.start();
            myHandler.postDelayed(UpdateSongTime, 100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {

            if (mediaPlayer.isPlaying()) {
                startTime = mediaPlayer.getCurrentPosition();
                currentTime.setText(String.format("%02d:%02d",

                                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes((long) startTime)))
                );
                seekBar.setProgress((int) startTime);
                myHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
}