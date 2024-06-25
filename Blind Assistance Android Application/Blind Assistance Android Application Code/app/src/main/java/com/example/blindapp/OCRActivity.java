package com.example.blindapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OCRActivity extends AppCompatActivity {

    Bitmap bitmap;
    TextView txtView;
    int SELECT_PICTURE = 200;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

   // private static final int CAMERA_REQUEST = 1888;
    //private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;


    ProgressDialog progressDialog;
    private TextToSpeech myTTS;   // Define the TTS objecy
    final String[] array = new String[]{
            "English-India", "Hindi", "Marathi"
    };
    private static final String TAG = "TextToSpeech";
    private static final int MY_STORAGE_PERMISSION_CODE = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocractivity);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        txtView=findViewById(R.id.txtView);
        myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Locale loc = new Locale ("en", "IND");
                    myTTS.setSpeechRate(0.7f);
                    myTTS.setLanguage(loc);
                    //  String str = txtView.getText().toString();
                    myTTS.speak("select image selection option from dialog box that is what you want camera or gallary , rather than you can cancle operation.", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);

            }
            else
            {
               // Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
              //  startActivityForResult(cameraIntent, PICK_IMAGE_CAMERA);
                try {
                    PackageManager pm = getPackageManager();
                    int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
                    int hasPerm1 = pm.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getPackageName());
                    if (hasPerm == PackageManager.PERMISSION_GRANTED || hasPerm1 == PackageManager.PERMISSION_GRANTED) {
                        final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(OCRActivity.this);
                        builder.setTitle("Select Option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (options[item].equals("Take Photo")) {

                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(intent, PICK_IMAGE_CAMERA);
                                    dialog.dismiss();
                                } else if (options[item].equals("Choose From Gallery")) {
                                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                                    {
                                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);

                                    }

                                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                                    dialog.dismiss();
                                } else if (options[item].equals("Cancel")) {
                                    dialog.dismiss();
                                    Intent i= new Intent(OCRActivity.this,MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        });
                        builder.show();
                    } else
                        Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }

        //image_view.setImageDrawable(getResources().getDrawable(R.drawable.ocr_sample));



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, PICK_IMAGE_CAMERA);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (requestCode == PICK_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            //imageView.setImageBitmap(bitmap);
            // Starting Text Recognizer
            if (!txtRecognizer.isOperational()) {
                // Shows if your Google Play services is not up to date or OCR is not supported for the device
                String sentence = "Detector dependencies are not yet available";
                txtView.setText(sentence);
            }
            else {
                // Set the bitmap taken to the frame to perform OCR Operations.
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray items = txtRecognizer.detect(frame);
                StringBuilder strBuilder = new StringBuilder();

                for (int i = 0; i < items.size(); i++) {
                    TextBlock item = (TextBlock) items.valueAt(i);
                    strBuilder.append(item.getValue());
                    strBuilder.append("/");
                    for (Text line : item.getComponents()) {
                        //extract scanned text lines here
                        Log.v("lines", line.getValue());
                        for (Text element : line.getComponents()) {
                            String ele_value = element.getValue();
                        }
                    }
                }

                txtView.setText(strBuilder);

                myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            Locale loc = new Locale("en", "IND");
                            myTTS.setLanguage(loc);
                            String str = txtView.getText().toString();
                            myTTS.setSpeechRate(0.5f);
                            myTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                });
