package com.example.akord;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.jtransforms.fft.FloatFFT_1D;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private int BufferElements2Rec = RECORDER_SAMPLERATE;
    private int BytesPerElement = 2;
    private final int AUDIO_RECORD_REQUEST = 2;
    Button verif;
    TextView nota, curent, trebuie;
    double[] note_freq = {55, 58.27, 61.73, 65.40, 69.29, 73.41, 77.78, 82.40, 87.30, 92.49, 97.99, 103.82, 110, 116.54, 123.47, 130.81,
            138.59, 146.83, 155.56, 164.81, 174.61, 185, 196, 207.65, 220, 233.08, 246.94, 261.63, 277.18, 293.66, 311.13, 329.63, 349.23,
            369.99, 392, 415.3, 440, 466.16};
    String[] note_name = {"A", "Bb", "B", "C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("SMEN", "HELLO BOIS");
        nota = findViewById(R.id.nota);
        nota.setText("-");
        trebuie = findViewById(R.id.trebuie);
        trebuie.setText("-");
        curent = findViewById(R.id.curent);
        curent.setText("-");
        verif = findViewById(R.id.button_rec);
        verif.setEnabled(false);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_RECORD_REQUEST);
        verif.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Countdown();
            }
        });


        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        Log.i("SMEN", String.valueOf(bufferSize));

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case AUDIO_RECORD_REQUEST:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    verif.setEnabled(true);
                }
        }
    }

    private void Countdown() {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        verif.setEnabled(false);
                    }
                });
                for(int i = 3; i >= 0; i--) {
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            verif.setText("" + finalI);
                        }
                    });
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        verif.setText("recording");
                    }
                });

                final short sData[] = new short[BufferElements2Rec];
                //recorder.release();
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                        RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

                recorder.startRecording();
                Log.i("SMEN", "am pornit recordr " + recorder.getRecordingState());
                recorder.read(sData, 0, BufferElements2Rec, AudioRecord.READ_BLOCKING);
                recorder.stop();
                recorder.release();

                final String[] estimat = gaseste_nota2(sData);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nota.setText(estimat[0]);
                        trebuie.setText("goal: " + estimat[1]);
                        curent.setText("read: " + estimat[2]);
                        verif.setEnabled(true);
                        verif.setText("verifica nota");
                    }
                });

            }
        }.start();
    }

    private String gaseste_nota(short[] sData) {
        float[] signal = new float[sData.length];
        for(int i = 0; i < sData.length; i++) {
            signal[i] = sData[i];
        }

        FloatFFT_1D floatFFT_1D = new FloatFFT_1D(RECORDER_SAMPLERATE);
        floatFFT_1D.realForward(signal);

        // Extract Real part
        float localMax = Float.MIN_VALUE;
        int maxValueFreq = -1;
        float[] result = new float[signal.length / 2];
        for(int s = 50; s <= 450; s++) {
            float re = signal[s * 2];
            float im = signal[s * 2 + 1];
            result[s] = (float) Math.sqrt(re * re + im * im) / result.length;
            if(result[s] > localMax) {
                maxValueFreq = s;
            }
            localMax = Math.max(localMax, result[s]);
        }
        return "" + maxValueFreq;
    }

    private String[] gaseste_nota2(short[] sData) {
        float[] signal = new float[sData.length];
        for(int i = 0; i < sData.length; i++) {
            signal[i] = sData[i];
        }

        FloatFFT_1D floatFFT_1D = new FloatFFT_1D(RECORDER_SAMPLERATE);
        floatFFT_1D.realForward(signal);

        // Extract Real part
        float localMax = Float.MIN_VALUE;
        int maxValueFreq = -1;
        float[] result = new float[signal.length / 2];
        for(int s = 60; s <= 450; s++) {
            float re = signal[s * 2];
            float im = signal[s * 2 + 1];
            result[s] = (float) Math.sqrt(re * re + im * im) / result.length;
            if(result[s] > localMax) {
                localMax = result[s];
                maxValueFreq = s;
            }
        }

        for(int s = 60; s <= 450; s++) {
            if(result[s] > localMax / 2) {
                maxValueFreq = s;
                break;
            }
        }

        int poz = 1;
        while(maxValueFreq > note_freq[poz]) {
            poz++;
        }

        String guide;
        String good_freq;
        if(Math.round(note_freq[poz]) == maxValueFreq) {
            guide =  note_name[poz % 12] + "" + (((poz + 8) / 12) + 1);
            good_freq = "" + note_freq[poz];
        } else if((note_freq[poz] / maxValueFreq) > (maxValueFreq / note_freq[poz - 1])) {
            guide =  note_name[(poz - 1) % 12] + "" + (((poz + 7) / 12) + 1) + "+";
            good_freq = "" + note_freq[poz - 1];
        } else {
            good_freq = "" + note_freq[poz];
            guide =  note_name[poz % 12] + "" + (((poz + 8) / 12) + 1) + "-";
        }

        return new String[]{guide, good_freq, String.valueOf(maxValueFreq)};
    }
}
