package com.kirtan.audionotetaker.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kirtan.audionotetaker.R;

/**
 * Created by Kirtan on 9/15/16.
 */
public class MenuFragment extends Fragment {
    private OnClickedListener mCallback;
    private View v;
    private Button rate, getanp;
    private TextView odallfiles, odaudiofiles, odrec, odyt, callfiles, caudiofiles, crec, cyt, onCloud;
    float x1,x2;
    float y1, y2;

    public interface OnClickedListener {
        void onCloseClicked();
        void onMenuOptionClicked(String s);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnClickedListener) context;
        }
        catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnCloseClickedListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnClickedListener) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnCloseClickedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_menu, container, false);
        final RelativeLayout relativeLayout = (RelativeLayout) v.findViewById(R.id.menuLayout);
        odallfiles = (TextView) v.findViewById(R.id.odallfiles);
        odaudiofiles = (TextView) v.findViewById(R.id.odaudioFiles);
        odrec = (TextView) v.findViewById(R.id.odrecordings);
        rate = (Button) v.findViewById(R.id.rateButton);
        getanp = (Button) v.findViewById(R.id.anpButton);

        odallfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onMenuOptionClicked("All Files");
            }
        });

        odaudiofiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onMenuOptionClicked("Audio Files");
            }
        });

        odrec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onMenuOptionClicked("Recorded Files");
            }
        });

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.kirtan.audionotetaker"));
                startActivity(intent);
            }
        });

        getanp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.kirtan.audionotepro"));
                startActivity(intent);
            }
        });

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

                        // if right to left sweep event on screen
                        if (x1 > x2)
                        {
                            if(mCallback != null){
                                mCallback.onCloseClicked();
                            }
                        }
                        break;
                    }
                }
                return true;
            }
        });

        return v;
    }
}
