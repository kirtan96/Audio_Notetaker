package com.kirtan.audionotetaker.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kirtan.audionotetaker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Search extends AppCompatActivity {
    ListView listView;
    ArrayList<String> key;
    String search;
    String file = "";
    TextView nrf;
    FileAdapter fileAdapter;
    SharedPreferences myPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        listView = (ListView) findViewById(R.id.listView);
        nrf = (TextView) findViewById(R.id.no_result);
        nrf.setVisibility(View.INVISIBLE);

        final EditText editText = (EditText) findViewById(R.id.searchText);
        assert editText != null;
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    handled = true;

                    search = editText.getText().toString().trim();
                    if(!search.isEmpty()) {
                        getSupportActionBar().setTitle(search);
                        key = new ArrayList<>();
                        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                        Map<String, ?> keys = myPrefs.getAll();
                        for (Map.Entry<String, ?> entryKey : keys.entrySet()) {
                            if ((!entryKey.getKey().contains("checkBox")) &&
                                    (!entryKey.getKey().contains("myYouTubeFiles")) &&
                                    (!entryKey.getKey().contains("content://")) &&
                                    (!entryKey.getKey().contains("file://")) &&
                                    (!entryKey.getKey().contains("(FOLDER)")) &&
                                    (!entryKey.getKey().contains("myFolders")) &&
                                    (!entryKey.getKey().contains("recordingsInt"))) {
                                if (myPrefs.getString(entryKey.getValue().toString(), "").toLowerCase().contains(search.toLowerCase()) ||
                                        ((!entryKey.getKey().toString().equals("myFiles") &&
                                                (entryKey.getKey().toString().toLowerCase().contains(search.toLowerCase())))))
                                {
                                    key.add(entryKey.getKey());
                                }
                            }
                        }
                        if(key.isEmpty())
                        {
                            nrf.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            nrf.setVisibility(View.INVISIBLE);
                        }
                        Collections.sort((List)key);
                        fileAdapter = new FileAdapter(key);
                        listView.setAdapter(fileAdapter);
                    }
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
            if (myPrefs.getString(c, "").contains("file:/"))
                v = getLayoutInflater().inflate(R.layout.list_recordings, null);
            else
                v = getLayoutInflater().inflate(R.layout.list_file, null);

            TextView lbl = (TextView) v.findViewById(R.id.note);
            lbl.setText(s.get(pos));

            return v;
        }

    }

}