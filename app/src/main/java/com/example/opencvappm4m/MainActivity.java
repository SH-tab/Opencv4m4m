package com.example.opencvappm4m;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {



    CameraBridgeViewBase cameraBridgeViewBase ;
    BaseLoaderCallback baseLoaderCallback ;


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
        return inputFrame.gray();
    }





}
