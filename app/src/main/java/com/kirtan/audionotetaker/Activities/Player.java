package com.kirtan.audionotetaker.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kirtan.audionotetaker.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Player extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    Button pause, skipLeft, skipRight;
    TextView currentTime;
    TextView finalTime;
    SeekBar seekBar;
    TextView title;
    ListView note;
    TextView t;
    double startTime;
    private Handler myHandler = new Handler();
    String n = "";
    String uri = "";
    ArrayList<String> noteList;
    SharedPreferences myPrefs;
    SharedPreferences.Editor editor;
    String real = "";
    int currentNotePos;
    NoteListAdapter nla;
    boolean isAppOpen;
    ImageView shareButton;
    final int PICK_FILE = 0;
    Uri myUri;
    File exportedFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        shareButton = (ImageView) findViewById(R.id.shareButton);
        pause = (Button) findViewById(R.id.pauseButton);
        currentTime = (TextView) findViewById(R.id.currentTime);
        finalTime = (TextView) findViewById(R.id.finalTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        title = (TextView) findViewById(R.id.title);
        note = (ListView) findViewById(R.id.note);
        t = (TextView) findViewById(R.id.noteText);
        skipLeft = (Button) findViewById(R.id.leftskip);
        skipLeft.setEnabled(false);
        skipRight = (Button) findViewById(R.id.rightskip);
        isAppOpen = true;
        mediaPlayer = new MediaPlayer();

        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();

        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.fab);

        currentNotePos = -1;
        Intent intent = getIntent();
        String file = intent.getStringExtra("file");
        getSupportActionBar().setTitle(file);
        title.setText(file);
        myUri = Uri.parse(myPrefs.getString(file, ""));
        uri = myUri.toString();
        Log.d("URI", uri);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(this.getBaseContext(), myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            if (myPrefs.contains(uri)) {
                n = myPrefs.getString(uri, "");
                Scanner in = new Scanner(n);
                noteList = new ArrayList<>();
                while (in.hasNextLine()) {
                    noteList.add((in.nextLine().trim()));
                }
                noteList.remove("");
                Collections.sort((List) noteList);
                nla = new NoteListAdapter(noteList);
                note.setAdapter(nla);
            }
            double fTime = mediaPlayer.getDuration();
            seekBar.setMax((int) fTime);
            finalTime.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) fTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) fTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) fTime)))
            );

            myHandler.postDelayed(UpdateSongTime, 100);
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
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

        skipLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = currentTime.getText().toString();
                String temp2 = finalTime.getText().toString();
                int cn = Integer.parseInt(Integer.parseInt(temp.substring(0, temp.indexOf(":"))) + "" +
                        (Integer.parseInt(temp.substring(temp.indexOf(":") + 1))));
                int fn = Integer.parseInt(Integer.parseInt(temp2.substring(0, temp2.indexOf(":"))) + "" +
                        Integer.parseInt(temp2.substring(temp2.indexOf(":") + 1)));
                cn = cn-10;
                int min = (Integer.parseInt(temp.substring(0, temp.indexOf(":"))));
                int sec = (Integer.parseInt(temp.substring(temp.indexOf(":") + 1))-10);
                int t = (int)(TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                mediaPlayer.pause();
                mediaPlayer.seekTo(t);
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
                if(cn >= 10)
                {
                    skipLeft.setEnabled(true);
                }
                else
                {
                    skipLeft.setEnabled(false);
                }
                if(cn >= fn-10)
                {
                    skipRight.setEnabled(false);
                }
                else
                {
                    skipRight.setEnabled(true);
                }
            }
        });

        skipRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = currentTime.getText().toString();
                String temp2 = finalTime.getText().toString();
                int cn = Integer.parseInt(Integer.parseInt(temp.substring(0, temp.indexOf(":"))) + "" +
                        Integer.parseInt(temp.substring(temp.indexOf(":") + 1)));
                int fn = Integer.parseInt(Integer.parseInt(temp2.substring(0, temp2.indexOf(":"))) + "" +
                        Integer.parseInt(temp2.substring(temp2.indexOf(":") + 1)));
                cn = cn+10;
                int min = (Integer.parseInt(temp.substring(0, temp.indexOf(":"))));
                int sec = (Integer.parseInt(temp.substring(temp.indexOf(":") + 1))+10);
                int t = (int)(TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                mediaPlayer.pause();
                mediaPlayer.seekTo(t);
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
                if(cn >= 10)
                {
                    skipLeft.setEnabled(true);
                }
                else
                {
                    skipLeft.setEnabled(false);
                }
                if(cn >= fn-10)
                {
                    skipRight.setEnabled(false);
                }
                else
                {
                    skipRight.setEnabled(true);
                }
            }
        });

        assert add != null;
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final String current = currentTime.getText().toString();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Player.this);
                alertDialog.setTitle("Notes");
                alertDialog.setMessage("Insert notes here:");

                final EditText input = new EditText(Player.this);
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

                final CheckBox checkBox = new CheckBox(Player.this);
                checkBox.setText("Continue playing the audio");
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkBox.isChecked()) {
                            mediaPlayer.start();
                            myHandler.postDelayed(UpdateSongTime, 100);
                            editor.putBoolean("checkBox", checkBox.isChecked());
                            editor.apply();
                        } else {
                            mediaPlayer.pause();
                            editor.putBoolean("checkBox", checkBox.isChecked());
                            editor.apply();
                        }
                    }
                });
                if (myPrefs.getBoolean("checkBox", false)) {
                    mediaPlayer.start();
                    myHandler.postDelayed(UpdateSongTime, 100);
                    checkBox.setChecked(true);
                } else {
                    mediaPlayer.pause();
                }
                LinearLayout ll = new LinearLayout(Player.this);
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

                                        if (noteList.get(i).trim().equals("")) {
                                            noteList.remove(i);
                                            i--;
                                        } else {
                                            n = n + noteList.get(i) + "\n";
                                        }
                                    }
                                    nla = new NoteListAdapter(noteList);
                                    note.setAdapter(nla);
                                    editor.putString(uri,
                                            n);
                                    editor.apply();
                                } else {
                                    Toast.makeText(Player.this, "Cannot add empty note!", Toast.LENGTH_LONG).show();
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

        note.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String time = note.getItemAtPosition(position).toString();
                time = time.substring(0, 5);
                int min = Integer.parseInt(time.substring(0, 2));
                int sec = Integer.parseInt(time.substring(3, 5));
                int t = (int) (TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                mediaPlayer.pause();
                mediaPlayer.seekTo(t);
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
            }
        });

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Player.this);
                builder.setTitle("Choose an option:");
                builder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Player.this);
                            alertDialog.setTitle("Edit Note");
                            alertDialog.setMessage("Note:");

                            final EditText input = new EditText(Player.this);
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

                            final CheckBox checkBox = new CheckBox(Player.this);
                            checkBox.setText("Continue playing the audio");
                            checkBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (checkBox.isChecked()) {
                                        mediaPlayer.start();
                                        myHandler.postDelayed(UpdateSongTime, 100);
                                        editor.putBoolean("checkBox", checkBox.isChecked());
                                        editor.apply();
                                    } else {
                                        mediaPlayer.pause();
                                        editor.putBoolean("checkBox", checkBox.isChecked());
                                        editor.apply();
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
                            LinearLayout ll = new LinearLayout(Player.this);
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
                                                editor.putString(uri, n);
                                                editor.apply();
                                                nla = new NoteListAdapter(noteList);
                                                note.setAdapter(nla);
                                            }
                                            else
                                            {
                                                Toast.makeText(Player.this, "Cannot add empty note!", Toast.LENGTH_LONG).show();
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
                            noteList.remove(position);
                            n = "";
                            for(int i = 0; i < noteList.size(); i++)
                            {
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
                            editor.putString(uri, n);
                            editor.apply();
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

    private void share() {
        export();
        Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        sharingIntent.setType("text/plain");
        /*String s = "";
        for(String notes: noteList)
        {
            s+= notes + "\n";
        }
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Notes for " + title.getText().toString());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, s);*/

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Audio and Notes for " + title.getText().toString());
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(myUri);
        uris.add(Uri.fromFile(exportedFile));
        sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(Intent.createChooser(sharingIntent, "Share audio and notes via"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAppOpen = false;
        if(mediaPlayer != null && mediaPlayer.isPlaying())
        {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppOpen = true;
        if(mediaPlayer != null)
        {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAppOpen = false;
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {

            if (mediaPlayer.isPlaying() && isAppOpen) {
                startTime = mediaPlayer.getCurrentPosition();
                currentTime.setText(String.format("%02d:%02d",

                                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes((long) startTime)))
                );
                seekBar.setProgress((int) startTime);
                if(currentTime.getText().toString().equals(finalTime.getText().toString()))
                {
                    pause.setText("Play");
                }
                else {
                    pause.setText("Pause");
                }
                checkCurrentPos();
                myHandler.postDelayed(this, 100);
            }
        }
    };

    private void checkCurrentPos() {
        if(noteList != null) {
            for (int i = 0; i < noteList.size(); i++) {
                String temp = noteList.get(i);
                String toCheck = temp.substring(0, temp.indexOf(" ") - 1);
                if (currentTime.getText().toString().equals(toCheck)) {
                    currentNotePos = i;
                    nla = new NoteListAdapter(noteList);
                    note.setAdapter(nla);
                    note.setSelection(currentNotePos);
                    break;
                }
            }
        }
        String temp = currentTime.getText().toString();
        String temp2 = finalTime.getText().toString();
        int cn = Integer.parseInt(Integer.parseInt(temp.substring(0, temp.indexOf(":"))) + "" +
                Integer.parseInt(temp.substring(temp.indexOf(":") + 1)));
        int fn = Integer.parseInt(Integer.parseInt(temp2.substring(0, temp2.indexOf(":"))) + "" +
                Integer.parseInt(temp2.substring(temp2.indexOf(":")+1)));
        if(cn >= 10)
        {
            skipLeft.setEnabled(true);
        }
        else
        {
            skipLeft.setEnabled(false);
        }
        if(cn >= fn-10)
        {
            skipRight.setEnabled(false);
        } else
        {
            skipRight.setEnabled(true);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.export)
        {
            if(export())
            {
                Toast.makeText(Player.this, "Exported Successfully to 'My Files/Audio Note/Notes/'", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(Player.this, "Export Unsuccessful!", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            //intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE );

        }
        return true;
    }

    private boolean export() {

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Audio Note" + File.separator + "Notes");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String outputFile = folder + File.separator + title.getText().toString() + ".txt";
        File n = new File(outputFile);
        exportedFile = n;
        try {
            PrintWriter pw = new PrintWriter(n);
            for(String note: noteList) {
                pw.println(note);
            }
            pw.close();
            //Toast.makeText(Player.this, "Exported Successfully!", Toast.LENGTH_LONG).show();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            File f = new File(uri.getPath());
            importFrom(f);
        }
    }

    private void importFrom(File f) {
        try {
            Scanner scanner = new Scanner(f);
            n = myPrefs.getString(uri, "");
            String temp = "";
            while(scanner.hasNextLine())
            {
                temp = scanner.nextLine();
                if(!n.contains(temp))
                {
                    n += temp + "\n";
                }
            }
            editor.putString(uri, n);
            editor.apply();
            Scanner in = new Scanner(n);
            noteList = new ArrayList<>();
            while (in.hasNextLine()) {
                noteList.add((in.nextLine().trim()));
            }
            noteList.remove("");
            Collections.sort((List) noteList);
            nla = new NoteListAdapter(noteList);
            note.setAdapter(nla);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}