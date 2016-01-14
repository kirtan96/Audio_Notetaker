package com.kirtan.audionotetaker;

import android.content.Context;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SearchResult extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    Button pause;
    TextView currentTime;
    TextView finalTime;
    SeekBar seekBar;
    TextView title;
    ListView note;
    double startTime;
    private Handler myHandler = new Handler();
    String n = "";
    String uri = "";
    ArrayList<String> noteList;
    String search = "";
    SharedPreferences myPrefs;
    String real = "";

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
                //mediaPlayer.pause();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchResult.this);
                alertDialog.setTitle("Notes");
                alertDialog.setMessage("Insert notes here:");

                final EditText input = new EditText(SearchResult.this);
                input.setSingleLine();
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                final CheckBox checkBox = new CheckBox(SearchResult.this);
                checkBox.setText("Continue playing the audio");
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkBox.isChecked()) {
                            mediaPlayer.start();
                            myHandler.postDelayed(UpdateSongTime, 100);
                            SharedPreferences.Editor e = myPrefs.edit();
                            e.putBoolean("checkBox", checkBox.isChecked());
                            e.commit();
                        } else {
                            mediaPlayer.pause();
                            SharedPreferences.Editor e = myPrefs.edit();
                            e.putBoolean("checkBox", checkBox.isChecked());
                            e.commit();
                        }
                    }
                });
                if(myPrefs.getBoolean("checkBox", false))
                {
                    mediaPlayer.start();
                    myHandler.postDelayed(UpdateSongTime, 100);
                    checkBox.setChecked(true);
                }
                else
                {
                    mediaPlayer.pause();
                }
                LinearLayout ll = new LinearLayout(SearchResult.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.addView(input);
                ll.addView(checkBox);
                alertDialog.setView(ll);

                alertDialog.setPositiveButton("Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                mediaPlayer.start();
                                myHandler.postDelayed(UpdateSongTime, 100);
                                if(!input.getText().toString().trim().equals("")) {
                                    if (!n.equals("")) {
                                        n = n + "\n" + current + ": " +
                                                input.getText().toString();
                                    } else {
                                        n = current + ": " + input.getText().toString();
                                    }
                                    Scanner in = new Scanner(n);
                                    noteList = new ArrayList<>();
                                    while (in.hasNextLine()) {
                                        noteList.add(in.nextLine());
                                    }
                                    noteList.remove("");
                                    Collections.sort((List) (noteList));     //sort
                                    n = "";
                                    for (int i = 0; i < noteList.size(); i++) {
                                        n = n + noteList.get(i) + "\n";
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
                                else
                                {
                                    Toast.makeText(SearchResult.this, "Cannot add empty note!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                mediaPlayer.start();
                                myHandler.postDelayed(UpdateSongTime, 100);
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
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
        setTitle(file);
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
                    noteList.add(temp.trim());
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
                noteList.remove("");
                Collections.sort((List) noteList);
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

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchResult.this);
                builder.setTitle("Choose an option:");
                builder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchResult.this);
                            alertDialog.setTitle("Edit Note");
                            alertDialog.setMessage("Note:");

                            final EditText input = new EditText(SearchResult.this);
                            input.setSingleLine();
                            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                            input.setLayoutParams(lp);

                            final CheckBox checkBox = new CheckBox(SearchResult.this);
                            checkBox.setText("Continue playing the audio");
                            checkBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (checkBox.isChecked()) {
                                        mediaPlayer.start();
                                        myHandler.postDelayed(UpdateSongTime, 100);
                                        SharedPreferences.Editor e = myPrefs.edit();
                                        e.putBoolean("checkBox", checkBox.isChecked());
                                        e.commit();
                                    } else {
                                        mediaPlayer.pause();
                                        SharedPreferences.Editor e = myPrefs.edit();
                                        e.putBoolean("checkBox", checkBox.isChecked());
                                        e.commit();
                                    }
                                }
                            });
                            if(myPrefs.getBoolean("checkBox", false))
                            {
                                mediaPlayer.start();
                                myHandler.postDelayed(UpdateSongTime, 100);
                                checkBox.setChecked(true);
                            }
                            else
                            {
                                mediaPlayer.pause();
                            }
                            LinearLayout ll = new LinearLayout(SearchResult.this);
                            ll.setOrientation(LinearLayout.VERTICAL);
                            ll.addView(input);
                            ll.addView(checkBox);
                            alertDialog.setView(ll);
                            real = noteList.get(position);
                            final String x = noteList.get(position).substring(
                                    noteList.get(position).indexOf(" ") + 1,
                                    noteList.get(position).length());
                            input.setText(x);

                            alertDialog.setPositiveButton("Save",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                            if(!input.getText().toString().trim().equals("")) {
                                                SharedPreferences.Editor e = myPrefs.edit();
                                                real = real.replace(x, input.getText());
                                                noteList.set(position, real);
                                                n = "";
                                                for (int i = 0; i < noteList.size(); i++) {
                                                    n = n + noteList.get(i) + "\n";
                                                }
                                                e.putString(uri, n);
                                                e.commit();
                                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SearchResult.this,
                                                        android.R.layout.simple_list_item_1, noteList);
                                                note.setAdapter(arrayAdapter);
                                            }
                                            else
                                            {
                                                Toast.makeText(SearchResult.this, "Cannot add empty note!", Toast.LENGTH_LONG).show();
                                            }
                                            mediaPlayer.start();
                                            myHandler.postDelayed(UpdateSongTime, 100);
                                        }
                                    });

                            alertDialog.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                            mediaPlayer.start();
                                            myHandler.postDelayed(UpdateSongTime, 100);
                                            dialog.cancel();
                                        }
                                    });

                            alertDialog.show();
                        } else {
                            SharedPreferences.Editor e = myPrefs.edit();
                            noteList.remove(position);
                            n = "";
                            for(int i = 0; i < noteList.size(); i++)
                            {
                                n = n + noteList.get(i) + "\n";
                            }
                            e.putString(uri, n);
                            e.commit();
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SearchResult.this,
                                    android.R.layout.simple_list_item_1, noteList);
                            note.setAdapter(arrayAdapter);
                        }
                    }
                });
                builder.show();
                return true;
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