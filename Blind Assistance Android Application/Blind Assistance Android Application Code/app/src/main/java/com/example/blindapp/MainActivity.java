package com.example.blindapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button btn_ocr,btn_artical_reading;
    private TextToSpeech myTTS;   // Define the TTS objecy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_artical_reading=findViewById(R.id.btn_artical_reading);
        btn_ocr=findViewById(R.id.btn_ocr);

        myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Locale loc = new Locale ("en", "IND");
                    myTTS.setSpeechRate(0.7f);
                    myTTS.setLanguage(loc);
                    //  String str = txtView.getText().toString();
                    myTTS.speak("pressed up button for Object detection and for artical reading click on down button", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        btn_ocr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i= new Intent(MainActivity.this, com.example.blindapp.detection.DetectorActivity.class);
                startActivity(i);
                finish();
            }
        });

        btn_artical_reading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i= new Intent(MainActivity.this,OCRActivity.class);
                //Intent i= new Intent(MainActivity.this, ArticalReading.class);
                startActivity(i);
                finish();
            }
        });

    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub

        if(myTTS != null){

            myTTS.stop();
            myTTS.shutdown();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        myTTS.stop();
        myTTS.shutdown();
    }
}