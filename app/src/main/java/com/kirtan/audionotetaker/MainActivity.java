package com.kirtan.audionotetaker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {


    ListView note;
    String uri = "";
    String file = "";
    ArrayList<String> noteList;
    SharedPreferences myPrefs;
    ImageView back;
    TextView title;
    Button search;
    FileAdapter adp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.fab);
        setTitle("All Notes");


        search = (Button) findViewById(R.id.search_button);
        back = (ImageView) findViewById(R.id.back);
        back.setVisibility(View.INVISIBLE);
        note = (ListView) findViewById(R.id.listView2);
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        title = (TextView) findViewById(R.id.title);
        title.setVisibility(View.INVISIBLE);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose an option:");
                if (title.getVisibility() == View.INVISIBLE) {
                    builder.setItems(new String[]{"Select a File", "Create a Folder"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 1) {
                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                alertDialog.setTitle("Folder");
                                alertDialog.setMessage("Add a Folder:");

                                final EditText input = new EditText(MainActivity.this);
                                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);
                                alertDialog.setView(input);

                                alertDialog.setPositiveButton("Add",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                if (!myPrefs.getString("myFolders", "").contains(input.getText().toString())) {
                                                    SharedPreferences.Editor e = myPrefs.edit();
                                                    String temp = myPrefs.getString("myFolders", "");
                                                    String name = input.getText().toString();
                                                    name = name.substring(0,1).toUpperCase() + name.substring(1);
                                                    e.putString("myFolders", temp + name.trim()
                                                            + " (FOLDER)" + "\n");
                                                    e.commit();
                                                    noteList.add(name.trim() + " (FOLDER)");
                                                    Collections.sort((List) noteList);
                                                    //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
                                                    //        android.R.layout.simple_list_item_1, noteList);
                                                    adp = new FileAdapter();
                                                    note.setAdapter(adp);
                                                } else {
                                                    Toast.makeText(MainActivity.this,
                                                            "Folder with this name already exists!",
                                                            Toast.LENGTH_LONG).show();
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
                                Intent intent_upload = new Intent();
                                intent_upload.setType("audio/*");
                                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intent_upload, 1);
                            }
                        }
                    });
                } else {
                    builder.setItems(new String[]{"File"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent_upload = new Intent();
                            intent_upload.setType("audio/*");
                            intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intent_upload, 1);
                        }
                    });
                }
                builder.show();
            }
        });

        note.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                file = noteList.get(position);
                if (!file.contains("(FOLDER)")) {
                    if (!myPrefs.getString(file, "").equals("")) {
                        Intent intent = new Intent(MainActivity.this, Player.class);
                        intent.putExtra("file", file);
                        startActivity(intent);
                    }
                } else {
                    noteList = new ArrayList<>();
                    Scanner in = new Scanner(myPrefs.getString(file, ""));
                    while (in.hasNextLine()) {
                        String temp = in.nextLine();
                        noteList.add(temp.trim());
                    }
                    noteList.remove("");
                    title.setVisibility(View.VISIBLE);
                    back.setVisibility(View.VISIBLE);
                    file = file.replace(" (FOLDER)", "");
                    title.setText(file);
                    Collections.sort((List) noteList);
                    //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
                    //        android.R.layout.simple_list_item_1, noteList);
                    adp = new FileAdapter();
                    note.setAdapter(adp);
                }
            }
        });

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle(note.getItemAtPosition(position).toString());
                alertDialog.setMessage("Delete the file");
                alertDialog.setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor e = myPrefs.edit();
                                String temp = "";
                                if ((!note.getItemAtPosition(position).toString().contains(" (FOLDER)")) &&
                                        title.getVisibility() == View.INVISIBLE) {
                                    temp = myPrefs.getString("myFiles", "");
                                    String t = note.getItemAtPosition(position).toString() + "\n";
                                    e.remove(myPrefs.getString(
                                            note.getItemAtPosition(position).toString()
                                            , ""));  //deletes notes in the audio file
                                    temp = temp.replace(t, "");     //deletes the audio file from the app
                                    e.putString("myFiles", temp);
                                    e.remove(note.getItemAtPosition(position).toString());
                                    e.commit();
                                    noteList = new ArrayList<>();
                                    Scanner in = new Scanner(myPrefs.getString("myFiles", ""));
                                    while (in.hasNextLine()) {
                                        String x = in.nextLine();
                                        noteList.add(x.trim());
                                    }
                                    in = new Scanner(myPrefs.getString("myFolders", ""));
                                    while (in.hasNextLine()) {
                                        String x = in.nextLine();
                                        noteList.add(x.trim());
                                    }
                                } else if ((!note.getItemAtPosition(position).toString().contains(" (FOLDER)")) &&
                                        title.getVisibility() == View.VISIBLE) {
                                    temp = myPrefs.getString(title.getText().toString().trim() + " (FOLDER)", "");
                                    String t = noteList.get(position) + "\n";
                                    e.remove(myPrefs.getString(
                                            noteList.get(position)
                                            , ""));  //deletes notes in the audio file
                                    temp = temp.replace(t, "");     //deletes the audio file from the app
                                    e.putString(title.getText().toString().trim() + " (FOLDER)", temp);
                                    e.remove(noteList.get(position));
                                    e.commit();
                                    noteList = new ArrayList<>();
                                    Scanner in = new Scanner(myPrefs.getString(
                                            title.getText().toString().trim() + " (FOLDER)", ""));
                                    while (in.hasNextLine()) {
                                        String x = in.nextLine();
                                        noteList.add(x.trim());
                                    }
                                } else {
                                    String t = noteList.get(position) + "\n";
                                    Scanner in = new Scanner(myPrefs.getString(
                                            noteList.get(position), ""));
                                    while (in.hasNextLine()) {
                                        temp = in.nextLine();
                                        Log.d("Deleted", myPrefs.getString(temp
                                                , ""));
                                        e.remove(myPrefs.getString(temp
                                                , ""));  //deletes notes in the audio file
                                        Log.d("Deleted", temp);
                                        e.remove(temp);
                                        e.commit();

                                    }
                                    temp = myPrefs.getString("myFolders", "");
                                    temp = temp.replace(t, "");     //deletes the audio file from the app
                                    e.putString("myFolders", temp);
                                    e.remove(note.getItemAtPosition(position).toString().trim());
                                    e.commit();
                                    noteList = new ArrayList<>();
                                    in = new Scanner(myPrefs.getString("myFiles", ""));
                                    while (in.hasNextLine()) {
                                        String x = in.nextLine();
                                        noteList.add(x.trim());
                                    }
                                    in = new Scanner(myPrefs.getString("myFolders", ""));
                                    while (in.hasNextLine()) {
                                        String x = in.nextLine();
                                        noteList.add(x.trim());
                                    }
                                }


                                noteList.remove("");
                                noteList.remove("");
                                Collections.sort((List) noteList);
                                //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
                                //        android.R.layout.simple_list_item_1, noteList);
                                adp = new FileAdapter();
                                note.setAdapter(adp);
                                Toast.makeText(MainActivity.this, "Deleted...", Toast.LENGTH_LONG).show();
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                alertDialog.show();
                return true;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });
        update();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSearch();
            }
        });
    }

    private void update() {
        back.setVisibility(View.INVISIBLE);
        title.setVisibility(View.INVISIBLE);
        noteList = new ArrayList<>();
        Scanner in = new Scanner(myPrefs.getString("myFiles", ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine();
            noteList.add(temp.trim());
        }
        in = new Scanner(myPrefs.getString("myFolders", ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine();
            noteList.add(temp.trim());
        }
        noteList.remove("");
        noteList.remove("");
        Collections.sort((List) (noteList));
        //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
        //        android.R.layout.simple_list_item_1, noteList);
        adp = new FileAdapter();
        note.setAdapter(adp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {

                //the selected audio.

                Uri myUri = data.getData();
                uri = myUri.toString();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Create");
                alertDialog.setMessage("Name the file:");

                final EditText input = new EditText(MainActivity.this);
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                final String myFiles = myPrefs.getString("myFiles", "");

                alertDialog.setPositiveButton("Create",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    if(title.getVisibility() == View.INVISIBLE) {
                                        String name = input.getText().toString();
                                        name = name.substring(0,1).toUpperCase() + name.substring(1);
                                        if (!myPrefs.getString("myFiles", "").contains(name)) {
                                            file = input.getText().toString();
                                            SharedPreferences.Editor e = myPrefs.edit();

                                            e.putString("myFiles", myFiles + name + "\n");
                                            e.putString(name, uri);
                                            e.commit();
                                            noteList = new ArrayList<>();
                                            Scanner in = new Scanner(myPrefs.getString("myFiles", ""));
                                            while (in.hasNextLine()) {
                                                String temp = in.nextLine();
                                                noteList.add(temp.trim());
                                            }
                                            in = new Scanner(myPrefs.getString("myFolders", ""));
                                            while (in.hasNextLine()) {
                                                String temp = in.nextLine();
                                                noteList.add(temp.trim());
                                            }
                                            noteList.remove("");
                                            Collections.sort((List) noteList);
                                            //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
                                            //        android.R.layout.simple_list_item_1, noteList);
                                            adp = new FileAdapter();
                                            note.setAdapter(adp);
                                            Intent intent = new Intent(MainActivity.this, Player.class);
                                            intent.putExtra("file", name);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(MainActivity.this, "This file already exists. Name it differently!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    else
                                    {
                                        String name = input.getText().toString();
                                        name = name.substring(0,1).toUpperCase() + name.substring(1);
                                        String f = myPrefs.getString(title.getText().toString().trim() + " (FOLDER)", "");
                                        if (!myPrefs.getString(f, "").contains(name)) {
                                            file = input.getText().toString();
                                            SharedPreferences.Editor e = myPrefs.edit();
                                            e.putString(title.getText().toString().trim() + " (FOLDER)",
                                                    f + name + "\n");
                                            e.putString(name, uri);
                                            e.commit();
                                            noteList = new ArrayList<>();
                                            Scanner in = new Scanner(myPrefs.getString(
                                                    title.getText().toString().trim() + " (FOLDER)", ""));
                                            while (in.hasNextLine()) {
                                                String temp = in.nextLine();
                                                noteList.add(temp.trim());
                                            }
                                            Collections.sort((List) noteList);
                                            //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
                                            //        android.R.layout.simple_list_item_1, noteList);
                                            adp = new FileAdapter();
                                            note.setAdapter(adp);
                                            Intent intent = new Intent(MainActivity.this, Player.class);
                                            intent.putExtra("file", name);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(MainActivity.this, "This file already exists. Name it differently!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            }
                        });
                alertDialog.show();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void navigateToSearch() {
        Intent intent = new Intent(this, Search.class);
        startActivity(intent);
    }

    private class FileAdapter extends BaseAdapter
    {

        /**
         * gets the size of conversatrion list
         * @return size of conversation list
         */
        @Override
        public int getCount()
        {
            return noteList.size();
        }

        /**
         * gets the selected conversation
         * @param arg0 the position on the list
         * @return the conversation at a selected position
         */
        @Override
        public String getItem(int arg0)
        {
            return noteList.get(arg0);
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
            String c = getItem(pos);
            if (c.contains(" (FOLDER)"))
                v = getLayoutInflater().inflate(R.layout.folder_list, null);
            else
                v = getLayoutInflater().inflate(R.layout.file_list, null);

            TextView lbl = (TextView) v.findViewById(R.id.name);
            lbl.setText(noteList.get(pos));

            return v;
        }

    }

}
