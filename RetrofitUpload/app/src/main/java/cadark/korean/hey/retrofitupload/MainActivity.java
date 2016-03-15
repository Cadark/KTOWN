package cadark.korean.hey.retrofitupload;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    private static final String ROOT_URL = "http://android2-001-site1.1tempurl.com/upload.php";
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

    private void insertImage(){
        Log.i(TAG, "Upload Image");
        final ProgressDialog loading = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);
        //Here we will handle the http request to insert user to mysql db
        //Creating a RestAdapter
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(ROOT_URL) //Setting the Root URL
                .build(); //Finally building the adapter

        //Creating object for our interface
        UploadAPI api = adapter.create(UploadAPI.class);

        //set image and name
        String image = getStringImage(curImg);
        //Getting Image Name
        //String name = editTextName.getText().toString().trim();
        String name = "testimage";

        //Defining the method insertuser of our interface
        api.insertImage(

                //Passing the values by getting it from editTexts
                image,
                name,

                //Creating an anonymous callback
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        //On success we will read the server's output using bufferedreader
                        //Creating a bufferedreader object
                        BufferedReader reader = null;

                        //An string to store output from the server
                        String output = "";

                        try {
                            //Initializing buffered reader
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));

                            //Reading the output in the string
                            output = reader.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //Displaying the output as a toast
                        Toast.makeText(MainActivity.this, output, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        //If any error occured displaying the error as toast
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        Log.d("ERROR_RETROFIT", )
                    }
                }
        );
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
                insertImage();
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
}
