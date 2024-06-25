package com.example.blindapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class Splash extends AppCompatActivity {

    ImageView imageView;
    private ProgressBar progress_indeterminate_circular;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        imageView = (ImageView) findViewById(R.id.imageView);
        progress_indeterminate_circular = (ProgressBar) findViewById(R.id.progress_indeterminate_circular);
        final Animation animation_1 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        final Animation animation_2 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.antirotate);
      //  final Animation animation_3 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_out);
        imageView.startAnimation(animation_2);
        runProgressDeterminateCircular();

       /* final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                    Intent i = new Intent(getBaseContext(),MainActivity.class);
                    startActivity(i);


            }
        }, 5000);

*/
        animation_2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.startAnimation(animation_1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation_1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.startAnimation(animation_2);
                finish();
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void runProgressDeterminateCircular() {
        final Handler mHandler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                int progress = progress_indeterminate_circular.getProgress() + 10;
                progress_indeterminate_circular.setProgress(progress);
                if (progress > 100) {
                    progress_indeterminate_circular.setProgress(0);
                }
                mHandler.postDelayed(this, 300);
            }
        };
        mHandler.post(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}