package com.example.pavitrakumar.ocr;

/**
 * Created by Pavitrakumar on 28/8/2016.
 */
import java.io.File;

import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

public class TessOCR {
    private TessBaseAPI mTess;

    public TessOCR() {
        // TODO Auto-generated constructor stub
        mTess = new TessBaseAPI();
        String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
        String language = "eng";
        File dir = new File(datapath + "tessdata/");
        if (!dir.exists())
            dir.mkdirs();
        mTess.init(datapath, language);
    }

    public String getOCRResult(Bitmap bitmap) {
        mTess.setImage(bitmap);
        String result = mTess.getUTF8Text();

        return result;
    }

    public void onDestroy() {
        if (mTess != null)
            mTess.end();
    }

}