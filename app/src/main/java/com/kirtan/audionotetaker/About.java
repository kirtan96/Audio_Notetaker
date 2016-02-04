package com.kirtan.audionotetaker;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String s= "About Audio Note:";
        String info = "\n\t\tIn this app, you can take notes on the audio files from your device or while recording the audio." +
                " You can export your notes as txt file for future" +
                "\n\n- You can find the recorded audio from this app in Audio Note/Recordings in your files.\n" +
                "- You can find the exported notes in Audio Note/Notes.";
        String important = "\n\nIMPORTANT:\nDO NOT MOVE THE AUDIO FILES FROM ONE FOLDER TO ANOTHER" +
                " OR ELSE YOU WILL LOSE YOU NOTES YOU TOOK ON THAT AUDIO FILE.\n\n";
       String features = "Features:\n- To add audio, you can select audio from your device or you can record audio\n" +
                "- Hold on a folder to rename or delete\n" +
                "- Hold on a file to rename, move, or delete\n" +
                "- Tap on a note and the audio will start playing from that note's timestamp\n" +
                "- Hold on a note to edit or delete\n";
        String f = s+ info+important+ features;
        SpannableString ss = new SpannableString(f);
        ss.setSpan(new RelativeSizeSpan(2f), 0,s.length(), 0); // set size
        ss.setSpan(new RelativeSizeSpan(2f), s.length()+ info.length(),f.indexOf("IMPORTANT:")+10, 0); // set size
        ss.setSpan(new RelativeSizeSpan(2f), s.length()+ info.length()+important.length(),f.indexOf("Features:")+9, 0);
        ss.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s.length(), 0);// set color
        ss.setSpan(new ForegroundColorSpan(Color.RED), s.length()+ info.length(), f.indexOf("IMPORTANT:")+10, 0);// set color
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), s.length()+ info.length()+important.length(),f.indexOf("Features:")+9, 0);
        TextView tv= (TextView) findViewById(R.id.aboutText);
        tv.setText(ss);
    }

}
