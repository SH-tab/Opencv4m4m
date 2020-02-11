package com.example.opencvappm4m;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {



    CameraBridgeViewBase cameraBridgeViewBase ;
    BaseLoaderCallback baseLoaderCallback ;

    // Variables definition.
    SeekBar mSkBbeta, mSkBalpha ;
    TextView mAlphaTv, mBetaTv ;
    int progressAlpha = 0 , progressBeta = 0 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_main) ;

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.cameraViewOpencv) ;
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE) ;
        cameraBridgeViewBase.setCvCameraViewListener(this) ;

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                if(status == BaseLoaderCallback.SUCCESS) {

                    cameraBridgeViewBase.enableView() ;

                } else {

                    super.onManagerConnected(status) ;

                }
            }
        } ;


        int MY_PERMISSIONS_REQUEST_CAMERA=0;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))
            {

            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA );

            }
        }

        mSkBalpha = findViewById(R.id.SkBalpha) ;
        mSkBalpha.setProgress(progressAlpha);
        mSkBalpha.setMax(400);

        mSkBbeta = findViewById(R.id.SkBbeta) ;
        mSkBbeta.setProgress(progressBeta);
        mSkBbeta.setMax(400);

        mAlphaTv = findViewById(R.id.AlphaTv) ;
        mAlphaTv.setText("alpha : " + progressAlpha);

        mBetaTv = findViewById(R.id.BetaTv) ;
        mBetaTv.setText("beta : " + progressBeta);

    }



    @Override
    protected void onResume() {
        super.onResume();

        if(!OpenCVLoader.initDebug()) {

            Toast.makeText(this, "There is a problem with OpenCV service", Toast.LENGTH_SHORT).show();

        } else {

            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);

        }

    }



    @Override
    protected void onPause() {
        super.onPause();

        if(cameraBridgeViewBase!=null) {

            cameraBridgeViewBase.disableView() ;

        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(cameraBridgeViewBase!=null) {

            cameraBridgeViewBase.disableView() ;

        }

    }



    @Override
    public void onCameraViewStarted(int width, int height) {

    }



    @Override
    public void onCameraViewStopped() {

    }




    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {



        // Variables for define changes in image's pixels.
        double alpha = 0;
        double beta = 0;

        // Here is SeekBars structure, this changes the values within "Cany" function by manual control in screen.l
        // This is alpha control SeekBar.
        mSkBalpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                // Introducing value from Seekbar to a variable.
                progressAlpha = i ;
                // Setting text and seekbar value to a textviex.
                mAlphaTv.setText("alpha : " + progressAlpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // This is beta control SeekBar.
        mSkBbeta.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int j, boolean fromUser) {
                progressBeta = j ;
                mBetaTv.setText("beta : " + progressBeta);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Putting de frame into a variable.
        Mat frame = inputFrame.gray() ;
        
        // Here we aply Canny function to the frame.
        // Parentheses content (from where, to where, alpha value, beta value). 
        Imgproc.Canny(frame, frame, progressAlpha,progressBeta) ;





        // Show results.
        return frame;
    }





}
