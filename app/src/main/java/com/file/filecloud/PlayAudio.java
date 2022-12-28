package com.file.filecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.file.cloud.R;

public class PlayAudio extends AppCompatActivity {
    private String uid,timestamp,filename,Uri;
    private ImageView playPauseIv;
    private SeekBar progress;
    private TextView timeStop, timeStart;
    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playaudio);

        Intent intent = getIntent();
        uid = intent.getStringExtra("myUid");
        timestamp = intent.getStringExtra("timestamp");
        filename = intent.getStringExtra("fileName");
        Uri = intent.getStringExtra("uri");

        playPauseIv= findViewById(R.id.playPauseIv);
        progress= findViewById(R.id.progress);
        timeStop= findViewById(R.id.timeStop);
        timeStart= findViewById(R.id.timeStart);
        mediaPlayer = new MediaPlayer();

        progress.setMax(100);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .4));

        prepareMediaPlayer();

        playPauseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    handler.removeCallbacks(updater);
                    mediaPlayer.pause();
                   /// playPauseIv.setImageResource(R.drawable.ic_play);
                }else {
                    mediaPlayer.start();
                    ////playPauseIv.setImageResource(R.drawable.ic_pause);
                    updateSeeker();
                }
            }
        });



    }

    private void prepareMediaPlayer() {
        try {
            mediaPlayer.setDataSource(Uri);
            mediaPlayer.prepare();
            timeStop.setText(milliSecondToTimer(mediaPlayer.getDuration() ));
        }catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeeker();
            long currentDuration = mediaPlayer.getCurrentPosition();
            timeStart.setText(milliSecondToTimer(currentDuration));

        }
    };

    private void updateSeeker (){
        if (mediaPlayer.isPlaying()){
            progress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
            handler.postDelayed(updater,1000);
        }
    }

    private String milliSecondToTimer(long milliSeconds){
        String timerString = "";
        String secondsString;

        int hours = (int) (milliSeconds / (1000 * 60 * 60));
        int minutes = (int) (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0){
            timerString = hours + ":";

        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        }else {
            secondsString = "" + seconds;
        }
        timerString = timerString + minutes + ":" + secondsString;
        return  timerString;
    }
}