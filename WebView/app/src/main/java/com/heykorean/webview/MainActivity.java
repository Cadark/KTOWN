package com.heykorean.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    private static final int MY_SOCKET_TIMEOUT_MS = 8000 ;
    private WebView wvShow;
    private Button valid, refuse;

    WebView webView;

    Dialog dialog2;

    private static Bitmap curImg;

    private String UPLOAD_URL = "http://android2-001-site1.1tempurl.com/upload.php";

    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }


    private void initView() {
        webView = (WebView) findViewById(R.id.wv_show);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("http://android2-001-site1.1tempurl.com/test_html.html");
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == WebAppInterface.SELECT_FILE) {
                Toast.makeText(getApplicationContext(), "Choose IMG Complete", Toast.LENGTH_SHORT).show();
                onSelectFromGalleryResult(data);

                SetImgIntoDialog();
            }
            else if (requestCode == WebAppInterface.REQUEST_CAMERA) {
                Toast.makeText(getApplicationContext(), "Take photo Complete", Toast.LENGTH_SHORT).show();
                onCaptureImageResult(data);

                SetImgIntoDialog();
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);

            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        curImg = thumbnail;
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);

        curImg = bm;
    }

    public void SetImgIntoDialog() {
        dialog2 = new Dialog(MainActivity.this);
        // Include dialog.xml file
        dialog2.setContentView(R.layout.dialog_display_img_layout);
        // Set dialog title
        dialog2.setTitle("Image Upload");
        //dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // set values for custom dialog components - text, image and button
        ImageView contentImg = (ImageView) dialog2.findViewById(R.id.contentUploadImg);
        contentImg.setImageBitmap(curImg);
        Button uploadImgBtn = (Button) dialog2.findViewById(R.id.uploadImgBtn);
        uploadImgBtn.setOnClickListener(this);
        Button cancelImgBtn = (Button) dialog2.findViewById(R.id.cancelImgBtn);
        cancelImgBtn.setOnClickListener(this);

        dialog2.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadImgBtn:
                Toast.makeText(getApplicationContext(), "Upload Img", Toast.LENGTH_SHORT).show();
                //uploadImage();
                new uploadImageAsyn().execute();
                dialog2.dismiss();
                break;

            case R.id.cancelImgBtn:
                dialog2.dismiss();
                break;
        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage() {
        //Showing the progress dialog
        Log.i(TAG, "Upload Image");
        final ProgressDialog loading = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.e(TAG,""+s.getBytes().length);
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        //set image to dog
                        //String linkTest = "http://android2-001-site1.1tempurl.com/img/uploads/208.png";
                        webView.loadUrl("javascript:add_image('"+s+"')");
                        Log.d("RES_LINKL", s);
                        Toast.makeText(MainActivity.this, "Response mess: " + s, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Log.e(TAG, "Volley Error: " +volleyError.networkResponse);
                        //Showing toast
                        try {

                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Err: " + e);
                        }

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(curImg);

                //Getting Image Name
                //String name = editTextName.getText().toString().trim();
                String name = "testimage";

                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, name);

                //returning parameters
                return params;
            }
        };
        //set time ( cannot time out err)
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    class uploadImageAsyn extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            final String[] link = {""};
            Log.i(TAG, "Upload Image");
            final ProgressDialog loading = ProgressDialog.show(getApplicationContext(), "Uploading...", "Please wait...", false, false);
            final StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Log.e(TAG,""+s.getBytes().length);
                            //Disimissing the progress dialog
                            loading.dismiss();
                            //Showing toast message of the response
                            //set image to dog
                            //String linkTest = "http://android2-001-site1.1tempurl.com/img/uploads/208.png";
                            //webView.loadUrl("javascript:add_image('"+s+"')");
                            //Log.d("RES_LINKL", s);
                            //Toast.makeText(MainActivity.this, "Response mess: " + s, Toast.LENGTH_LONG).show();
                            link[0] = s;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            //Dismissing the progress dialog
                            loading.dismiss();
                            Log.e(TAG, "Volley Error: " +volleyError.networkResponse);
                            //Showing toast
                            try {

                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Err: " + e);
                            }

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    //Converting Bitmap to String
                    String image = getStringImage(curImg);

                    //Getting Image Name
                    //String name = editTextName.getText().toString().trim();
                    String name = "testimage";

                    //Creating parameters
                    Map<String, String> params = new Hashtable<String, String>();

                    //Adding parameters
                    params.put(KEY_IMAGE, image);
                    params.put(KEY_NAME, name);

                    //returning parameters
                    return params;
                }
            };
            //Creating a Request Queue
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

            //Adding request to the queue
            requestQueue.add(stringRequest);

            return link[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            webView.loadUrl("javascript:add_image('"+s+"')");
            Log.d("RES_LINKL", s);
            Toast.makeText(MainActivity.this, "Response mess: " + s, Toast.LENGTH_LONG).show();
        }
    }
}
