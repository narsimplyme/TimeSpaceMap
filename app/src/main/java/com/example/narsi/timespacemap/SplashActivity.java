package com.example.narsi.timespacemap;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.VideoView;

public class SplashActivity extends AppCompatActivity {
    VideoView VideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        VideoView=(VideoView)findViewById(R.id.videoView);

        Uri video=Uri.parse("android.resource://" + getPackageName()+"/" +R.raw.intro);

        VideoView.setVideoURI(video);

        VideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(isFinishing())
                    return;
                startActivity(new Intent(SplashActivity.this, MapsActivity.class));
                finish();
            }
        });
        VideoView.start();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startActivity(new Intent(SplashActivity.this, MapsActivity.class));
            finish();
        }
        return super.onTouchEvent(event);
    }
}
