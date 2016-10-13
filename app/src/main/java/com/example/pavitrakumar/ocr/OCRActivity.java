package com.example.pavitrakumar.ocr;

import android.content.res.AssetManager;
import android.media.Image;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
/*
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;
*/

public class OCRActivity extends Activity implements OnClickListener {
    public TessOCR mTessOCR;
    public TextView mResult;
    public ProgressDialog mProgressDialog;
    public static ImageView mImage,mImage2;
    public Button mButtonGallery, mButtonCamera, mShare,testButton,mDoOCR, mSettings, mDriveSave;
    public String mCurrentPhotoPath;
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_PICK_PHOTO = 2;
    public static final int REQUEST_CROP_PHOTO = 3;
    public static String recognized_text = "";
    public static Bitmap cropped_image;
    public static Bitmap processed_image;

    public static Bitmap 				myBitmap;
    public static Bitmap 				workingBitmap;
    public static Mat 				 	mrgba;
    public static Mat  					mIntermediateMat;
    public ImageView 				myImage;

    public static int A_threhsold_pix_neigh = 15;
    public static int A_threshold_c = 2;
    public static boolean do_Erode = false;
    public static boolean do_Dilate = false;
    public static int erode_kernel_size = 5;
    public static int dilate_kernel_size = 3;

    public static int ocr_process_running = 0;
    public static int ocr_done_once = 0;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    mrgba = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private void copyFileOrDir(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = "/data/data/" + this.getPackageName() + "/" + path;
                Log.d("PATH",fullPath);
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName =Environment.getExternalStorageDirectory() + "/" + filename;
            Log.d("PATH--INFILE",filename);
            Log.d("PATH--OUTFILE",newFileName);

            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        View b1 = findViewById(R.id.shareButton);
        b1.setVisibility(View.GONE);

        View b2 = findViewById(R.id.settingsButton);
        b2.setVisibility(View.GONE);

        View b3 = findViewById(R.id.ocrButton);
        b3.setEnabled(false);
*/


        String datapath = Environment.getExternalStorageDirectory() + "/tesseract/tessdata/";
        File file = new File(datapath, "eng.traineddata" );
        if (!file.exists()) {
            Log.d("FILE DOES NOT EXIst","no file");
            copyFileOrDir("tesseract");

        }


        mResult = (TextView) findViewById(R.id.tv_result);
        mResult.setText(" ");
        mImage = (ImageView) findViewById(R.id.image);
        mImage2 = (ImageView) findViewById(R.id.image2);

       // mButtonGallery = (Button) findViewById(R.id.bt_gallery);
       // mButtonGallery.setOnClickListener(this);
       // mButtonCamera = (Button) findViewById(R.id.bt_camera);
       // mButtonCamera.setOnClickListener(this);
        mShare = (Button) findViewById(R.id.shareButton);
        mShare.setOnClickListener(this);
        testButton = (Button) findViewById(R.id.selectImageButton);
        testButton.setOnClickListener(this);
        mDoOCR = (Button) findViewById(R.id.ocrButton);
        mDoOCR.setOnClickListener(this);
        mSettings = (Button) findViewById(R.id.settingsButton);
        mSettings.setOnClickListener(this);
        mTessOCR = new TessOCR();

        mDriveSave = (Button) findViewById(R.id.saveToDriveButton);
        mDriveSave.setOnClickListener(this);
    }

    private void uriOCR(Uri uri) {
        if (uri != null) {
            InputStream is = null;
            try {
                is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                mImage.setImageBitmap(bitmap);
                doOCR(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri uri = (Uri) intent
                    .getParcelableExtra(Intent.EXTRA_STREAM);
            uriOCR(uri);
        }
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
//        if(mProgressDialog==null){
  //          mProgressDialog.dismiss();
    //    }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        mTessOCR.onDestroy();
    }

    @Override
    protected void onStop(){
        super.onStop();

    }

    public Bitmap preProcess(Bitmap myBitmap)  {
        //image pre processing
        final Bitmap temp = myBitmap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImage.setImageBitmap(temp);
            }
        });
        //mImage.setImageBitmap(myBitmap);


        Log.d("INFO:","Bitmap size:"+Integer.toString(myBitmap.getHeight())+","+Integer.toString(myBitmap.getWidth()));

        Utils.bitmapToMat(myBitmap, mrgba);

        Log.d("INFO:","size of mat"+Integer.toString(mrgba.rows())+","+Integer.toString(mrgba.cols()));

        workingBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(workingBitmap, mrgba);

        Imgproc.cvtColor(mrgba, mrgba, Imgproc.COLOR_RGB2GRAY,0);

        Log.d("INFO:","size of mat after cvtcolor"+Integer.toString(mrgba.height())+","+Integer.toString(mrgba.width()));



        //Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types

        //Log.d("INFO:","size of mat"+Integer.toString(mrgba.rows())+","+Integer.toString(mrgba.cols()));
        //workingBitmap = Bitmap.createBitmap(mrgba.cols(), mrgba.rows(), conf);

        //Utils.matToBitmap(mrgba, workingBitmap);

