package com.example.calllogreader;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Call Recording variables
        final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
        final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
        final String AUDIO_RECORDER_FOLDER = "AudioRecorder";

        MediaRecorder recorder = null;
        int currentFormat = 0;
        int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,
                MediaRecorder.OutputFormat.THREE_GPP };
        String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4,
                AUDIO_RECORDER_FILE_EXT_3GP };

        AudioManager audioManager;



        Cursor mCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = mCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int duration = mCursor.getColumnIndex(CallLog.Calls.DURATION);
        int type = mCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = mCursor.getColumnIndex(CallLog.Calls.DATE);
        StringBuilder sb = new StringBuilder();
        while (mCursor.moveToNext()){
            String phnumber = mCursor.getString(number);
            String callduration = mCursor.getString(duration);
            String calltype = mCursor.getString(type);
            String calldate = mCursor.getString(date);
            Date d = new Date(calldate);
            String callTypeStr = "";
            switch (Integer.parseInt(calltype)) {
                case CallLog.Calls.OUTGOING_TYPE:
                    callTypeStr = "Outgoing";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    callTypeStr = "Incoming";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    callTypeStr = "Missing";
                    break;
            }
            sb.append("Phone Number" +phnumber);
            sb.append("Call Duration" +callduration);
            sb.append("Call Type" +callTypeStr);
            sb.append("Call Date" +d);
            sb.append("___________________");
            sb.append(System.getProperty("line.separator"));
        }

        TextView callDetails = (TextView) findViewById(R.id.calllog);
        callDetails.setText(sb.toString());

        //Outside OnCreate Method
        private String getFilename() {
            String filepath = Environment.getExternalStorageDirectory().getPath();
            File file = new File(filepath, AUDIO_RECORDER_FOLDER);

            if (!file.exists()) {
                file.mkdirs();
            }

            return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
        }

        MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Toast.makeText(MainActivity.this,
                        "Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
            }
        };

        MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                Toast.makeText(MainActivity.this,
                        "Warning: " + what + ", " + extra, Toast.LENGTH_SHORT)
                        .show();
            }
        };

        //To Put on Speaker
        audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);

        //To Start Recording
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        //recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            Log.e("REDORDING :: ",e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("REDORDING :: ",e.getMessage());
            e.printStackTrace();
        }

        //To Stop recording: Put Speaker off
        audioManager.setSpeakerphoneOn(false);

        try{
            if (null != recorder) {
                recorder.stop();
                recorder.reset();
                recorder.release();

                recorder = null;
            }
        }catch(RuntimeException stopException){

        }

    }
}
