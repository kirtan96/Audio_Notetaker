package com.kirtan.audionotetaker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordAudio extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    Button start, stop;
    SharedPreferences myPrefs;
    String outputFile;
    String fileName, folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        stop.setEnabled(false);

        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        setTitle(fileName);
        folderName = intent.getStringExtra("folderName");
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Audio Note");
        if (!folder.exists()) {
            folder.mkdir();
        }
        SharedPreferences.Editor e = myPrefs.edit();
        outputFile = folder + "/recordings " + myPrefs.getInt("recordingsInt", 1) + ".3gp";
        System.out.println(outputFile);
        mediaRecorder.setOutputFile(outputFile);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                start.setEnabled(false);
                stop.setEnabled(true);
                Toast.makeText(RecordAudio.this, "Recording started", Toast.LENGTH_LONG).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder  = null;
                stop.setEnabled(false);
                start.setEnabled(true);
                save();
            }
        });
    }

    private void save() {
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putInt("recordingsInt", myPrefs.getInt("recordingsInt", 0) + 1);
        editor.commit();
        if(folderName.equals("All Notes"))
        {
            editor.putString("myFiles", myPrefs.getString("myFiles", "") + fileName + "\n");
            editor.commit();
        }
        else
        {
            editor.putString(folderName + " (FOLDER)", myPrefs.getString(folderName + " (FOLDER)", "") + fileName + "\n");
            editor.commit();
        }
        editor.putString(fileName, Uri.fromFile(new File(outputFile)).toString());
        editor.commit();
        Toast.makeText(this, "Recording Saved!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(RecordAudio.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("WARNING");
        builder.setMessage("You will lose this recording. Do you want to continue?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
