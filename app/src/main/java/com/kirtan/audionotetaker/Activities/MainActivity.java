package com.kirtan.audionotetaker.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kirtan.audionotetaker.Fragments.MenuFragment;
import com.kirtan.audionotetaker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements MenuFragment.OnClickedListener{


    ListView note;
    String uri = "", file = "";
    SharedPreferences myPrefs, favs, settings;
    SharedPreferences.Editor editor,favEditior, setEditor;
    ImageView back, favorite, menu, search;
    TextView title;
    Button add;
    FileAdapter adp;
    public View row;
    ArrayList<String> folderLists, fileLists, recordLists, noteList;
    boolean isFolderOpen;
    final String MY_FILES = "myFiles",
            MY_FOLDERS = "myFolders",
            FAVS = "favorites",
            SETTINGS = "settings";
    final int PERMISSION_REQ = 0;
    RelativeLayout relativeLayout;
    float x1,y1,x2,y2;
    private MenuFragment menuFragment;
    private FragmentManager fragmentManager;
    private boolean isMenuOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        add = (Button) findViewById(R.id.fab);
        add.setVisibility(View.VISIBLE);
        search = (ImageView) findViewById(R.id.search_button);
        back = (ImageView) findViewById(R.id.back);
        back.setVisibility(View.INVISIBLE);
        note = (ListView) findViewById(R.id.listView2);
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();
        favs = getSharedPreferences(FAVS, MODE_PRIVATE);
        favEditior = favs.edit();
        settings = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        setEditor = settings.edit();
        setEditor.putString("menu", "All Files");
        setEditor.apply();
        title = (TextView) findViewById(R.id.title);
        title.setText("All Notes");
        title.setVisibility(View.INVISIBLE);
        relativeLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        menu = (ImageView) findViewById(R.id.menu_button);
        favorite = (ImageView) findViewById(R.id.favorite);
        isFolderOpen = false;
        isMenuOpen = false;

        if(checkAndRequestPermissions()) {
            accessComponents();
        }
    }

    private void accessComponents() {
        assert add != null;
        add.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose an option:");
                if (title.getVisibility() == View.INVISIBLE) {
                    builder.setItems(new String[]{"Open Audio File", "Start New Recording", "Create New Folder"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 2) {
                                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                        alertDialog.setTitle("Add a Folder:");
                                        final EditText input = new EditText(MainActivity.this);
                                        input.setSingleLine();
                                        input.setBackgroundResource(android.R.drawable.editbox_background_normal);
                                        input.setHint("Name of the folder");
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
                                                        String name = input.getText().toString();
                                                        if (name.length() >= 1) {
                                                            name = generateName(name);
                                                        }
                                                        if (!myPrefs.getString(MY_FOLDERS, "").contains(name.trim() + " (FOLDER)") &&
                                                                !name.trim().equals("") &&
                                                                !name.trim().equals("All Notes")) {
                                                            String temp = myPrefs.getString(MY_FOLDERS, "");
                                                            editor.putString(MY_FOLDERS, temp + name.trim()
                                                                    + " (FOLDER)" + "\n");
                                                            editor.apply();
                                                            folderLists.add(name.trim());
                                                            Collections.sort((List) folderLists);
                                                            noteList = new ArrayList<>();
                                                            noteList.addAll(folderLists);
                                                            noteList.addAll(fileLists);
                                                            noteList.addAll(recordLists);
                                                            adp = new FileAdapter(noteList);
                                                            note.setAdapter(adp);
                                                        } else {
                                                            Snackbar snackbar = Snackbar.make(v ,
                                                                    "Cannot create a folder with this name!",
                                                                    Snackbar.LENGTH_LONG);
                                                            snackbar.show();
                                                            /*Toast.makeText(MainActivity.this,
                                                                    "Cannot create a folder with this name!",
                                                                    Toast.LENGTH_LONG).show();*/
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
                                    } else if (which == 0) {
                                        openAudioFile();
                                    } else if (which == 1) {
                                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                        alertDialog.setTitle("Recording File");

                                        final EditText input = new EditText(MainActivity.this);
                                        input.setSingleLine();
                                        input.setBackgroundResource(android.R.drawable.editbox_background_normal);
                                        input.setHint("Name of the file");
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
                                                        String name = input.getText().toString().trim();
                                                        if (name.length() >= 1) {
                                                            name = generateName(name);
                                                        }
                                                        if (!name.trim().equals("") &&
                                                                checkEveryFolder(name+"\n")) {
                                                            Intent intent = new Intent(MainActivity.this, RecordAudio.class);
                                                            intent.putExtra("fileName", name);
                                                            intent.putExtra("folderName", "All Notes");
                                                            startActivity(intent);
                                                        } else {
                                                            Toast.makeText(MainActivity.this,
                                                                    "A file/folder with this name already exists!",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
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
                                }
                            });
                } else {
                    builder.setItems(new String[]{"Open Audio File", "Start New Recording"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                openAudioFile();
                            } else if (which == 1) {
                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                alertDialog.setTitle("Recording File");

                                final EditText input = new EditText(MainActivity.this);
                                input.setSingleLine();
                                input.setBackgroundResource(android.R.drawable.editbox_background_normal);
                                input.setHint("Name of the file");
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
                                                String name = input.getText().toString().trim();
                                                if (name.length() >= 1) {
                                                    name = generateName(name);
                                                }
                                                if (!name.trim().equals("") &&
                                                        checkEveryFolder(name+"\n")) {
                                                    Intent intent = new Intent(MainActivity.this, RecordAudio.class);
                                                    intent.putExtra("fileName", name);
                                                    intent.putExtra("folderName", title.getText().toString().trim());
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(MainActivity.this,
                                                            "A file/folder with this name already exists!",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

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
                if(position >= folderLists.size())
                {
                    navigateTo(file.trim());
                }
                else {
                    updateFolder();
                }
            }
        });

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle(note.getItemAtPosition(position).toString());
                if(settings.getString("menu", "All Files").equals("All Files")) {
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
                    } else if (position < folderLists.size()) {
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
                }
                else{
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
                if(!isMenuOpen) {
                    title.setText("All Notes");
                    menu.setVisibility(View.VISIBLE);
                    update();
                }
                else{
                    hideFragment();
                }
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSearch();
            }
        });
        update();

        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent touchevent) {
                switch (touchevent.getAction())
                {
                    // when user first touches the screen we get x and y coordinate
                    case MotionEvent.ACTION_DOWN:
                    {
                        x1 = touchevent.getX();
                        y1 = touchevent.getY();
                    }
                    case MotionEvent.ACTION_UP:
                    {
                        x2 = touchevent.getX();
                        y2 = touchevent.getY();

                        if (x1 < x2)
                        {
                            showFragment();
                        }
                        break;
                    }
                }
                return true;
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment();
            }
        });
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
                if (isUniqueAudio(uri)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Create");
                    final EditText input = new EditText(MainActivity.this);
                    input.setSingleLine();
                    input.setBackgroundResource(android.R.drawable.editbox_background_normal);
                    input.setHint("Name of the file");
                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    alertDialog.setView(input);
                    final String myFiles = myPrefs.getString(MY_FILES, "");
                    alertDialog.setPositiveButton("Create",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = input.getText().toString();
                                    if (name.length() >= 1) {
                                        name = generateName(name);
                                    }
                                    if (title.getVisibility() == View.INVISIBLE &&
                                            !name.trim().equals("")
                                            && checkEveryFolder(name + "\n")) {
                                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                        if (!myFiles.contains(name)) {
                                            file = input.getText().toString();
                                            editor.putString(MY_FILES, myFiles + name + "\n");
                                            editor.putString(name, uri);
                                            editor.apply();
                                            update();
                                            navigateTo(name);
                                        } else {
                                            Toast.makeText(MainActivity.this, "This file already exists. Name it differently!", Toast.LENGTH_LONG).show();
                                        }
                                    } else if (title.getVisibility() == View.VISIBLE &&
                                            !name.trim().equals("") &&
                                            checkEveryFolder(name + "\n")) {
                                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                        String f = myPrefs.getString(title.getText().toString().trim() + " (FOLDER)", "");
                                        if (!f.contains(name)) {
                                            file = input.getText().toString();
                                            editor.putString(title.getText().toString().trim() + " (FOLDER)",
                                                    f + name + "\n");
                                            editor.putString(name, uri);
                                            editor.apply();
                                            update();
                                            navigateTo(name);
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
                else {
                    Toast.makeText(this, "This Audio File Already Exists!", Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @NonNull
    private String generateName(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    private void showFragment() {
        isMenuOpen = true;
        back.setVisibility(View.VISIBLE);
        menu.setVisibility(View.INVISIBLE);
        search.setVisibility(View.INVISIBLE);
        add.setVisibility(View.INVISIBLE);
        fragmentManager = getFragmentManager();
        menuFragment = new MenuFragment();
        fragmentManager.beginTransaction().
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                add(R.id.mainLayout, menuFragment).
                commit();
    }

    private void hideFragment() {
        isMenuOpen = false;
        back.setVisibility(View.INVISIBLE);
        menu.setVisibility(View.VISIBLE);
        add.setVisibility(View.VISIBLE);
        search.setVisibility(View.VISIBLE);
        fragmentManager.beginTransaction().
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                remove(menuFragment)
                .commit();
    }

    @Override
    public void onCloseClicked() {
        hideFragment();
    }

    @Override
    public void onMenuOptionClicked(String s) {
        hideFragment();
        makeNewArrayLists();
        if(s.equals("All Files")){
            update();
        }
        else if(s.equals("Audio Files")){
            getAudioFiles();
            updateListView();
        }
        else if(s.equals("Recordings")){
            getRecordedFiles();
            updateListView();
        }
        else{
            getFavoriteFiles();
            updateListView();
        }
    }

    private void getAudioFiles() {
        String files = myPrefs.getString(MY_FILES, "");
        String folders = myPrefs.getString(MY_FOLDERS, "");
        Scanner in = new Scanner(files);
        while(in.hasNextLine()){
            String temp = in.nextLine().trim();
            if(!myPrefs.getString(temp, "").contains("file:/") &&
                    !temp.equals("")){
                fileLists.add(temp);
            }
        }
        in = new Scanner(folders);
        while(in.hasNextLine()){
            String temp = myPrefs.getString(in.nextLine(), "");
            Scanner t = new Scanner(temp);
            while(t.hasNextLine()){
                String te = t.nextLine().trim();
                if(!myPrefs.getString(te, "").contains("file:/") &&
                        !temp.equals("")){
                    fileLists.add(te);
                }
            }
        }
    }

    private void getRecordedFiles() {
        String files = myPrefs.getString(MY_FILES, "");
        String folders = myPrefs.getString(MY_FOLDERS, "");
        Scanner in = new Scanner(files);
        while(in.hasNextLine()){
            String temp = in.nextLine().trim();
            if(myPrefs.getString(temp, "").contains("file:/") &&
                    !temp.equals("")) {
                recordLists.add(temp);
            }
        }
        in = new Scanner(folders);
        while(in.hasNextLine()){
            String temp = myPrefs.getString(in.nextLine(), "");
            Scanner t = new Scanner(temp);
            while(t.hasNextLine()){
                String te = t.nextLine().trim();
                if(myPrefs.getString(te, "").contains("file:/") &&
                        !temp.equals("")){
                    recordLists.add(te);
                }
            }
        }
    }

    private void getFavoriteFiles() {
        String files = myPrefs.getString(MY_FILES, "");
        String folders = myPrefs.getString(MY_FOLDERS, "");
        String favorites = favs.getString("favs", "");
        Scanner in = new Scanner(files);
        while(in.hasNextLine()){
            String temp = in.nextLine().trim();
            if(myPrefs.getString(temp, "").contains("file:/") &&
                    favorites.contains(temp + "||") &&
                    !temp.equals("")){
                recordLists.add(temp);
            }
            else if(favorites.contains(temp + "||") &&
                    !temp.equals("")){
                fileLists.add(temp);
            }
        }
        in = new Scanner(folders);
        while(in.hasNextLine()){
            String temp = myPrefs.getString(in.nextLine(), "");
            Scanner t = new Scanner(temp);
            while(t.hasNextLine()){
                String te = t.nextLine().trim();
                if(myPrefs.getString(te, "").contains("file:/") &&
                        favorites.contains(te + "||") &&
                        !te.equals("")){
                    recordLists.add(te);
                }
                else if(favorites.contains(te + "||") &&
                        !te.equals("")){
                    fileLists.add(te);
                }
            }
        }
    }

    private void makeNewArrayLists() {
        noteList = new ArrayList<>();
        fileLists = new ArrayList<>();
        folderLists = new ArrayList<>();
        recordLists = new ArrayList<>();
    }

    private void updateFolder() {
        isFolderOpen = true;
        makeNewArrayLists();
        Scanner in = new Scanner(myPrefs.getString(file + " (FOLDER)", ""));
        while (in.hasNextLine()) {
            String temp = in.nextLine().trim();
            if (myPrefs.getString(temp, "").contains("file:/")) {
                recordLists.add(temp);
            } else {
                fileLists.add(temp);
            }
        }
        title.setVisibility(View.VISIBLE);
        back.setVisibility(View.VISIBLE);
        menu.setVisibility(View.INVISIBLE);
        title.setText(file);
        updateListView();
    }

    /**
     * Updates the listView
     */
    private void update() {
        isFolderOpen = false;
        back.setVisibility(View.INVISIBLE);
        title.setVisibility(View.INVISIBLE);
        menu.setVisibility(View.VISIBLE);
        makeNewArrayLists();
        Scanner in = new Scanner(myPrefs.getString(MY_FILES, ""));
        while(in.hasNextLine()) {
            String temp = in.nextLine().trim();
            if(myPrefs.getString(temp, "").contains("file:/"))
            {
                recordLists.add(temp);
            }
            else {
                fileLists.add(temp);
            }
        }
        in = new Scanner(myPrefs.getString(MY_FOLDERS, ""));
        while(in.hasNextLine()) {
            String temp = in.nextLine();
            temp = temp.replace(" (FOLDER)", "");
            if(!temp.trim().equals("")){
                folderLists.add(temp.trim());
            }
        }
        updateListView();
    }

    private void updateListView() {
        Collections.sort((List) fileLists);
        Collections.sort((List) folderLists);
        Collections.sort((List) recordLists);
        fileLists.remove("");
        folderLists.remove("");
        recordLists.remove("");
        noteList.addAll(folderLists);
        noteList.addAll(fileLists);
        noteList.addAll(recordLists);
        adp = new FileAdapter(noteList);
        note.setAdapter(adp);
    }

    /**
     * Opens audio chooser
     */
    private void openAudioFile() {
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, 1);
    }


    /**
     * Moves the selected file to selected folder
     * @param position - moves the file to a different folder
     */
    private void move(final int position) {
        final String currentName = noteList.get(position);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Move to...");
        builder.setCancelable(false);
        final ListView listView = new ListView(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(lp);
        builder.setView(listView);
        final ArrayList<String> list =  new ArrayList<>();
        Scanner in = new Scanner(myPrefs.getString(MY_FOLDERS, ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine();
            temp = temp.replace(" (FOLDER)", "");
            if(!myPrefs.getString(temp.trim() + " (FOLDER)", "").contains(currentName+"\n"))
            {
                list.add(temp.trim());
            }
        }
        if(title.getVisibility() == View.VISIBLE)
        {
            if(!myPrefs.getString(MY_FILES, "").contains(currentName+"\n")) {
                list.add("All Notes");
            }
            list.remove(title.getText().toString().trim());
        }
        list.remove("");
        Collections.sort((List) list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        final String[] temp = {""};
        final String[] t = {""};
        final String[] t2 = {""};
        final Integer[] i = {0};

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int p, long id) {
                if (title.getVisibility() == View.INVISIBLE) {
                    temp[0] = myPrefs.getString(MY_FILES, "");
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
                    editor.putString(MY_FILES, temp[0]);
                    editor.putString(t2[0].toString(), myPrefs.getString(t2[0].toString(), "") + t[0]);
                    editor.apply();
                    update();
                } else if (title.getVisibility() == View.VISIBLE &&
                        i[0] == 1) {
                    editor.putString(title.getText().toString() + " (FOLDER)", temp[0]);
                    if (!t2[0].equals("All Notes")) {
                        editor.putString(t2[0].toString(), myPrefs.getString(t2[0].toString(), "") + t[0]);
                    } else {
                        editor.putString(MY_FILES, myPrefs.getString(MY_FILES, "") + t[0]);
                    }
                    editor.apply();
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
    private void rename(final int position) {
        final String currentName = noteList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Rename");
        builder.setCancelable(false);
        final EditText input = new EditText(MainActivity.this);
        input.setSingleLine();
        input.setBackgroundResource(android.R.drawable.editbox_background_normal);
        input.setHint("Name of the file");
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
                    if (changedName.length() >= 1) {
                        changedName = generateName(changedName);
                        if (settings.getString("menu", "All Files").equals("All Files")) {
                            if (position >= folderLists.size() && position < folderLists.size() +
                                    fileLists.size() +
                                    recordLists.size() &&
                                    title.getVisibility() == View.INVISIBLE &&
                                    checkEveryFolder(changedName + "\n")) {
                                renameInAllFiles(currentName, changedName);
                            } else if (position < folderLists.size() &&
                                    title.getVisibility() == View.INVISIBLE &&
                                    !myPrefs.getString(MY_FOLDERS, "").contains(changedName + " (FOLDER)")) {
                                renameFolder(currentName, changedName);
                            } else if (!myPrefs.getString(title.getText().toString().trim() + " (FOLDER)", "").contains(
                                    changedName.trim() + "\n") &&
                                    title.getVisibility() == View.VISIBLE &&
                                    checkEveryFolder(changedName + "\n")) {
                                renameInFolder(title.getText().toString(), currentName, changedName);
                            }
                        } else {
                            if (myPrefs.getString(MY_FILES, "").contains(currentName + "\n") &&
                                    checkEveryFolder(changedName + "\n")) {
                                renameInAllFiles(currentName, changedName);
                            } else if (checkEveryFolder(changedName + "\n")) {
                                String folders = myPrefs.getString(MY_FOLDERS, "");
                                Scanner in = new Scanner(folders);
                                while (in.hasNextLine()) {
                                    String folder = in.nextLine();
                                    if (myPrefs.getString(folder, "").contains(currentName + "\n")) {
                                        folder = folder.replace(" (FOLDER)", "");
                                        renameInFolder(folder, currentName, changedName);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Invalid file/folder name!",
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

    private void renameInFolder(String folderName, String currentName, String changedName) {
        String temp = myPrefs.getString(folderName + " (FOLDER)",
                "");
        String t = "\n" + currentName + "\n";
        if (!temp.contains(t)) {
            t = currentName + "\n";
            temp = temp.replace(t, changedName + "\n");
        } else {
            temp = temp.replace(t, "\n" + changedName + "\n");
        }
        favEditior.putString("favs", favs.getString("favs", "").replace(currentName+"||", changedName+"||"));
        favEditior.apply();
        editor.putString(changedName, myPrefs.getString(currentName, ""));
        editor.putString(folderName + " (FOLDER)", temp);
        editor.remove(currentName);
        editor.apply();
        getUpdated();
    }

    private void renameFolder(String currentName, String changedName) {
        String temp = myPrefs.getString(MY_FOLDERS, "");
        String t = "\n" + currentName + " (FOLDER)" + "\n";
        if (!temp.contains(t)) {
            t = currentName + " (FOLDER)" + "\n";
            temp = temp.replace(t, changedName + " (FOLDER)" + "\n");
        } else {
            temp = temp.replace(t, "\n" + changedName + " (FOLDER)" + "\n");
        }
        editor.putString(changedName + " (FOLDER)", myPrefs.getString(currentName + " (FOLDER)", ""));
        editor.putString(MY_FOLDERS, temp);
        editor.remove(currentName + " (FOLDER)");
        editor.apply();
        update();
    }

    private void renameInAllFiles(String currentName, String changedName) {
        String temp = myPrefs.getString(MY_FILES, "");
        String t = "\n" + currentName + "\n";
        if (!temp.contains(t)) {
            t = currentName + "\n";
            temp = temp.replace(t, changedName + "\n");
        } else {
            temp = temp.replace(t, "\n" + changedName + "\n");
        }
        favEditior.putString("favs", favs.getString("favs", "").replace(currentName+"||", changedName+"||"));
        favEditior.apply();
        editor.putString(changedName, myPrefs.getString(currentName, ""));
        editor.putString(MY_FILES, temp);
        editor.remove(currentName);
        editor.apply();
        getUpdated();
    }


    /**
     * Deletes the file or the folder
     * @param position deleted the file/folder at the given position
     */
    private void delete(int position) {
        String temp;
        if(settings.getString("menu", "All Files").equals("All Files")) {
            if (position >= folderLists.size() &&
                    position < folderLists.size() + fileLists.size() + recordLists.size() &&
                    title.getVisibility() == View.INVISIBLE) {
                deleteInAllFiles(position);
            } else if (title.getVisibility() == View.VISIBLE) {
                deleteInFolder(title.getText().toString().trim(), position);
            } else if (position < folderLists.size()) {
                deleteFolder(position);
            }
        }
        else{
            if(myPrefs.getString(MY_FILES, "").contains(noteList.get(position) + "\n")){
                deleteInAllFiles(position);
            }
            else{
                String folders = myPrefs.getString(MY_FOLDERS, "");
                String currentName = noteList.get(position);
                Scanner in = new Scanner(folders);
                while (in.hasNextLine()) {
                    String folder = in.nextLine();
                    if (myPrefs.getString(folder, "").contains(currentName + "\n")) {
                        folder = folder.replace(" (FOLDER)", "");
                        deleteInFolder(folder, position);
                        break;
                    }
                }
            }
        }
        Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_LONG).show();
    }

    private void deleteFolder(int position) {
        String temp;
        String t = noteList.get(position) + " (FOLDER)" + "\n";
        Scanner in = new Scanner(myPrefs.getString(
                noteList.get(position) + " (FOLDER)", ""));
        while (in.hasNextLine()) {
            temp = in.nextLine();
            Log.d("Deleted", myPrefs.getString(temp
                    , ""));
            editor.remove(myPrefs.getString(temp
                    , ""));  //deletes notes in the audio file
            Log.d("Deleted", temp);
            editor.remove(temp);  //delete the audio file
            editor.apply();
        }
        temp = myPrefs.getString(MY_FOLDERS, "");
        temp = temp.replace(t, "");     //deletes the folder from the app
        editor.putString(MY_FOLDERS, temp);
        editor.remove(note.getItemAtPosition(position).toString() + " (FOLDER)");
        editor.apply();
        update();
    }

    private void deleteInFolder(String folderName, int position) {
        String temp;
        temp = myPrefs.getString(folderName + " (FOLDER)", "");
        String t = noteList.get(position) + "\n";
        favEditior.putString("favs", favs.getString("favs", "").replace(note.getItemAtPosition(position)+"||", ""));
        favEditior.apply();
        editor.remove(myPrefs.getString(
                noteList.get(position)
                , ""));  //deletes notes in the audio file
        temp = temp.replace(t, "");     //deletes the audio file from the app
        editor.putString(folderName + " (FOLDER)", temp);
        editor.remove(noteList.get(position));
        editor.apply();
        getUpdated();
    }

    private void deleteInAllFiles(int position) {
        String temp;
        temp = myPrefs.getString(MY_FILES, "");
        String t = note.getItemAtPosition(position).toString() + "\n";
        favEditior.putString("favs", favs.getString("favs", "").replace(note.getItemAtPosition(position)+"||", ""));
        favEditior.apply();
        editor.remove(myPrefs.getString(
                note.getItemAtPosition(position).toString()
                , ""));  //deletes notes in the audio file
        temp = temp.replace(t, "");     //deletes the audio file from the app
        editor.putString(MY_FILES, temp);
        editor.remove(note.getItemAtPosition(position).toString());
        editor.apply();
        getUpdated();
    }

    private boolean isUniqueAudio(String u) {
        boolean b = true;
        Map<String, ?> keys = myPrefs.getAll();
        for (Map.Entry<String, ?> entryKey : keys.entrySet()) {
            if (entryKey.getKey().equals(u) || entryKey.getValue().equals(u)){
                b = false;
                break;
            }
        }
        return b;
    }

    private boolean checkEveryFolder(String name) {
        boolean b = true;
        for(String x: myPrefs.getString(MY_FOLDERS,"").split("\n"))
        {
            if(!x.trim().equals("")){
                String z = myPrefs.getString(x.trim(),"");
                if(myPrefs.getString(x.trim(),"").contains(name)){
                    b = false;
                    break;
                }
            }
        }
        for(String x: myPrefs.getString(MY_FILES,"").split("\n"))
        {
            if(!x.trim().equals("")) {
                if (x.trim().equals(name.replace("\n", ""))) {
                    b = false;
                    break;
                }
            }
        }
        return b;
    }

    private void navigateTo(String name) {
        isFolderOpen = false;
        Intent intent = new Intent(MainActivity.this, Player.class);
        intent.putExtra("file", name);
        startActivity(intent);
    }

    /**
     * Navigates to search
     */
    private void navigateToSearch() {
        Intent intent = new Intent(this, Search.class);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        accessComponents();
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                            showDialogOK("Record Audio & Read and Write External Storage Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    finish();
                                                    System.exit(0);
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
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
        public View getView(final int pos, View v, ViewGroup arg2)
        {
            if (pos < folderLists.size())
                    v = getLayoutInflater().inflate(R.layout.list_folder, null);
            else if (myPrefs.getString(noteList.get(pos), "").contains("file:/")) {
                v = getLayoutInflater().inflate(R.layout.list_recordings, null);
                final ImageView fav = (ImageView) v.findViewById(R.id.favorite);
                String list = favs.getString("favs", "");
                if (list.contains(s.get(pos) + "||")) {
                    fav.setImageResource(R.mipmap.fullstar);
                } else {
                    fav.setImageResource(R.mipmap.emptystar);
                }
                fav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleFav(fav, s.get(pos));
                    }
                });
            }
            else if (pos < folderLists.size() + fileLists.size()) {
                v = getLayoutInflater().inflate(R.layout.list_file, null);
                final ImageView fav = (ImageView) v.findViewById(R.id.favorite);
                String list = favs.getString("favs", "");
                if (list.contains(s.get(pos) + "||")) {
                    fav.setImageResource(R.mipmap.fullstar);
                } else {
                    fav.setImageResource(R.mipmap.emptystar);
                }
                fav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleFav(fav, s.get(pos));
                    }
                });
            }
            if(v != null) {
                TextView lbl = (TextView) v.findViewById(R.id.note);
                lbl.setText(s.get(pos));
            }
            return v;
        }

        public void toggleFav(ImageView fav, String s){
            String list = favs.getString("favs", "");
            SharedPreferences.Editor edit = favs.edit();
            if (fav.getDrawable().getConstantState().equals(getResources().getDrawable(R.mipmap.fullstar).getConstantState())) {
                fav.setImageResource(R.mipmap.emptystar);
                list = list.replace(s + "||", "");
            } else {
                fav.setImageResource(R.mipmap.fullstar);
                list += s + "||";
            }
            edit.putString("favs", list);
            edit.commit();
        }

    }

    @Override
    public void onBackPressed() {
        if(menu.getVisibility() == View.INVISIBLE && title.getVisibility() == View.INVISIBLE)
        {
            hideFragment();
            menu.setVisibility(View.VISIBLE);
            add.setVisibility(View.VISIBLE);
            search.setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction().
                    setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                    remove(menuFragment)
                    .commit();
        }
        else {
            if (title.getVisibility() == View.VISIBLE) {
                update();
                menu.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUpdated();
    }

    private void getUpdated() {
        makeNewArrayLists();
        switch(settings.getString("menu", "All Files")){
            case "Audio Files":
                getAudioFiles();
                updateListView();
                break;
            case "Recordings":
                getRecordedFiles();
                updateListView();
                break;
            case "Favorites":
                getFavoriteFiles();
                updateListView();
                break;
            default:
                update();
                break;
        }
    }

    private  boolean checkAndRequestPermissions() {
        int permissionRead = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recordPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (recordPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionRead != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQ);
            return false;
        }
        return true;
    }
}