        //Log.d("INFO:","converted Bitmap size:"+Integer.toString(workingBitmap.getHeight())+","+Integer.toString(workingBitmap.getWidth()));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImage2.setImageBitmap(workingBitmap);
            }
        });

        // }
        // if(THRESHOLD){
        //Imgproc.GaussianBlur(mrgba, mrgba, new Size(5,5),15);

        //int erosion_size = 5;
        //int dilation_size = 3;



        //Imgproc.erode(mrgba, mrgba, element);
        //Imgproc.dilate(mrgba, mrgba, element);
        //Imgproc.erode(mrgba, mrgba, element);

        //Imgproc.cvtColor(mrgba, mrgba, Imgproc.COLOR_RGB2GRAY,3);
        //Imgproc.threshold(mrgba, mrgba,50,255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        //                                                                                                                 odd value,any-value
        Imgproc.adaptiveThreshold(mrgba, mrgba, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,A_threhsold_pix_neigh,A_threshold_c);
        Imgproc.medianBlur(mrgba,mrgba,7);
        if(do_Dilate) {
            Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(dilate_kernel_size, dilate_kernel_size));

            Imgproc.dilate(mrgba, mrgba, element1);
        }
        if(do_Erode) {
            Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(erode_kernel_size, erode_kernel_size));

            Imgproc.erode(mrgba, mrgba, element2);
        }

        // }

        Utils.matToBitmap(mrgba, workingBitmap);
        //myImage.setImageBitmap(workingBitmap);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImage2.setImageBitmap(workingBitmap);
            }
        });
        return workingBitmap;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * http://developer.android.com/training/camera/photobasics.html
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String storageDir = Environment.getExternalStorageDirectory()
                + "/TessOCR";
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        File image = new File(storageDir + "/" + imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_TAKE_PHOTO
                && resultCode == Activity.RESULT_OK) {
            setPic();
        }
        else if (requestCode == REQUEST_PICK_PHOTO
                && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                uriOCR(uri);
            }
        }else if (requestCode == REQUEST_CROP_PHOTO && resultCode == Activity.RESULT_OK){
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);

                mImage.setImageBitmap(bitmap);
                cropped_image = bitmap;
                //doOCR(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor << 1;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImage.setImageBitmap(bitmap);
        doOCR(bitmap);

    }

    /**
    NOTE: http://stackoverflow.com/questions/34933380/sha1-key-for-debug-release-android-studio-mac
     GENERATE DEBUG AND PUBLIC KEYS! ADD TO https://console.developers.google.com/apis/credentials/oauthclient?project=lunar-listener-145813
    **/
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        switch (id) {
            /*
            case R.id.bt_gallery:
                pickPhoto();
                mResult.setText("");
                break;
            case R.id.bt_camera:
                takePhoto();
                break;
                */
            case R.id.shareButton:
                if(ocr_process_running==1){
                    Toast toast = Toast.makeText(getApplicationContext(), "Wait for process to complete!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                Log.d("INFO:","button cliked!");
                if(recognized_text.equals("")){
                    Toast toast = Toast.makeText(getApplicationContext(), "Use the recognition feature first!", Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = recognized_text;
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
                break;
            case R.id.selectImageButton:
                //int REQUEST_CODE = 99;
                if(ocr_process_running==1){
                    Toast toast = Toast.makeText(getApplicationContext(), "Wait for process to complete!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                int preference = ScanConstants.OPEN_MEDIA;
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
                startActivityForResult(intent, REQUEST_CROP_PHOTO);
                break;
            case R.id.ocrButton:
                if(ocr_process_running==1){
                    Toast toast = Toast.makeText(getApplicationContext(), "Wait for process to complete!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if(cropped_image==null){
                    Toast toast = Toast.makeText(getApplicationContext(), "Select an Image!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }else {
                    Log.d("INFO(RUN):","SYSTEM BUSY!");
                    ocr_process_running = 1;
                    doOCR(preProcess(cropped_image));
                    //ocr_process_running = 0;
                    //Log.d("INFO(RUN):","SYSTEM FREE!");
                }
                break;
            case R.id.settingsButton:
                if(ocr_process_running==1){
                    Toast toast = Toast.makeText(getApplicationContext(), "Wait for process to complete!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if(cropped_image==null){
                    Toast toast = Toast.makeText(getApplicationContext(), "Select an Image!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }else {
                    Intent settIntent = new Intent(this, OCRSettings.class);
                    //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    //cropped_image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    //byte[] byteArray = stream.toByteArray();
                    //settIntent.putExtra("image", byteArray);
                    startActivity(settIntent);
                }
                break;
            case R.id.saveToDriveButton:
                //Intent settIntent = new Intent(this, saveToDrive.class);
                //settIntent.putExtra("OCR_TEXT","hello!");
                //startActivity(settIntent);

                if(ocr_process_running==1){
                    Toast toast = Toast.makeText(getApplicationContext(), "Wait for process to complete!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                if (ocr_done_once == 0){
                    Toast toast = Toast.makeText(getApplicationContext(), "Do OCR to get Text!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }


                if(cropped_image==null){
                    Toast toast = Toast.makeText(getApplicationContext(), "Select an Image!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }else {
                    Intent settIntent = new Intent(this, saveToDrive.class);
                    settIntent.putExtra("OCR_TEXT",recognized_text);
                    startActivity(settIntent);
                }
                break;
        }
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PHOTO);
    }

    private void takePhoto() {
        dispatchTakePictureIntent();
    }

    private void doOCR(final Bitmap bitmap) {
        mResult.setText("");
        if (mProgressDialog == null) {
//            mProgressDialog = ProgressDialog.show(this, "Processing",
  //                  "Doing OCR...", true);
        }
        else {
          //  mProgressDialog.show();
        }

        new Thread(new Runnable() {
            public void run() {
                //this is just for testing
               // preProcess(bitmap);
                //uncomment this for actual app

                final String result = mTessOCR.getOCRResult(bitmap);


                //final String result = "...";
                recognized_text = result;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (result != null && !result.equals("")) {
                            mResult.setText(result);

                        }
                        ocr_process_running = 0;
                        ocr_done_once = 1;
                        Log.d("INFO(RUN):","SYSTEM FREE!");
//                        mProgressDialog.dismiss();
                    }

                });

            };
        }).start();
    }
}