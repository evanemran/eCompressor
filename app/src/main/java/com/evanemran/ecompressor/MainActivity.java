package com.evanemran.ecompressor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {
    public static final int RESULT_LOAD_IMG = 1;
    private PackageInfo mPackageInfo;
    ImageView imageView, imageOutput;
    EditText txtHeight, txtWidth;
    TextView txtOriginalSize, txtResultSize, txtQuality;
    SeekBar seekBar;
    Button btnCompress, btnPick;
    File compressedImage, originalImage;
    private static String filePath;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/eCompressor");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();
        try {
            mPackageInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            setupVersionInfo();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        imageView = findViewById(R.id.imageHolder);
        imageOutput = findViewById(R.id.imageOutput);
        btnCompress = findViewById(R.id.btnCompress);
        btnPick = findViewById(R.id.btnPick);
        txtOriginalSize = findViewById(R.id.txtOriginalSize);
        txtResultSize = findViewById(R.id.txtResultSize);
        txtHeight = findViewById(R.id.txtHeight);
        txtWidth = findViewById(R.id.txtWidth);
        txtQuality = findViewById(R.id.txtQuality);
        seekBar = findViewById(R.id.seekQuality);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());
        filePath =path.getAbsolutePath();

        if (!path.exists()){
            path.mkdirs();
        }

        seekBar.setProgress(40);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtQuality.setText("Quality: " + i + "%");
                seekBar.setMax(100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        btnCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isvalidParams()){
                    try {
//                    File file = new File(String.valueOf(imageView.getDrawable()));
                        int quality = seekBar.getProgress();
                        int width = Integer.parseInt(txtWidth.getText().toString());
                        int height = Integer.valueOf(txtHeight.getText().toString());
                        compressedImage = new Compressor(MainActivity.this)
                                .setMaxHeight(height)
                                .setMaxWidth(width)
                                .setQuality(quality)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .setDestinationDirectoryPath(filePath)
                                .compressToFile(originalImage);
                        File finalFile = new File(filePath , originalImage.getName());
                        Bitmap myBitmap = BitmapFactory.decodeFile(finalFile.getAbsolutePath());
                        imageOutput.setImageBitmap(myBitmap);
                        txtResultSize.setText("Size: " + Formatter.formatShortFileSize(MainActivity.this, finalFile.length()));
                        Toast.makeText(MainActivity.this, "Compressed & Saved to " + filePath, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean isvalidParams() {
        boolean validity = false;
        if (!txtHeight.getText().toString().isEmpty() && !txtWidth.getText().toString().isEmpty()){
            validity = true;
        }
        else{
            if (txtHeight.getText().toString().isEmpty()){
                Toast.makeText(this, "Enter Height!", Toast.LENGTH_SHORT).show();
                validity = false;
            }
            else if (txtWidth.getText().toString().isEmpty()){
                Toast.makeText(this, "Enter Width!", Toast.LENGTH_SHORT).show();
                validity = false;
            }
        }
        return validity;
    }

    private void askPermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void openGallery() {
//        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.setType("image/*");
//        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);

        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                btnCompress.setVisibility(View.VISIBLE);

//                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());

                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                originalImage = new File(imageUri.getPath().replace("raw/", ""));
                imageView.setImageBitmap(selectedImage);
                txtOriginalSize.setText("Size: " + Formatter.formatShortFileSize(this, originalImage.length()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(MainActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void setupVersionInfo() {

        if (mPackageInfo != null) {
            TextView versionInfoTextView = findViewById(R.id.versionInfoTextView);
            String vinfo = String.format("V: %s", mPackageInfo.versionName);
            versionInfoTextView.setText(vinfo);

        }

    }
}