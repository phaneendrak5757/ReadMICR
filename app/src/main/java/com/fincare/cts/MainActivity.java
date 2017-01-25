package com.fincare.cts;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Constants;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button readMIRC,c_fnt,c_bak,upload;
    private EditText mirc;
    private Bitmap img,f_img,b_img;
    private static final int REQUEST_CODE = 99;
    private static final int REQUEST_CODE_f = 98;
    private static final int REQUEST_CODE_b = 97;


    String f_pngfile,b_pngfile;


    //--------------------------for tess-two----------------

    public static final String PACKAGE_NAME = "com.fincare.CTS";
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";


    public static final String lang = "mcr";

    private static final String TAG = "Fincare.CTS";
    protected String _path;
    protected boolean _taken;

    protected static final String PHOTO_TAKEN = "photo_taken";

    //--------------------------end tess-two-----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
        _path = DATA_PATH + "/ocr.jpg";

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        mirc=(EditText) findViewById(R.id.mirc);
        readMIRC=(Button) findViewById(R.id.readMIRC);
        c_fnt=(Button) findViewById(R.id.c_fnt);
        c_bak=(Button) findViewById(R.id.c_bak);

        upload=(Button) findViewById(R.id.upload);



        readMIRC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        c_fnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                startActivityForResult(intent, REQUEST_CODE_f);
            }
        });


        c_bak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                startActivityForResult(intent, REQUEST_CODE_b);
            }
        });

        upload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                uploadMultipart();
            }
        });

    }


    public void uploadMultipart() {
        //getting name for the image
        String name = mirc.getText().toString().trim();

        //getting the actual path of the image
        //String f_path = getPath(Uri.parse(new File(f_pngfile).toString()));
        //String b_path = getPath(Uri.parse(new File(b_pngfile).toString()));

        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, Globals.UPLOAD_URL)
                    .addFileToUpload(f_pngfile, "f_image") //Adding file
                    .addFileToUpload(b_pngfile, "b_image") //Adding file
                    .addParameter("mcir", name) //Adding text parameter to the request
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String getPath(Uri uri) {

        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        Log.d(TAG,imagePath);
        return cursor.getString(column_index);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
                img=bitmap;


                Log.v(TAG, "Before baseApi");

                TessBaseAPI baseApi = new TessBaseAPI();
                baseApi.setDebug(true);
                baseApi.init(DATA_PATH, lang);




                baseApi.setImage(bitmap);

                String recognizedText = baseApi.getUTF8Text();

                baseApi.end();

                if ( lang.equalsIgnoreCase("mcr") ) {
                    recognizedText = recognizedText.replaceAll("[^0-9]+", "-");
                }

                recognizedText = recognizedText.trim();

                if ( recognizedText.length() != 0 ) {
                    mirc.setText(recognizedText);
                    Log.d(TAG,recognizedText);
                }




            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_CODE_f && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
                f_img = bitmap;
                String name = mirc.getText().toString().trim();


                File file1 = new File(Environment.getExternalStorageDirectory() +"/scanDocs");
                file1.mkdir();
                f_pngfile = Environment.getExternalStorageDirectory() +"/scanDocs/f_"+name+".jpg";




                File file = new File(f_pngfile);

                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_CODE_b && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
                b_img = bitmap;


                String name = mirc.getText().toString().trim();

                File file1 = new File(Environment.getExternalStorageDirectory() +"/scanDocs");
                file1.mkdir();
                b_pngfile = Environment.getExternalStorageDirectory() +"/scanDocs/b_"+name+".jpg";
                File file = new File(b_pngfile);

                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
