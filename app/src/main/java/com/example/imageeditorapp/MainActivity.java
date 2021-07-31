package com.example.imageeditorapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.imageeditorapp.Helper.CommonUtils;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.exifinterface.media.ExifInterface;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.imageeditorapp.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    ImageView userpic,delete,img_rotateright,img_rotateleft;
    Uri resultUri;
    Bitmap bitmap = null;
    final static int FLIP_VERTICAL = 1;
    final static int FLIP_HORIZONTAL = 2;
    TextView tv_text;
    File file;
    Uri selectedfile;
    ImageView save,open,info,crop;
    final int PIC_CROP = 1;
    private static final int SAF_CODE = 42;
    String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        // Here we are initialising
        // the text and image View
        save = findViewById(R.id.img_save);
        open = findViewById(R.id.img_open);
        info=findViewById(R.id.img_info);
        crop=findViewById(R.id.img_crop);
        userpic = findViewById(R.id.user_img);
        img_rotateright = findViewById(R.id.img_horizontal);
        img_rotateleft = findViewById(R.id.img_vertical);
        delete=findViewById(R.id.img_delete);
        tv_text=findViewById(R.id.tv_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           performCrop(selectedfile);

            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userpic.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_baseline_image_24));
            delete.setVisibility(View.GONE);
            tv_text.setVisibility(View.GONE);
            bitmap = null;
            }
        });
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent()
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
            }
        });

        userpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(bitmap == null) {
                   Toast.makeText(getApplicationContext(),"Please select an Image",Toast.LENGTH_LONG).show();

                }else{
                    //This functionality is to show the full view of image
                    new CommonUtils().showFullScreenImage(MainActivity.this, Uri.parse(String.valueOf(selectedfile)));

                }

            }
        });
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExif(selectedfile);

            }
        });

        img_rotateright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = flip(bitmap ,FLIP_HORIZONTAL);
                userpic.setImageBitmap(bitmap);

            }
        });
        img_rotateleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = flip(bitmap ,FLIP_VERTICAL);
                userpic.setImageBitmap(bitmap);

            }
        });

   save.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        alertDialogToSelectFolder();

    }
});


    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 123 ) {
                selectedfile = data.getData(); //The uri with the location of the file
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                userpic.setImageBitmap(bitmap);
                delete.setVisibility(View.VISIBLE);
                tv_text.setVisibility(View.VISIBLE);
            } else if(requestCode==SAF_CODE){
                Uri treeUri = data.getData();

                grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                saveBitmapAsFileInAppCache(treeUri, filename, bitmap);
            }
            else if (requestCode == PIC_CROP) {
                    if (data != null) {
                        // get the returned data
                        Bundle extras = data.getExtras();
                        // get the cropped bitmap
                        Bitmap selectedBitmap = extras.getParcelable("data");
                        bitmap = selectedBitmap;
                        userpic.setImageBitmap(bitmap);
                        delete.setVisibility(View.VISIBLE);
                        tv_text.setVisibility(View.VISIBLE);


                    }
                }




        }
    }



//functionality to flip image horizontally or vertically
    public static Bitmap flip(Bitmap src, int type) {
        // create new matrix for transformation
        Matrix matrix = new Matrix();
        // if vertical
        if(type == FLIP_VERTICAL) {
            matrix.preScale(1.0f, -1.0f);
        }
        // if horizonal
        else if(type == FLIP_HORIZONTAL) {
            matrix.preScale(-1.0f, 1.0f);
            // unknown type
        } else {
            return null;
        }

        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }
    //exif functionality to get all the image related details.
    void showExif(Uri photoUri){
        if(photoUri != null){

            ParcelFileDescriptor parcelFileDescriptor = null;

            try {
                parcelFileDescriptor = getContentResolver().openFileDescriptor(photoUri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                ExifInterface exifInterface = new ExifInterface(fileDescriptor);
                String exif="Exif: " + fileDescriptor.toString();
                exif += "\nIMAGE_LENGTH: " +
                        exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                exif += "\nIMAGE_WIDTH: " +
                        exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                exif += "\n DATETIME: " +
                        exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                exif += "\n TAG_MAKE: " +
                        exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                exif += "\n TAG_MODEL: " +
                        exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                exif += "\n TAG_ORIENTATION: " +
                        exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
                exif += "\n TAG_WHITE_BALANCE: " +
                        exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
                exif += "\n TAG_FOCAL_LENGTH: " +
                        exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                exif += "\n TAG_FLASH: " +
                        exifInterface.getAttribute(ExifInterface.TAG_FLASH);
                exif += "\nGPS related:";
                exif += "\n TAG_GPS_DATESTAMP: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
                exif += "\n TAG_GPS_TIMESTAMP: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
                exif += "\n TAG_GPS_LATITUDE: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                exif += "\n TAG_GPS_LATITUDE_REF: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                exif += "\n TAG_GPS_LONGITUDE: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

                parcelFileDescriptor.close();

                Toast.makeText(getApplicationContext(),
                        exif,
                        Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            }

            String strPhotoPath = photoUri.getPath();

        }else{
            Toast.makeText(getApplicationContext(),
                    "No Image Found",
                    Toast.LENGTH_LONG).show();
        }
    };

    //This functionality is for saving bitmap in cache.

    public DocumentFile saveBitmapAsFileInAppCache(Uri folderPath, String fileName, Bitmap img) {
        int COMPRESS_QUALITY = 100;
        DocumentFile bitmapFile = null;

        try {

            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, folderPath);
            bitmapFile = pickedDir.createFile("image/png", fileName);
            OutputStream out = getContentResolver().openOutputStream(bitmapFile.getUri());
            img.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, out);
            out.close();
        } catch (Exception e) {
            Log.e("CommonUtils ", "saveImgToCache error: " + bitmapFile.getName(), e);
        }
        return bitmapFile;

    }

//this functionality is for cropping image.
    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

//permission to open file directory
    private void requestSAFPermission(){

        final AlertDialog alert = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.storage_access))
                .setMessage(getString(R.string.root_dir))
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), SAF_CODE);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Toast.makeText(MainActivity.this, R.string.error,Toast.LENGTH_LONG).show();
                        dialogInterface.dismiss();
                        finish();
                    }
                }).show();
    }

//here we are using dialog to enter user preferred name to save image in selected folder.
    void alertDialogToSelectFolder(){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(MainActivity.this);
        alert.setMessage("Please enter the file name");

        alert.setView(edittext);

        alert.setPositiveButton("Yes ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                filename = edittext.getText().toString();
                 System.out.println("filename"+filename);
                 requestSAFPermission();
            }
        });

        alert.setNegativeButton("No ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

}