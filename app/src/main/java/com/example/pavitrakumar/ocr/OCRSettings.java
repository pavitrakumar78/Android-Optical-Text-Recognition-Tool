package com.example.pavitrakumar.ocr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Pavitrakumar on 29/8/2016.
 */
public class OCRSettings extends Activity implements View.OnClickListener {

    static Bitmap processed_img;

    public Bitmap preProcess(Bitmap myBitmap,int A_threhsold_pix_neigh,int A_threshold_c,boolean do_erode,int erode_kernel_size,boolean do_dilate,int dilate_kernel_size)  {
        //image pre processing


        //Log.d("INFO:","Bitmap size:"+Integer.toString(myBitmap.getHeight())+","+Integer.toString(myBitmap.getWidth()));

        Utils.bitmapToMat(myBitmap, OCRActivity.mrgba);

        //Log.d("INFO:","size of mat"+Integer.toString(mrgba.rows())+","+Integer.toString(mrgba.cols()));

        OCRActivity.workingBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(OCRActivity.workingBitmap, OCRActivity.mrgba);

        Imgproc.cvtColor(OCRActivity.mrgba, OCRActivity.mrgba, Imgproc.COLOR_RGB2GRAY,0);

        Log.d("INFO(settings):","size of mat after cvtcolor"+Integer.toString( OCRActivity.mrgba.height())+","+Integer.toString( OCRActivity.mrgba.width()));



        //Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types

        //Log.d("INFO:","size of mat"+Integer.toString(mrgba.rows())+","+Integer.toString(mrgba.cols()));
        //workingBitmap = Bitmap.createBitmap(mrgba.cols(), mrgba.rows(), conf);

        //Utils.matToBitmap(mrgba, workingBitmap);

        //Log.d("INFO:","converted Bitmap size:"+Integer.toString(workingBitmap.getHeight())+","+Integer.toString(workingBitmap.getWidth()));

        // }
        // if(THRESHOLD){
        //Imgproc.GaussianBlur(mrgba, mrgba, new Size(5,5),15);

        //int erosion_size = 5;
        //int dilation_size = 3;

        Log.d("INFO(settings):",Integer.toString(dilate_kernel_size));
        Log.d("INFO(settings):",Integer.toString(erode_kernel_size));
        Log.d("INFO(settings):", Boolean.toString(do_dilate));
        Log.d("INFO(settings):", Boolean.toString(do_erode));
        Log.d("INFO(settings):","done init kernel");

        //Imgproc.erode(mrgba, mrgba, element);
        //Imgproc.dilate(mrgba, mrgba, element);
        //Imgproc.erode(mrgba, mrgba, element);

        //Imgproc.cvtColor(mrgba, mrgba, Imgproc.COLOR_RGB2GRAY,3);
        //Imgproc.threshold(mrgba, mrgba,50,255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        //
        Log.d("INFO(settings):","done b/w");
        //                                                                                                                                          odd value,any-value
        Imgproc.adaptiveThreshold(OCRActivity.mrgba, OCRActivity.mrgba, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,A_threhsold_pix_neigh,A_threshold_c);
        Imgproc.medianBlur(OCRActivity.mrgba,OCRActivity.mrgba,7);
        if(do_dilate) {
            Log.d("INFO(settings):","init and do dilate");
            Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilate_kernel_size, dilate_kernel_size));
            Imgproc.dilate(OCRActivity.mrgba, OCRActivity.mrgba, element1);
        }
        if(do_erode) {
            Log.d("INFO(settings):","init and do erode");
            Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(erode_kernel_size, erode_kernel_size));
            Imgproc.erode(OCRActivity.mrgba, OCRActivity.mrgba, element2);
        }
        Log.d("INFO(settings):","done erode/dilate");
        // }

        Utils.matToBitmap(OCRActivity.mrgba, OCRActivity.workingBitmap);
        Log.d("INFO(settings):","done!");
        //myImage.setImageBitmap(workingBitmap);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                im.setImageBitmap(OCRActivity.workingBitmap);
            }
        });
        Log.d("INFO(settings):","done setting image!");
        return OCRActivity.workingBitmap;
    }

    CheckBox ch_dilate;
    CheckBox ch_erode;

    EditText dilate_kernel_val;
    EditText erode_kernel_val;

    SeekBar sb2;
    SeekBar sb1;

    SeekBar sb3;
    SeekBar sb4;

    Button chk_image;
    Button do_ocr;

    Bitmap cropped_img_bitmap;
    ImageView im;

    TextView dilationVal,erosionVal,thresh_pix_neigh_val,thresh_c_val;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_settings_new);

        cropped_img_bitmap = OCRActivity.cropped_image;

        im = (ImageView) findViewById(R.id.imageView);

        dilationVal = (TextView) findViewById(R.id.dilationVal);
        erosionVal = (TextView) findViewById(R.id.erosionVal);

        dilationVal.setText(""+OCRActivity.dilate_kernel_size);
        erosionVal.setText(""+OCRActivity.erode_kernel_size);

        thresh_pix_neigh_val = (TextView) findViewById(R.id.textView);
        thresh_c_val = (TextView) findViewById(R.id.textView2);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                im.setImageBitmap(OCRActivity.cropped_image);
            }
        });

        sb1 = (SeekBar) findViewById(R.id.PixNeighSeekBar);
        sb1.setProgress(OCRActivity.A_threhsold_pix_neigh);
        //TextView tv1 = (TextView) findViewById(R.id.textView);
        thresh_pix_neigh_val.setText("Pixel Neighbour Size: "+sb1.getProgress() + "");
        sb1.setMax(50);
        sb1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
                int stepSize = 2;
                progress = ((int)Math.round(progress/stepSize))*stepSize +1;
                seekBar.setProgress(progress);
                //TextView tv1 = (TextView) findViewById(R.id.textView);
                thresh_pix_neigh_val.setText("Pixel Neighbour Size:  "+progress + "");

                //Toast.makeText(getApplicationContext(), String.valueOf(progress),Toast.LENGTH_LONG).show();

            }
        });

        sb2 = (SeekBar) findViewById(R.id.constantSeekBar);
        sb2.setProgress(OCRActivity.A_threshold_c);
        //TextView tv2 = (TextView) findViewById(R.id.textView2);
        thresh_c_val.setText("C (Constant to subtract from pixels): "+sb2.getProgress() + "");
        sb2.setMax(50);
        sb2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
                int stepSize = 1;
                progress = ((int)Math.round(progress/stepSize))*stepSize;
                seekBar.setProgress(progress);
                //TextView tv2 = (TextView) findViewById(R.id.textView2);
                thresh_c_val.setText("C (Constant to subtract from pixels): "+progress + "");
                //Toast.makeText(getApplicationContext(), String.valueOf(progress),Toast.LENGTH_LONG).show();

            }
        });

        sb3 = (SeekBar) findViewById(R.id.dilationSeekBar);
        sb3.setMax(20);
        sb3.setProgress(OCRActivity.dilate_kernel_size);
        sb3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
                seekBar.setProgress(progress);
                dilationVal.setText(progress + "");
            }
        });

        sb4 = (SeekBar) findViewById(R.id.erosionSeekBar);
        sb4.setMax(20);
        sb4.setProgress(OCRActivity.erode_kernel_size);
        sb4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
                seekBar.setProgress(progress);
                erosionVal.setText(progress + "");
            }
        });


        ch_dilate = (CheckBox) findViewById(R.id.DilateChBox);
        ch_dilate.setChecked(OCRActivity.do_Dilate);

        ch_erode = (CheckBox) findViewById(R.id.ErosionChBox);
        ch_erode.setChecked(OCRActivity.do_Erode);

        //dilate_kernel_val = (EditText) findViewById(R.id.dilationValue);
        //dilate_kernel_val.setText(""+OCRActivity.dilate_kernel_size);


        //erode_kernel_val = (EditText) findViewById(R.id.erosionValue);
        //erode_kernel_val.setText(""+OCRActivity.erode_kernel_size);

        //dilate_kernel_val.setEnabled(false);
        //erode_kernel_val.setEnabled(false);



        chk_image = (Button) findViewById(R.id.chkImageButton);
        chk_image.setOnClickListener(this);
        do_ocr = (Button) findViewById(R.id.goBackButton);
        do_ocr.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int dilate_kernel_size = 1;
        int erode_kernel_size = 1;
        switch(id){
            case R.id.chkImageButton:

                if(ch_dilate.isChecked()){
                    dilate_kernel_size = sb3.getProgress();
                    if (dilate_kernel_size==0){
                        dilate_kernel_size = 1;
                    }
                }
                if(ch_erode.isChecked()){
                    erode_kernel_size = sb4.getProgress();
                    if(erode_kernel_size==0){
                        erode_kernel_size = 1;
                    }
                }
                // preProcess(img_bitmap,sb1.getProgress(),sb2.getProgress(),ch_dilate.isChecked(),dilate_kernel_size,ch_erode.isChecked(),erode_kernel_size);
                OCRActivity.A_threhsold_pix_neigh = sb1.getProgress();
                OCRActivity.A_threshold_c = sb2.getProgress();
                OCRActivity.do_Erode = ch_erode.isChecked();
                OCRActivity.do_Dilate = ch_dilate.isChecked();
                OCRActivity.dilate_kernel_size = dilate_kernel_size;
                OCRActivity.erode_kernel_size = erode_kernel_size;
                Log.d("INFO","variables set!");
                this.processed_img = preProcess( cropped_img_bitmap,
                        OCRActivity.A_threhsold_pix_neigh,
                        OCRActivity.A_threshold_c,
                        OCRActivity.do_Erode,
                        OCRActivity.erode_kernel_size,
                        OCRActivity.do_Dilate,
                        OCRActivity.dilate_kernel_size);
                Log.d("INFO(settings):","done process method!");
                break;
            case R.id.goBackButton:
                if(ch_dilate.isChecked()){
                    dilate_kernel_size = sb3.getProgress();
                    if (dilate_kernel_size==0){
                        dilate_kernel_size = 1;
                    }
                }
                if(ch_erode.isChecked()){
                    erode_kernel_size = sb4.getProgress();
                    if(erode_kernel_size==0){
                        erode_kernel_size = 1;
                    }
                }
                OCRActivity.A_threhsold_pix_neigh = sb1.getProgress();
                OCRActivity.A_threshold_c = sb2.getProgress();
                OCRActivity.do_Erode = ch_erode.isChecked();
                OCRActivity.do_Dilate = ch_dilate.isChecked();
                OCRActivity.dilate_kernel_size = dilate_kernel_size;
                OCRActivity.erode_kernel_size = erode_kernel_size;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("INFO(settings)---->",Integer.toString(OCRActivity.cropped_image.getHeight()));
                        OCRActivity.mImage.setImageBitmap(OCRActivity.cropped_image);
                    }
                });
                //OCRActivity.processed_image = this.processed_img;

                //OCRActivity.mImage.setImageBitmap(OCRActivity.cropped_image);
                this.finish();
                //Intent ocrIntent = new Intent(this, OCRActivity.class);
                //startActivity(ocrIntent);
                break;

        }
    }
}

