package com.kirtan.audionotetaker.Classes;

import java.util.HashMap;

/**
 * Created by Kirtan on 11/19/16.
 */

public interface File {
    void setName(String name);
    String getName();
    String getPath();
    HashMap<String, String> getNotes();
    void addNote(String time, String note);
    void editNote(String time, String note);
    void deleteNote(String time);
    void setFav();
    boolean isFav();
}
