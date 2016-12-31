package com.kirtan.audionotetaker.Classes;

import android.net.Uri;

import java.util.HashMap;

/**
 * Created by Kirtan on 11/19/16.
 */

public class RecordedFile implements File {

    private String name;
    private Uri uri;
    private HashMap<String, String> notes;
    private boolean fav;

    public RecordedFile(String name, Uri uri){
        this.name = name;
        this.uri = uri;
        notes = new HashMap<>();
        fav = false;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public HashMap<String, String> getNotes() {
        return notes;
    }

    @Override
    public void addNote(String time, String note) {
        if(notes.containsKey(time)){
            notes.put(time, notes.get(time) + "\n" + note);
        }
        else{
            notes.put(time, note);
        }
    }

    @Override
    public void editNote(String time, String note) {
        notes.put(time, note);
    }

    @Override
    public void deleteNote(String time) {
        notes.remove(time);
    }

    @Override
    public void setFav() {
        fav = true;
    }

    @Override
    public boolean isFav() {
        return fav;
    }
}
