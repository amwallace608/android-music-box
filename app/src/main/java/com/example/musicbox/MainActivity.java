package com.example.musicbox;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView titleTxt, artistTxt, leftTxt, rightTxt;
    private ImageView albumImg;
    private Button prevBtn, playBtn, nextBtn;
    private SeekBar mSeekBar;
    private MediaPlayer mediaPlayer;
    private int[] songIDs;
    private int songTracker;
    private Thread seekBarThread;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set up UI
        setUpUI();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
                int currentPos = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();

                //convert ms of current position into date object, then format
                leftTxt.setText(dateFormat.format(new Date(currentPos)));
                //calculate time remaining, convert to date object and reformat
                rightTxt.setText(dateFormat.format(new Date(duration - currentPos)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
	
    //set up UI and media player method
    public void setUpUI(){
        //Text views
        titleTxt = (TextView) findViewById(R.id.titleTxt);
        artistTxt = (TextView) findViewById(R.id.artistTxt);
        leftTxt = (TextView) findViewById(R.id.leftTimeTxt);
        rightTxt = (TextView) findViewById(R.id.rightTimeTxt);
        //Image View
        albumImg = (ImageView) findViewById(R.id.albumImg);
        //Buttons
        prevBtn = (Button) findViewById(R.id.prevBtn);
        playBtn = (Button) findViewById(R.id.playBtn);
        nextBtn = (Button) findViewById(R.id.nextBtn);
        //Seekbar
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        //onclick listeners
        prevBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        //Media player setup
        //songIDs setup
        songIDs = new int[]{R.raw.spiritbox_blessed_be,
                R.raw.ne_obliviscaris_eyrie,
                R.raw.ramzoid_canada};
        //mediaplayer instantiation
        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songIDs[0]);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                skipNext();
            }
        });
        mSeekBar.setMax(mediaPlayer.getDuration());
        songTracker = 0;
        setTrackInfo();

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.prevBtn:
                skipPrev();
                break;
            case R.id.playBtn:
                if(mediaPlayer.isPlaying()){
                    //pause
                    pauseMusic();
                }else{
                    startMusic();
                }

                break;
            case R.id.nextBtn:
                skipNext();
                break;
            default:
                break;
        }
    }

    //play music method
    public void startMusic(){
        if(mediaPlayer != null){
            //start music
            mediaPlayer.start();
            //start seekBarThread
            updateThread();
            //change button icon (foreground) to pause symbol
            playBtn.setForeground(getDrawable(R.drawable.ic_pause_black_24dp));
        }
    }
    //pause music method
    public void pauseMusic(){
        if (mediaPlayer != null){
            //pause music
            mediaPlayer.pause();
            //change button icon (foreground) to play symbol
            playBtn.setForeground(getDrawable(R.drawable.ic_play_arrow_black_24dp));
        }
    }
    //skip next
    public void skipNext(){
        //skip to first song if last song is playing
        if(songTracker == 2){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(),songIDs[0]);
            songTracker = 0;
            mediaPlayer.start();
            playBtn.setForeground(getDrawable(R.drawable.ic_pause_black_24dp));
        }else{
            //not last song, go forward one
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(),songIDs[songTracker+1]);
            songTracker++;
            mediaPlayer.start();
            playBtn.setForeground(getDrawable(R.drawable.ic_pause_black_24dp));
        }
        setTrackInfo();
    }
    //skip previous
    public void skipPrev(){
        //if first song, restart
        if(songTracker == 0){
            mediaPlayer.seekTo(0);
        }else{
            //not first song, go back one
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(),songIDs[songTracker-1]);
            songTracker--;
            mediaPlayer.start();
            playBtn.setForeground(getDrawable(R.drawable.ic_pause_black_24dp));
            setTrackInfo();
        }
    }
    //update/set track information and album image
    public void setTrackInfo(){

        //set track info and album images for each song
        switch (songTracker){
            case 0:
                albumImg.setImageDrawable(getDrawable(R.drawable.blessed_be));
                titleTxt.setText(R.string.track0_title);
                artistTxt.setText(R.string.track0_artist);
                break;
            case 1:
                albumImg.setImageDrawable(getDrawable(R.drawable.urn_album));
                titleTxt.setText(R.string.track1_title);
                artistTxt.setText(R.string.track1_artist);
                break;
            case 2:
                albumImg.setImageDrawable(getDrawable(R.drawable.canada));
                titleTxt.setText(R.string.track2_title);
                artistTxt.setText(R.string.track2_artist);
                break;
        }
    }
    //Thread to update seekbar progress to song time
    public void updateThread(){
        seekBarThread = new Thread(){
            @Override
            public void run(){
                try{
                    //run while media player is playing, after 50ms sleep
                    while(mediaPlayer != null && mediaPlayer.isPlaying()) {
                        Thread.sleep(50);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int newPosition = mediaPlayer.getCurrentPosition();
                                int newMax = mediaPlayer.getDuration();
                                mSeekBar.setMax(newMax);
                                mSeekBar.setProgress(newPosition);
                                //update time texts
                                leftTxt.setText(new SimpleDateFormat("mm:ss")
                                        .format(new Date(mediaPlayer.getCurrentPosition())));
                                rightTxt.setText(new SimpleDateFormat("mm:ss")
                                        .format(new Date(mediaPlayer.getDuration() -
                                                mediaPlayer.getCurrentPosition())));
                            }
                        });
                    }
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };
        seekBarThread.start();
    }

    @Override
    protected void onDestroy() {
        //release media player resources
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        //stop thread
        seekBarThread.interrupt();
        seekBarThread = null;
        super.onDestroy();
    }
}
