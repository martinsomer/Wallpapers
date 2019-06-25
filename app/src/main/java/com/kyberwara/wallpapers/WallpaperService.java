package com.kyberwara.wallpapers;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WallpaperService extends Service {

    ScheduledExecutorService service;
    private ArrayList<Uri> images;
    private int index = 0;

    public WallpaperService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        images = intent.getParcelableArrayListExtra("images");
        int period = intent.getIntExtra("period", 10);

        TimeUnit timeUnit;
        switch (intent.getStringExtra("timeUnit")) {
            case "Seconds":
                timeUnit = TimeUnit.SECONDS;
                break;

            case "Minutes":
                timeUnit = TimeUnit.MINUTES;
                break;

            case "Hours":
                timeUnit = TimeUnit.HOURS;
                break;

            case "Days":
                timeUnit = TimeUnit.DAYS;
                break;

            default:
                timeUnit = TimeUnit.MINUTES;
                break;
        }

        Runnable runnable = new Runnable() {
            public void run() {

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), images.get(index));
                    WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
                    wm.setBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (index < images.size() - 1) {
                    index += 1;
                } else {
                    index = 0;
                }
            }
        };

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, period, timeUnit);

        //return super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        service.shutdown();
        super.onDestroy();
    }
}