// Shows if your Google Play services is not up to date or OCR is not supported for the device
                //speak();
               /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Language");
                builder.setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // ((EditText) v).setText(array[i]);
                        if (array[i].equalsIgnoreCase("English-India")) {


                            myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                public void onInit(int status) {
                                    if (status != TextToSpeech.ERROR) {
                                        Locale loc = new Locale("en", "IND");
                                        myTTS.setLanguage(loc);
                                        String str = txtView.getText().toString();
                                        myTTS.setSpeechRate(0.5f);
                                        myTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                }
                            });

                        } else if (array[i].equalsIgnoreCase("Hindi")) {

                            myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                public void onInit(int status) {
                                    if (status != TextToSpeech.ERROR) {
                                        String str = txtView.getText().toString();
                                        Locale loc = new Locale("hi", "IND");
                                        int result = myTTS.setLanguage(loc);
                                        myTTS.setSpeechRate(0.5f);
                                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                            Log.e(TAG, "Language is not available");
                                        } else {
                                            // myTTS.speak("स्मार्टहब पर मिले 124 रुपये और 50 पैसे नकद", TextToSpeech.QUEUE_FLUSH, null);
                                            myTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);
                                        }

                                    }
                                }
                            });


                        } else if (array[i].equalsIgnoreCase("Marathi")) {
                            myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                public void onInit(int status) {
                                    if (status != TextToSpeech.ERROR) {
                                        String str = txtView.getText().toString();
                                        Locale loc = new Locale("mr", "IND");
                                        int result = myTTS.setLanguage(loc);
                                        myTTS.setSpeechRate(0.5f);
                                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                            Log.e(TAG, "Language is not available");
                                        } else {
                                            //   myTTS.speak("Smarthub वर 124 रुपये आणि 50 पैसे रोख मिळाले", TextToSpeech.QUEUE_FLUSH, null);
                                            myTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);
                                        }

                                    }
                                }
                            });

                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.show();*/
            }
        }
        else {//if (requestCode == PICK_IMAGE_GALLERY && requestCode == Activity.RESULT_OK){
            if (data != null){
                Uri selectedImage = data.getData();
                if (null != selectedImage) {

                    //CONVERT IMAGE TO BITMAP
                    try {
                        bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("bitmap", String.valueOf(bitmap));

                    //convert to form of store into database
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                    byte[] b = baos.toByteArray();
                    //Image = Base64.encodeToString(b, Base64.DEFAULT);
                    //Log.e("Image", Image);

                    // TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                    if (!txtRecognizer.isOperational())
                    {

                        // Shows if your Google Play services is not up to date or OCR is not supported for the device
                        txtView.setText("Detector dependencies are not yet available");
                    }
                    else
                    {
                        // Set the bitmap taken to the frame to perform OCR Operations.
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray items = txtRecognizer.detect(frame);
                        StringBuilder strBuilder = new StringBuilder();

                        for (int i = 0; i < items.size(); i++) {
                            TextBlock item = (TextBlock) items.valueAt(i);
                            strBuilder.append(item.getValue());
                            strBuilder.append("/");
                            for (Text line : item.getComponents()) {
                                //extract scanned text lines here
                                Log.v("lines", line.getValue());
                                for (Text element : line.getComponents()) {
                                    //extract scanned text words here
                                    if(items.size()==i+1) {
                                        Log.v("element", element.getValue() +"//views are : "+element.getValue());//+"//"+items.size() +"//"+i
                                        // views=element.getValue();
                                    }
                                }
                            }
                        }

                        txtView.setText(strBuilder.toString());
                        // txtView.setText(sentence);
                        myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            public void onInit(int status) {
                                if (status != TextToSpeech.ERROR) {
                                    Locale loc = new Locale ("en", "IND");
                                    myTTS.setLanguage(loc);
                                    String str = txtView.getText().toString();
                                    myTTS.setSpeechRate(0.5f);
                                    myTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        });
                        //speak();
                       /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Select Language");
                        builder.setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // ((EditText) v).setText(array[i]);
                                if(array[i].equalsIgnoreCase("English-India")){


                                    myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        public void onInit(int status) {
                                            if (status != TextToSpeech.ERROR) {
                                                Locale loc = new Locale ("en", "IND");
                                                myTTS.setLanguage(loc);
                                                String str = txtView.getText().toString();
                                                myTTS.setSpeechRate(0.5f);
                                                myTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);
                                            }
                                        }
                                    });

                                }
                                else if(array[i].equalsIgnoreCase("Hindi")){

                                    myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        public void onInit(int status) {
                                            if (status != TextToSpeech.ERROR) {
                                                String str = txtView.getText().toString();
                                                Locale loc = new Locale ("hi", "IND");
                                                int result = myTTS.setLanguage(loc);
                                                myTTS.setSpeechRate(0.5f);
                                                if (result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED){
                                                    Log.e(TAG, "Language is not available");
                                                }else {
                                                    // myTTS.speak("स्मार्टहब पर मिले 124 रुपये और 50 पैसे नकद", TextToSpeech.QUEUE_FLUSH, null);
                                                    myTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);
                                                }

                                            }
                                        }
                                    });


                                }
                                else if(array[i].equalsIgnoreCase("Marathi")){
                                    myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        public void onInit(int status) {
                                            if (status != TextToSpeech.ERROR) {
                                                String str = txtView.getText().toString();
                                                Locale loc = new Locale ("mr", "IND");
                                                int result = myTTS.setLanguage(loc);
                                                myTTS.setSpeechRate(0.5f);
                                                if (result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED){
                                                    Log.e(TAG, "Language is not available");
                                                }else {
                                                    //   myTTS.speak("Smarthub वर 124 रुपये आणि 50 पैसे रोख मिळाले", TextToSpeech.QUEUE_FLUSH, null);
                                                    myTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);
                                                }

                                            }
                                        }
                                    });

                                }
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();*/
                        //txtView.setText("views : "+views);

                    }

                }

            }

        }
    }

    public void speak() {



    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if(myTTS != null){

            myTTS.stop();
            myTTS.shutdown();
        }

        super.onPause();
    }


    @Override
    protected void onResume() {

        speak();

        super.onResume();
    }


    @Override
    public void onBackPressed() {

        myTTS.stop();
        myTTS.shutdown();

        Intent in =new Intent(OCRActivity.this,MainActivity.class);
        startActivity(in);
        finish();
    }


}