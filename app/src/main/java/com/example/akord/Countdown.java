package com.example.akord;

import android.app.Activity;
import android.widget.Button;
import androidx.core.app.ActivityCompat;

public class Countdown implements Runnable {

    Button button;

    @Override
    public void run() {

        try {
            Thread.sleep(000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
