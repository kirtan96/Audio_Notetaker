package com.kirtan.audionotetaker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {


    ListView note;
    String uri = "";
    String file = "";
    ArrayList<String> noteList;
    SharedPreferences myPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        note = (ListView) findViewById(R.id.listView2);
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, 1);
            }
        });

        note.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                file = note.getItemAtPosition(position).toString();
                Intent intent = new Intent(MainActivity.this, Player.class);
                intent.putExtra("file", file);
                startActivity(intent);
            }
        });
        noteList = new ArrayList<>();
        Scanner in = new Scanner(myPrefs.getString("myFiles", ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine();
            noteList.add(temp.trim());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1, noteList);
        note.setAdapter(arrayAdapter);
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
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                final String myFiles = myPrefs.getString("myFiles", "");
                alertDialog.setPositiveButton("Create",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if(!myPrefs.getString("myFiles", "").contains(input.getText().toString())) {
                                        file = input.getText().toString();
                                        SharedPreferences.Editor e = myPrefs.edit();
                                        e.putString("myFiles", myFiles + file + "\n");
                                        e.putString(file, uri);
                                        e.commit();
                                        Intent intent = new Intent(MainActivity.this, Player.class);
                                        intent.putExtra("file", file);
                                        startActivity(intent);
                                    }
                                    else
                                    {
                                        Toast.makeText(MainActivity.this, "This file already exists. Name it differently!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                alertDialog.show();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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
