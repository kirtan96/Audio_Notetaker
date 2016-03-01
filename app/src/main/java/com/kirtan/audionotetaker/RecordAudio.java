package com.kirtan.audionotetaker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class RecordAudio extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    Button start, stop;
    SharedPreferences myPrefs;
    String outputFile;
    String fileName, folderName;
    TextView timer;
    private Handler myHandler = new Handler();
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    int mins, secs;
    ListView note;
    FloatingActionButton add;
    ArrayList<String> noteList;
    String myUri, n, real;
    NoteListAdapter nla;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        timer = (TextView) findViewById(R.id.timer);
        note = (ListView) findViewById(R.id.list);
        add = (FloatingActionButton) findViewById(R.id.fab);
        timer.setText("00:00");
        stop.setEnabled(false);
        add.setEnabled(false);
        note.setEnabled(false);

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
                File.separator + "Audio Note" + File.separator + "Recordings");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        SharedPreferences.Editor e = myPrefs.edit();
        if(myPrefs.getInt("recordingsInt", -1) == -1)
        {
            e.putInt("recordingsInt", 1);
            e.commit();
        }
        outputFile = folder + "/recordings " + myPrefs.getInt("recordingsInt", 1) + ".3gp";
        mediaRecorder.setOutputFile(outputFile);
        myUri = Uri.fromFile(new File(outputFile)).toString();
        n = "";
        real = "";
        noteList = new ArrayList<>();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                start.setEnabled(false);
                stop.setEnabled(true);
                note.setEnabled(true);
                add.setEnabled(true);
                startTime = SystemClock.uptimeMillis();
                myHandler.postDelayed(UpdateRecordingTime, 100);
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
                timeSwapBuff += timeInMilliseconds;
                myHandler.removeCallbacks(UpdateRecordingTime);

                save();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final String current = timer.getText().toString();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordAudio.this);
                alertDialog.setTitle("Notes");
                alertDialog.setMessage("Insert notes here:");

                final EditText input = new EditText(RecordAudio.this);
                input.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setSingleLine(true);
                input.setLines(4); // desired number of lines
                input.setHorizontallyScrolling(false);
                input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                LinearLayout ll = new LinearLayout(RecordAudio.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.addView(input);
                alertDialog.setView(ll);

                alertDialog.setPositiveButton("Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                if (!input.getText().toString().trim().equals("")) {
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
                                    Collections.sort((List) (noteList));    //sort
                                    n = "";
                                    for (int i = 0; i < noteList.size(); i++) {
                                        if(noteList.get(i).trim().equals(""))
                                        {
                                            noteList.remove(i);
                                            i--;
                                        }
                                        else
                                        {
                                            n = n + noteList.get(i) + "\n";
                                        }
                                    }
                                    noteList.remove("");
                                    noteList.remove("");
                                    nla = new NoteListAdapter(noteList);
                                    note.setAdapter(nla);

                                    SharedPreferences.Editor e = myPrefs.edit();
                                    e.putString(myUri, n);
                                    e.commit();
                                } else {
                                    Toast.makeText(RecordAudio.this, "Cannot add empty note!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RecordAudio.this);
                builder.setTitle("Choose an option:");
                builder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordAudio.this);
                            alertDialog.setCancelable(false);
                            alertDialog.setTitle("Edit Note");
                            alertDialog.setMessage("Note:");

                            final EditText input = new EditText(RecordAudio.this);
                            input.setInputType(
                                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                            input.setSingleLine(true);
                            input.setLines(4); // desired number of lines
                            input.setHorizontallyScrolling(false);
                            input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                            input.setLayoutParams(lp);


                            LinearLayout ll = new LinearLayout(RecordAudio.this);
                            ll.setOrientation(LinearLayout.VERTICAL);
                            ll.addView(input);
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
                                            if (!input.getText().toString().trim().equals("")) {
                                                SharedPreferences.Editor e = myPrefs.edit();
                                                real = real.replace(x, input.getText());
                                                noteList.set(position, real);
                                                n = "";
                                                for (int i = 0; i < noteList.size(); i++) {
                                                    if(noteList.get(i).trim().equals(""))
                                                    {
                                                        noteList.remove(i);
                                                        i--;
                                                    }
                                                    else
                                                    {
                                                        n = n + noteList.get(i) + "\n";
                                                    }
                                                }
                                                e.putString(myUri, n);
                                                e.commit();
                                                nla = new NoteListAdapter(noteList);
                                                note.setAdapter(nla);
                                            } else {
                                                Toast.makeText(RecordAudio.this, "Cannot add empty note!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                            alertDialog.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                            dialog.cancel();
                                        }
                                    });

                            alertDialog.show();
                        } else {
                            SharedPreferences.Editor e = myPrefs.edit();
                            noteList.remove(position);
                            n = "";
                            for (int i = 0; i < noteList.size(); i++) {
                                if(noteList.get(i).trim().equals(""))
                                {
                                    noteList.remove(i);
                                    i--;
                                }
                                else
                                {
                                    n = n + noteList.get(i) + "\n";
                                }
                            }
                            e.putString(myUri, n);
                            e.commit();
                            nla = new NoteListAdapter(noteList);
                            note.setAdapter(nla);
                        }
                    }
                });

                builder.show();
                return true;
            }
        });

    }

    private void save() {
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putInt("recordingsInt", myPrefs.getInt("recordingsInt", 1) + 1);
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
        editor.putString(fileName, myUri);
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
                File f = new File(outputFile);
                f.delete();
                SharedPreferences.Editor e = myPrefs.edit();
                e.remove(myUri);
                e.commit();
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

    private Runnable UpdateRecordingTime = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedTime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            timer.setText("" + String.format("%02d", mins) + ":"
                    + String.format("%02d", secs));
            myHandler.postDelayed(this,100);
        }
    };

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