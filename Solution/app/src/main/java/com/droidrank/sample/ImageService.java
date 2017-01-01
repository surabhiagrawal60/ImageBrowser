package com.droidrank.sample;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;



public class ImageService extends Service {

    final Bitmap[] bitmap = new Bitmap[1];
    final String url = "https://unsplash.it/200/300/?random";
    final boolean[] storeSuccess = new boolean[1];

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {

        Log.d("ImageService", "onStart()");

        Thread threadDownloadData = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("ImageService", "threadDownloadData.run()");
                if (MainActivity.stored_image_counter > 99)
                {
                    Log.d("ImageService", "Limit Exceeded");
                    return;
                }

                for (int i = 0; i < 5; i++)
                {
                    bitmap[0] = Utility.getBitmapFromURL(url);
                    storeSuccess[0] = MainActivity.storeImage(bitmap[0]);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        threadDownloadData.start();
    }
}