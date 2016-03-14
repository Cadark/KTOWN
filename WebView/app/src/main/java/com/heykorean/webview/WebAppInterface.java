package com.heykorean.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Hey!TheAnh on 3/8/2016.
 */
public class WebAppInterface {

    public static int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    Context mContext;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, "xin chao 1", Toast.LENGTH_SHORT).show();
        mContext.startActivity(new Intent(mContext, NewActivity.class));


    }

    @JavascriptInterface
    public void showCamera() {
        // Create custom dialog object
        final Dialog dialog = new Dialog(mContext);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_layout);
        // Set dialog title
        dialog.setTitle("Custom Dialog");
        //dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // set values for custom dialog components - text, image and button
        TextView text1 = (TextView) dialog.findViewById(R.id.takePhotoTv);
        text1.setText("Take Photo");
        TextView text2 = (TextView) dialog.findViewById(R.id.chooseGalleyTv);
        text2.setText("Gallery");
        TextView text3 = (TextView) dialog.findViewById(R.id.cancelTv);
        text3.setText("Cancel");

        dialog.show();

        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ((Activity) mContext).startActivityForResult(intent, REQUEST_CAMERA);

                dialog.dismiss();
            }
        });

        text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                ((Activity) mContext).startActivityForResult(
                        Intent.createChooser(intent, "Select File"),
                        SELECT_FILE);

                dialog.dismiss();
            }
        });

        text3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
