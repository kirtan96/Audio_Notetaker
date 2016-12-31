package com.kirtan.audionotetaker.Classes;

import java.util.ArrayList;

/**
 * Created by Kirtan on 11/19/16.
 */

public class Folder {

    private String name;
    private ArrayList<AudioFile> audioFiles;
    private ArrayList<RecordedFile> recordedFiles;

    public Folder(String name){
        this.name = name;
        audioFiles = new ArrayList<>();
        recordedFiles = new ArrayList<>();
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void addAudioFile(AudioFile f){
        if(unique(f.getName())){
            audioFiles.add(f);
        }
    }

    public void addRecordedFile(RecordedFile f){
        if(unique(f.getName())){
            recordedFiles.add(f);
        }
    }

    public void removeAudioFile(AudioFile f){
        audioFiles.remove(f);
    }

    public void removeRecordedFile(RecordedFile f){
        recordedFiles.remove(f);
    }

    private boolean unique(String fName){
        for(AudioFile af: audioFiles){
            if(af.getName().equals(fName)){
                return false;
            }
        }
        for(RecordedFile rf: recordedFiles){
            if(rf.getName().equals(fName)){
                return false;
            }
        }
        return true;
    }
}
