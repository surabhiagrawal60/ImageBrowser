package com.droidrank.sample;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.ListIterator;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends Activity {

    private Button previous, next;
    static final int PERMISSION_REQUEST_CODE = 1;
//    ArrayList<String> imagePathList = new ArrayList<>();
//    ListIterator<String> iterator = imagePathList.listIterator();
    final Bitmap[] bitmap = new Bitmap[1];
    final String url = "https://unsplash.it/200/300/?random";
    final boolean[] storeSuccess = new boolean[1];
    static String stored_image_path[] = new String[100];
    static int stored_image_counter = 0;
    static int browsingCounter = 0;
    ImageView img = null;
    PendingIntent pendingIntent;
    final int service_freq_pothole = 5 * 1000; //in ms
    Intent myIntent ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!checkPermission())
            requestPermission();

        Bundle bundle = new Bundle();
        myIntent = new Intent(MainActivity.this, ImageService.class);
        myIntent.putExtras(bundle);
        startService(myIntent);

        img = (ImageView) findViewById(R.id.imageview);

        initView();
    }

    private void initView() {

        final ImageView img = (ImageView) findViewById(R.id.imageview);
        previous = (Button) findViewById(R.id.previous);
        //onclick of previous button should navigate the user to previous image
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browsingCounter--;
                if (browsingCounter > 0) {
                    bitmap[0] = BitmapFactory.decodeFile(stored_image_path[browsingCounter]);
                    Log.d("MainActivity","previous:" + stored_image_path[browsingCounter]);

                    if (bitmap[0] != null && img != null) {
                        img.setImageBitmap(bitmap[0]);

                    } else {
                        Toast.makeText(getApplicationContext(), "Please check the internet connection.", Toast.LENGTH_SHORT).show();
                        img.setImageResource(R.drawable.ic_no_camera_capture_picture_image);
                    }
                } else
                {
                    browsingCounter = 1;
                    Toast.makeText(getApplicationContext(), "No more Images", Toast.LENGTH_SHORT).show();
                }
            }
        });
        next = (Button) findViewById(R.id.next);
        //onclick of next button should navigate the user to next image
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((stored_image_counter - browsingCounter) < 2 )
                    startService(myIntent);

                browsingCounter++;
                if (browsingCounter < 0)
                    browsingCounter = 0;

                if (browsingCounter >= stored_image_counter)
                {
                    browsingCounter--;
                }

//                if ( browsingCounter < stored_image_counter)

                if (stored_image_path[browsingCounter] != null) {
                    String imgPath;
//                    imgPath = iterator.next();
                    imgPath = stored_image_path[browsingCounter];

                    Log.d("MainActivity", "next(). storedImage: " + imgPath);
                    bitmap[0] = BitmapFactory.decodeFile(imgPath);
                    if (bitmap[0] != null) {
                        img.setImageBitmap(bitmap[0]);

                    } else {
                        Toast.makeText(getApplicationContext(), "Please connect to Wifi or mobile network", Toast.LENGTH_SHORT).show();
                        img.setImageResource(R.drawable.ic_no_camera_capture_picture_image);
                    }
                } else {

                    DownloadImages downloadImages = new DownloadImages();
                    downloadImages.execute();
                }
            }
        });
    }

    public static boolean storeImage(Bitmap imageData) {
        //get path to external storage (SD card)

        if (imageData == null )
        {
            Log.d("MainActivity","store(): imageData==null");
            return false;
        }

        String iconsStoragePath = Environment.getExternalStorageDirectory() + "/myAppDir/img_";
        File sdIconStorageDir = new File(iconsStoragePath);

        //create storage directories, if they don't exist
        sdIconStorageDir.mkdirs();

        try {
            String filePath = sdIconStorageDir.toString() + stored_image_counter ;
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            stored_image_counter++;
            stored_image_path[stored_image_counter] = filePath;
//            imagePathList.add(filePath);

            Log.d("MainActivity","store():img_path:"+filePath);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            //choose another format if PNG doesn't suit you
            imageData.compress(Bitmap.CompressFormat.PNG, 100, bos);

            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean checkPermission() {


        int perm_write = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);


        return (perm_write == PackageManager.PERMISSION_GRANTED);

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{
                        WRITE_EXTERNAL_STORAGE,
                },
                PERMISSION_REQUEST_CODE);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted ) {
                        Toast.makeText(MainActivity.this, "Permission Granted, Now you can access location data/camera/storage/accounts", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Your app might not work without permissions. Please restart your app.", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ WRITE_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        }
                                );
                                return;
                            }
                        }
                    }
                }

                break;
        }
    }

    class DownloadImages extends AsyncTask<Void, Void, String[][]> {

        @Override
        protected void onPreExecute()
        {
//            progressDialog.show();
            bitmap[0] = null;
            Toast.makeText(MainActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String[][] doInBackground(Void... args) {
            // Execute the HTTP request on background task
            Log.d("MainActivity", "Asynch:doInBackground()");
            bitmap[0] = Utility.getBitmapFromURL(url);
            storeSuccess[0] = storeImage(bitmap[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String[][] latlon) {
            if (storeSuccess[0])
                img.setImageBitmap(bitmap[0]);
            else
                Toast.makeText(getApplicationContext(), "Problem in Storing Image", Toast.LENGTH_SHORT).show();
        }
    }


}
//building,tree, snow, sea, station,long tree, rocks, wolf