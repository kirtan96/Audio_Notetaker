package com.kirtan.audionotetaker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class Search extends AppCompatActivity {
    ListView listView;
    ArrayList<String> key;
    String search;
    String file = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listView);

        final EditText editText = (EditText) findViewById(R.id.searchText);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    handled = true;

                    search = editText.getText().toString();
                    key = new ArrayList<>();
                    SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    Map<String, ?> keys = myPrefs.getAll();
                    for(Map.Entry<String, ?> entryKey: keys.entrySet())
                    {
                        if(!entryKey.getKey().equals("myFiles") && !entryKey.getKey().contains("content://") ) {
                            if (entryKey.getValue().toString().contains(editText.getText().toString())) {
                                key.add(entryKey.getKey());
                            }
                        }
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(Search.this,
                            android.R.layout.simple_list_item_1, key);
                    listView.setAdapter(arrayAdapter);
                }

                return handled;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                file = listView.getItemAtPosition(position).toString();
                Intent intent = new Intent(Search.this, SearchResult.class);
                intent.putExtra("file", file);
                intent.putExtra("search", search);
                startActivity(intent);
            }
        });
    }

}