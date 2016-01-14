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
    public View row;
    ArrayList<String> folderLists;
    ArrayList<String> fileLists;

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
                    builder.setItems(new String[]{"Open Audio File", "Start New Recording", "Create New Folder"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 2) {
                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                alertDialog.setTitle("Folder");
                                alertDialog.setMessage("Add a Folder:");

                                final EditText input = new EditText(MainActivity.this);
                                input.setSingleLine();
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
                                                System.out.println(myPrefs.getString("myFolders", ""));
                                                String name = input.getText().toString();
                                                if(name.length()>=1)
                                                {
                                                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                                }
                                                if (!myPrefs.getString("myFolders", "").contains(name.trim() + " (FOLDER)") &&
                                                        !name.trim().equals("")) {
                                                    SharedPreferences.Editor e = myPrefs.edit();
                                                    String temp = myPrefs.getString("myFolders", "");
                                                    e.putString("myFolders", temp + name.trim()
                                                            + " (FOLDER)" + "\n");
                                                    e.commit();
                                                    folderLists.add(name.trim());
                                                    Collections.sort((List) folderLists);
                                                    noteList = new ArrayList<>();
                                                    noteList.addAll(folderLists);
                                                    noteList.addAll(fileLists);
                                                    adp = new FileAdapter(noteList);
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
                            } else if(which == 0){
                                Intent intent_upload = new Intent();
                                intent_upload.setType("audio/*");
                                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intent_upload, 1);
                            }
                            else if(which == 1)
                            {
                                Intent intent = new Intent(MainActivity.this, RecordAudio.class);
                                startActivity(intent);
                            }
                        }
                    });
                } else {
                    builder.setItems(new String[]{"Select Audio File"}, new DialogInterface.OnClickListener() {
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
                if (position >= folderLists.size()) {
                    Intent intent = new Intent(MainActivity.this, Player.class);
                    intent.putExtra("file", file);
                    startActivity(intent);
                } else {
                    folderLists = new ArrayList<>();
                    noteList = new ArrayList<>();
                    Scanner in = new Scanner(myPrefs.getString(file + " (FOLDER)", ""));
                    while (in.hasNextLine()) {
                        String temp = in.nextLine();
                        noteList.add(temp.trim());
                    }
                    noteList.remove("");
                    title.setVisibility(View.VISIBLE);
                    back.setVisibility(View.VISIBLE);
                    title.setText(file);
                    setTitle(file);
                    Collections.sort((List) noteList);
                    adp = new FileAdapter(noteList);
                    note.setAdapter(adp);
                }
            }
        });

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle(note.getItemAtPosition(position).toString());
                if (position >= folderLists.size()) {
                    alertDialog.setItems(new String[]{"Rename", "Move to...", "Delete"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                rename(position);

                            } else if (which == 1) {
                                move(position);
                            } else {
                                delete(position);
                            }
                        }
                    });
                } else {
                    alertDialog.setItems(new String[]{"Rename", "Delete"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                rename(position);
                            } else {
                                delete(position);
                            }
                        }
                    });
                }
                alertDialog.show();
                return true;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitle("All Notes");
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


    /**
     * Moves the selected file to selected folder
     * @param position - moves the file at the given position
     */
    private void move(final int position) {
        final String currentName = noteList.get(position);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Move to...");
        final ListView listView = new ListView(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(lp);
        builder.setView(listView);
        final ArrayList<String> list =  new ArrayList<>();
        Scanner in = new Scanner(myPrefs.getString("myFolders", ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine();
            temp = temp.replace(" (FOLDER)", "");
            if(!myPrefs.getString(temp.trim() + " (FOLDER)", "").contains(currentName))
            {
                list.add(temp.trim());
            }
        }
        if(title.getVisibility() == View.VISIBLE)
        {
            if(!myPrefs.getString("myFiles", "").contains(currentName)) {
                list.add("All Notes");
            }
            list.remove(title.getText().toString().trim());
        }
        list.remove("");
        Collections.sort((List) list);
        listView.setAdapter(new FileAdapter(list));
        final String[] temp = {""};
        final String[] t = {""};
        final String[] t2 = {""};
        final Integer[] i = {0};

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int p, long id) {
                if (title.getVisibility() == View.INVISIBLE) {
                    temp[0] = myPrefs.getString("myFiles", "");
                    t[0] = noteList.get(position) + "\n";
                    temp[0] = temp[0].replace(t[0].toString(), "");
                    t2[0] = list.get(p) + " (FOLDER)";
                    i[0] = 1;
                } else if (title.getVisibility() == View.VISIBLE) {
                    temp[0] = myPrefs.getString(title.getText().toString() + " (FOLDER)", "");
                    t[0] = noteList.get(position) + "\n";
                    temp[0] = temp[0].replace(t[0].toString(), "");
                    if (!list.get(p).equals("All Notes")) {
                        t2[0] = list.get(p) + " (FOLDER)";
                    } else {
                        t2[0] = list.get(p);
                    }
                    i[0] = 1;
                }
                if (row != null) {
                    row.setBackgroundResource(R.color.back);
                }
                row = view;
                view.setBackgroundResource(R.color.colorPrimaryDark);
            }
        });

        builder.setPositiveButton("Move", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (title.getVisibility() == View.INVISIBLE &&
                        i[0] == 1) {
                    SharedPreferences.Editor e = myPrefs.edit();
                    e.putString("myFiles", temp[0]);
                    e.putString(t2[0].toString(), myPrefs.getString(t2[0].toString(), "") + t[0]);
                    e.commit();
                    update();
                }
                else if(title.getVisibility() == View.VISIBLE &&
                        i[0] == 1){
                    SharedPreferences.Editor e = myPrefs.edit();
                    e.putString(title.getText().toString() + " (FOLDER)", temp[0]);
                    if(!t2[0].equals("All Notes"))
                    {
                        e.putString(t2[0].toString(), myPrefs.getString(t2[0].toString(), "") + t[0]);
                    }
                    else
                    {
                        e.putString("myFiles", myPrefs.getString("myFiles", "") + t[0]);
                    }
                    e.commit();
                    update();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    /**
     * Renames the selected file or folder
     * @param position - renames the file/folder at the given position
     */
    private void rename(int position) {
        final String currentName = noteList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Rename");
        final EditText input = new EditText(MainActivity.this);
        input.setSingleLine();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        input.setText(currentName);

        builder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        String changedName = input.getText().toString().trim();
                        if(changedName.length()>=1)
                        {
                            changedName = changedName.substring(0,1).toUpperCase()
                                    + changedName.substring(1);
                        }
                        if (myPrefs.getString(currentName,"").contains("content:/") &&
                                title.getVisibility() == View.INVISIBLE &&
                                !myPrefs.getString("myFiles", "").contains(changedName.trim() + "\n")) {
                            SharedPreferences.Editor e = myPrefs.edit();
                            String temp = myPrefs.getString("myFiles", "");
                            String t = "\n" + currentName + "\n";
                            if(!temp.contains(t))
                            {
                                t = currentName + "\n";
                                temp = temp.replace(t, changedName + "\n");
                            }
                            else
                            {
                                temp = temp.replace(t, "\n" + changedName + "\n");
                            }
                            e.putString(changedName, myPrefs.getString(currentName, ""));

                            e.putString("myFiles", temp);
                            e.remove(currentName);
                            e.commit();
                            update();
                        }
                        else if(!myPrefs.getString(currentName,"").contains("content:/") &&
                                !myPrefs.getString("myFolders", "").contains(changedName.trim()
                                        + " (FOLDER)" + "\n") &&
                                title.getVisibility() == View.INVISIBLE)
                        {
                            SharedPreferences.Editor e = myPrefs.edit();
                            String temp = myPrefs.getString("myFolders", "");
                            String t = "\n" + currentName + " (FOLDER)" + "\n";
                            if(!temp.contains(t))
                            {
                                t = currentName + " (FOLDER)" + "\n";
                                temp = temp.replace(t, changedName + " (FOLDER)" + "\n");
                            }
                            else
                            {
                                temp = temp.replace(t, "\n" + changedName + " (FOLDER)" + "\n");
                            }
                            e.putString(changedName + " (FOLDER)", myPrefs.getString(currentName + " (FOLDER)", ""));

                            e.putString("myFolders", temp);
                            e.remove(currentName + " (FOLDER)");
                            e.commit();
                            update();
                        }
                        else if(!myPrefs.getString(title.getText().toString().trim() + " (FOLDER)", "").contains(
                                changedName.trim() + "\n") &&
                                title.getVisibility() == View.VISIBLE){
                            SharedPreferences.Editor e = myPrefs.edit();
                            String temp = myPrefs.getString(title.getText().toString()+ " (FOLDER)",
                                    "");
                            String t = "\n" + currentName + "\n";
                            if(!temp.contains(t))
                            {
                                t = currentName + "\n";
                                temp = temp.replace(t, changedName + "\n");
                            }
                            else
                            {
                                temp = temp.replace(t, "\n" + changedName + "\n");
                            }
                            e.putString(changedName, myPrefs.getString(currentName, ""));

                            e.putString(title.getText().toString() + " (FOLDER)", temp);
                            e.remove(currentName);
                            e.commit();
                            update();
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this,
                                    "A file/folder with this name already exists!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        dialog.cancel();
                    }
                });

        builder.show();
    }


    /**
     * Deletes the file or the folder
     * @param position deleted the file/folder at the given position
     */
    private void delete(int position) {
        SharedPreferences.Editor e = myPrefs.edit();
        String temp;
        if ((myPrefs.getString(note.getItemAtPosition(position).toString(), "").contains("content:/")) &&
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
            update();
        } else if ((myPrefs.getString(note.getItemAtPosition(position).toString(), "")
                .contains("content:/")) &&
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
            update();
        } else {
            String t = noteList.get(position) + " (FOLDER)" + "\n";
            Scanner in = new Scanner(myPrefs.getString(
                    noteList.get(position) + " (FOLDER)", ""));
            while (in.hasNextLine()) {
                temp = in.nextLine();
                Log.d("Deleted", myPrefs.getString(temp
                        , ""));
                e.remove(myPrefs.getString(temp
                        , ""));  //deletes notes in the audio file
                Log.d("Deleted", temp);
                e.remove(temp);  //delete the audio file
                e.commit();
            }
            temp = myPrefs.getString("myFolders", "");
            temp = temp.replace(t, "");     //deletes the folder from the app
            e.putString("myFolders", temp);
            e.remove(note.getItemAtPosition(position).toString() + " (FOLDER)");
            e.commit();
            update();
        }
        update();
        Toast.makeText(MainActivity.this, "Deleted...", Toast.LENGTH_LONG).show();
    }


    /**
     * Updates the listView
     */
    private void update() {
        back.setVisibility(View.INVISIBLE);
        title.setVisibility(View.INVISIBLE);
        noteList = new ArrayList<>();
        fileLists = new ArrayList<>();
        folderLists = new ArrayList<>();
        Scanner in = new Scanner(myPrefs.getString("myFiles", ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine();
            fileLists.add(temp.trim());
        }
        in = new Scanner(myPrefs.getString("myFolders", ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine();
            temp = temp.replace(" (FOLDER)", "");
            folderLists.add(temp.trim());
        }
        fileLists.remove("");
        folderLists.remove("");
        Collections.sort((List) (fileLists));
        Collections.sort((List) (folderLists));
        noteList.addAll(folderLists);
        noteList.addAll(fileLists);
        adp = new FileAdapter(noteList);
        note.setAdapter(adp);
    }


    /**
     * After the selection of the audio file
     * @param requestCode - the requestcode
     * @param resultCode - the resultcode
     * @param data - the received data
     */
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
                input.setSingleLine();
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

                                String name = input.getText().toString();
                                if (name.length() >= 1) {
                                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                }
                                if (title.getVisibility() == View.INVISIBLE &&
                                        !input.getText().toString().trim().equals("")) {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    if (!myPrefs.getString("myFiles", "").contains(name)) {
                                        file = input.getText().toString();
                                        SharedPreferences.Editor e = myPrefs.edit();
                                        e.putString("myFiles", myFiles + name + "\n");
                                        e.putString(name, uri);
                                        e.commit();
                                        update();
                                        Intent intent = new Intent(MainActivity.this, Player.class);
                                        intent.putExtra("file", name);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this, "This file already exists. Name it differently!", Toast.LENGTH_LONG).show();
                                    }
                                } else if (title.getVisibility() == View.VISIBLE &&
                                        !name.trim().equals("")) {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    String f = myPrefs.getString(title.getText().toString().trim() + " (FOLDER)", "");
                                    if (!myPrefs.getString(f, "").contains(name)) {
                                        file = input.getText().toString();
                                        SharedPreferences.Editor e = myPrefs.edit();
                                        e.putString(title.getText().toString().trim() + " (FOLDER)",
                                                f + name + "\n");
                                        e.putString(name, uri);
                                        e.commit();
                                        update();
                                        Intent intent = new Intent(MainActivity.this, Player.class);
                                        intent.putExtra("file", name);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this, "This file already exists. Name it differently!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Sorry. Cannot create a file with this name!", Toast.LENGTH_LONG).show();
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


    /**
     * Navigates to search
     */
    private void navigateToSearch() {
        Intent intent = new Intent(this, Search.class);
        startActivity(intent);
    }


    /**
     * Private Class for listView
     */
    private class FileAdapter extends BaseAdapter
    {

        ArrayList<String> s;
        protected FileAdapter(ArrayList<String> s1)
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
            String c = getItem(pos);
            if (pos < folderLists.size())
                v = getLayoutInflater().inflate(R.layout.folder_list, null);
            else
                v = getLayoutInflater().inflate(R.layout.file_list, null);

            TextView lbl = (TextView) v.findViewById(R.id.name);
            lbl.setText(s.get(pos));

            return v;
        }

    }

}
