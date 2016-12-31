package com.kirtan.audionotetaker.Classes;

import java.util.ArrayList;

/**
 * Created by Kirtan on 11/19/16.
 */

public class AudioNote {

    //TODO: Try to see if you can implement the objects in the app

    private ArrayList<Folder> folders;
    private ArrayList<AudioFile> audioFiles;
    private ArrayList<RecordedFile> recordedFiles;

    public AudioNote(){
        folders = new ArrayList<>();
    }

    public void addFolder(Folder f){
        if(unique(f)){
            folders.add(f);
        }
    }

    public void removeFolder(Folder f){
        folders.remove(f);
    }

    private boolean unique(Folder f){
        for(Folder fl: folders){
            if(fl == f){
                return false;
            }
        }
        return true;
    }
}
