package com.kirtan.audionotetaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    Button pause;
    TextView currentTime;
    TextView finalTime;
    SeekBar seekBar;
    TextView title;
    Button add;
    TextView note;
    Button edit;
    TextView t;
    double startTime;
    private Handler myHandler = new Handler();
    String n = "";
    String uri = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button chooser = (Button) findViewById(R.id.chooserButton);
        pause = (Button) findViewById(R.id.pauseButton);
        currentTime = (TextView) findViewById(R.id.currentTime);
        finalTime = (TextView) findViewById(R.id.finalTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        title = (TextView) findViewById(R.id.title);
        add = (Button) findViewById(R.id.addButton);
        note = (TextView) findViewById(R.id.note);
        edit = (Button) findViewById(R.id.editButton);
        t = (TextView) findViewById(R.id.noteText);
        hide();
        mediaPlayer = new MediaPlayer();
        chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, 1);
            }
        });
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
                // get prompts.xml view
                mediaPlayer.pause();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Notes");
                alertDialog.setMessage("Insert notes here:");

                final EditText input = new EditText(MainActivity.this);
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
                                if(!n.equals("")) {
                                    n = n + "\n" + current + ": " +
                                            input.getText().toString();
                                }
                                else {
                                    n = current + ": " + input.getText().toString();
                                }
                                note.setText(n);
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

        edit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get prompts.xml view
                mediaPlayer.pause();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Notes");
                alertDialog.setMessage("Edit notes here:");

                final EditText input = new EditText(MainActivity.this);
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
                                note.setText(n);
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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
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

    /*@Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer != null)
        {
            mediaPlayer.pause();
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {

                //the selected audio.
                Uri myUri = data.getData();
                uri = myUri.toString();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    show();
                    mediaPlayer.setDataSource(getApplicationContext(), myUri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
                    mRetriever.setDataSource(MainActivity.this, myUri);
                    show();
                    title.setText(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                    SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    if (myPrefs.contains(uri)) {
                        n = myPrefs.getString(uri, "");
                        note.setText(n);
                    }
                    /*Map<String, ?> keys = myPrefs.getAll();
                    for(Map.Entry<String,?> entry : keys.entrySet()){
                        Log.d("All the keys",entry.getKey() + ": " +
                                entry.getValue().toString());

                    }*/
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
            } else {
                if(mediaPlayer != null)
                {
                    mediaPlayer.start();
                    myHandler.postDelayed(UpdateSongTime, 100);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hide() {
        currentTime.setVisibility(View.INVISIBLE);
        finalTime.setVisibility(View.INVISIBLE);
        pause.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.INVISIBLE);
        title.setVisibility(View.INVISIBLE);
        add.setVisibility(View.INVISIBLE);
        note.setVisibility(View.INVISIBLE);
        edit.setVisibility(View.INVISIBLE);
        t.setVisibility(View.INVISIBLE);
    }

    private void show() {
        currentTime.setVisibility(View.VISIBLE);
        finalTime.setVisibility(View.VISIBLE);
        pause.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        note.setVisibility(View.VISIBLE);
        add.setVisibility(View.VISIBLE);
        edit.setVisibility(View.VISIBLE);
        t.setVisibility(View.VISIBLE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.search) {
            navigateToSearch();
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToSearch() {
        Intent intent = new Intent(this, Search.class);
        startActivity(intent);
    }
}
