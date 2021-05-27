package com.ishika.mugix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button play, next, prev, rewind, fastForward;
    TextView songname, start, end;
    BarVisualizer barVisualizer;
    SeekBar seekBar;

    String sname;
    public static final String EXTRA_Name = "Song_name";
    static MediaPlayer mediaPlayer;
    int pos;
    ArrayList<File> mySongs;
    Thread updateSeekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home);{
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(barVisualizer != null){
            barVisualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_player);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);
        rewind = findViewById(R.id.rewind);
        fastForward = findViewById(R.id.forward);
        songname = findViewById(R.id.songName);
        start = findViewById(R.id.textStart);
        end = findViewById(R.id.textEnd);
        seekBar = findViewById(R.id.seekbar);
        barVisualizer = findViewById(R.id.bar);

        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }


        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        sname = intent.getStringExtra("songname");
        pos = bundle.getInt("pos");
        songname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(pos).toString());
        sname = mySongs.get(pos).getName();
        songname.setText(sname);

        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();


        updateSeekbar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPos = 0;

                while (currentPos<totalDuration){
                    try {
                        sleep(500);
                        currentPos = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPos);
                    }catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endtime = createTime(mediaPlayer.getDuration());
        end.setText(endtime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                start.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    play.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                    mediaPlayer.pause();
                }else{
                    play.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next.performClick();
            }
        });

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId != -1){
            barVisualizer.setAudioSessionId(audioSessionId);
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                pos = (pos+1)%mySongs.size();
                Uri uri = Uri.parse(mySongs.get(pos).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                sname = mySongs.get(pos).getName();
                songname.setText(sname);
                mediaPlayer.start();
                play.setBackgroundResource(R.drawable.ic_baseline_pause_24);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId != -1){
                    barVisualizer.setAudioSessionId(audioSessionId);
                }

            }
        });


        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(pos-1<0)
                    pos = mySongs.size()-1;
                else
                    pos--;
                Uri uri = Uri.parse(mySongs.get(pos).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                sname = mySongs.get(pos).getName();
                songname.setText(sname);
                mediaPlayer.start();
                play.setBackgroundResource(R.drawable.ic_baseline_pause_24);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId != -1){
                    barVisualizer.setAudioSessionId(audioSessionId);
                }

            }
        });

        fastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });


        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }

    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time += min + ":";

        if(sec<10){
            time += "0";
        }

        time += sec;

        return time;
    }
}