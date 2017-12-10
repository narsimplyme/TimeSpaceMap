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

        VideoView=(VideoView)findViewById(R.id.videoView);//videoview xml파일 가져오기

        Uri video=Uri.parse("android.resource://" + getPackageName()+"/" +R.raw.intro);//video 위치 변수에 저장

        VideoView.setVideoURI(video);//videovidw에 위치 넣기

        VideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(isFinishing())
                    return;
                startActivity(new Intent(SplashActivity.this, MapsActivity.class));//인텐트 발생으로 맵엑티비티 시작
                finish();
            }
        });
        VideoView.start();//비디오뷰 시작
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN) {//터치 다운 됬을 경우
            startActivity(new Intent(SplashActivity.this, MapsActivity.class));//맵스 액티비티 바로시작
            finish();
        }
        return super.onTouchEvent(event);
    }
}
