package com.example.opencvappm4m;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;

import org.opencv.core.* ;
import org.opencv.core.Scalar ;
import org.opencv.imgproc.Imgproc ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ArtificialVision extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {



    // -------------------------  GENERAL DECLARATIONS  --------------------------------------------

    CameraBridgeViewBase cameraBridgeViewBase ;
    BaseLoaderCallback baseLoaderCallback ;

    TextView mTvBufferIn ;

    Button mBtnDesconectar ;

    Handler bluetoothIn ;

    private ConnectedThread MyConexionBT ;

    final int handlerState = 0 ;
    private BluetoothAdapter btAdapter = null ;
    private BluetoothSocket btSocket = null ;
    private StringBuilder DataStringIN = new StringBuilder() ;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") ;

    private Mat rgbFrame ;

    private long ptRightX ;
    private long ptLeftX ;
    private long ptCenterX ;
    private Point ptRight ;
    private Point ptLeft ;




    // -------------------------  ON CREATE  -------------------------------------------------------

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.artificial_vision) ;

        // Get the camera view from the layout using ID and enter them in a variable.
        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.cameraViewOpencv) ;
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE) ;
        cameraBridgeViewBase.setCvCameraViewListener(this) ;

        mBtnDesconectar = findViewById(R.id.BtnDesconectar) ;
        mTvBufferIn = findViewById(R.id.TvBufferIn) ;


        // Starts camera activity in its view.
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



        // It will receive Bluetooth data from external device.
        bluetoothIn = new Handler() {

            @SuppressLint("SetTextI18n")
            public void handleMessage(android.os.Message msg) {


                if (msg.what == handlerState) {

                    String readMessage = (String) msg.obj ;
                    DataStringIN.append(readMessage) ;

                    int endOfLineIndex = DataStringIN.indexOf("#") ;

                    if (endOfLineIndex > 0) {

                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex) ;
                        mTvBufferIn.setText(getString(R.string.Txt_dataSend) + dataInPrint) ;
                        DataStringIN.delete(0, DataStringIN.length()) ;

                    }

                }
            }

        };


        // Get the Bluetooth adapter.
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Button action to disconnect bluetooth socket connection.
        mBtnDesconectar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (btSocket!=null) {

                    try {

                        btSocket.close();

                    } catch (IOException e) {

                        Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();

                    }
                }

                finish();
            }
        });



    }



    // Creates a safe exit's connection for the device using UUID service.
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID) ;

    }



    @Override
    protected void onResume() {
        super.onResume();

        // Initializes OpenCV loaded version and check if there are errors.
        if(!OpenCVLoader.initDebug()) {

            Toast.makeText(this, "There is a problem with OpenCV service", Toast.LENGTH_SHORT).show();

        } else {

            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);

        }


        // -------------------------  BLUETOOTH CONNECTION PART  -----------------------------------

        // Gets the MAC direction from DeviceListActivity via intent.

        Intent intent = getIntent() ;


        // Gets the MAC direction from DeviceListActivity via EXTRA_DEVICE_ADDRESS.

        String address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS) ;


        //Sets the Mac address to a Bluetooth device variable.

        BluetoothDevice device = btAdapter.getRemoteDevice(address) ;


        try {

            btSocket = createBluetoothSocket(device) ;

        } catch (IOException e) {

            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();

        }


        // Establish the connection with the Bluetooth socket.

        try {

            btSocket.connect() ;

        } catch (IOException e) {

            try {

                btSocket.close() ;

            } catch (IOException e2) {
                Toast.makeText(this, "Something was wrong with Bluetooth connection", Toast.LENGTH_SHORT).show();
            }

        }

        MyConexionBT = new ConnectedThread(btSocket) ;
        MyConexionBT.start() ;


    }



    // -------------------------  ON PAUSE AND ON DESTROY  -----------------------------------------

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


        try {

            // This part allows the socket not to be open after the app is closed.

            btSocket.close() ;

        } catch (IOException e2) {

            Toast.makeText(this, "Something was wrong", Toast.LENGTH_SHORT).show();

        }


    }



    // -------------------------  CAMERA VIEWS PART  ------------------------------------------------

    @Override
    public void onCameraViewStarted(int width, int height) {

    }



    @Override
    public void onCameraViewStopped() {

    }



    // -------------------------  WORKING IN FRAMES PART  ------------------------------------------

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


        // -------------------------  PRINCIPAL DECLARATIONS  --------------------------------------

        // Putting de frame in variables.
        if (rgbFrame != null) {

            rgbFrame.release() ;
            rgbFrame = inputFrame.rgba() ;

        } else {

            rgbFrame = inputFrame.rgba();

        }

        Mat grayFrame = inputFrame.gray();

        Mat workFrame = new Mat();


        // Define colors.
        Scalar colorBlue = new Scalar(10, 255, 255);
        Scalar colorPink = new Scalar(380, 0, 300);
        Scalar colorGreen = new Scalar(100, 200 , 50) ;
        Scalar colorBlue2 = new Scalar(10, 100, 255) ;

        // Variables for define changes in canny parameters.
        double alpha = 120;
        double beta = 100;


        // -------------------------  CANNY PART  --------------------------------------------------

        // Applies Canny function to the frame.
        // Parentheses content (from where, to where, alpha value, beta value).
        Imgproc.Canny(grayFrame, workFrame, alpha, beta);


        // -------------------------  MASK PART  ---------------------------------------------------

        // It will create a mask by workframe.
        Mat mask = new Mat(grayFrame.rows(), grayFrame.cols(), grayFrame.type(), Scalar.all(0));

        // Drawing a rectangle (region of interest).
        Imgproc.rectangle(mask, new Point(100, 860), new Point(1820, 460), colorPink, -1, 8, 0);

        Mat maskArea = new Mat();


        workFrame.copyTo(maskArea, mask);


        // -------------------------  DETECTION LINES PART  ----------------------------------------

        // Standard Hough Line Transform
        Mat lines = new Mat(); // will hold the results of the detection

        Imgproc.HoughLinesP(maskArea, lines, 1, Math.PI/30, 100, 150, 10); // runs the actual detection

        // Draw the lines
        for (int x = 0; x < lines.rows(); x++) {

            double[] l = lines.get(x, 0);

            // Theta will be line's slope.
            double theta = (l[2]-l[0]) / (l[3]-l[1]) ;


            // If theta is positive it's a right line.
            if (theta>0 && theta<1.8) {

                // Calculate half of left line.
                ptRightX = Math.round(l[0] + ( (l[2]-l[0])/2) ) ;

                // Define right point.
                ptRight = new Point(ptRightX, 630) ;

                // Draw all founded lines.
                Imgproc.line(rgbFrame, new Point(l[0], l[1]), new Point(l[2], l[3]), colorBlue, 3, Imgproc.LINE_AA, 0);

            }

            // If theta is negative it's a left line.
            if (theta>-1.8 && theta<0 ) {

                // Calculate half of left line.
                ptLeftX = Math.round( l[0] + ((l[2]-l[0])/2) ) ;

                // Define left point.
                ptLeft = new Point(ptLeftX, 630) ;

                // Draw all founded lines.
                Imgproc.line(rgbFrame, new Point(l[0], l[1]), new Point(l[2], l[3]), colorBlue, 3, Imgproc.LINE_AA, 0);

            }


            // -------------------------  DRAW SOME LINES AND DEFINE CENTER POINT  -----------------

            if (ptLeft != null && ptRight != null) {

                ptCenterX = ptLeftX + (ptRightX - ptLeftX)/2 ;



                // Draw a line between the two rails found.
                Imgproc.line(rgbFrame, ptRight, ptLeft, colorPink, 3, Imgproc.LINE_AA, 0);

                // Draw a line for the line between's center.
                Imgproc.line(rgbFrame, new Point(ptCenterX, 570), new Point(ptCenterX, 690), colorGreen,3, Imgproc.LINE_AA, 0);


            }

            // Draw a reference line at center of X axis.
            Imgproc.line(rgbFrame, new Point(960, 570), new Point(960, 690), colorBlue2,3, Imgproc.LINE_AA, 0);


            // -------------------------  DECISIONS FOR THE CAR CONTROLS  --------------------------

            if (ptCenterX>=0 && ptCenterX<=660) {

                //MyConexionBT.write(getString(R.string.Txt_Left));
                mTvBufferIn.setText(getString(R.string.Txt_Left));

            }else if (ptCenterX>660 && ptCenterX<960) {

                //MyConexionBT.write(getString(R.string.Txt_LittleLeft));
                mTvBufferIn.setText(getString(R.string.Txt_LittleLeft)) ;

            } else if (ptCenterX>960 && ptCenterX<1260) {

                //MyConexionBT.write(getString(R.string.Txt_LittleRight));
                mTvBufferIn.setText(getString(R.string.Txt_LittleRight)) ;

            } else if (ptCenterX>=1260 && ptCenterX<=1920) {

                //MyConexionBT.write(getString(R.string.Txt_Right));
                mTvBufferIn.setText(getString(R.string.Txt_Right)) ;

            }



        }



        // -------------------------  GARBAGE COLLECTOR PART  ---------------------------------------

        maskArea.release() ;
        mask.release() ;
        grayFrame.release() ;
        workFrame.release() ;
        lines.release() ;

        System.gc();


        // -------------------------  Show results  ------------------------------------------------

        Imgproc.rectangle(rgbFrame, new Point(100, 860), new Point(1820, 460), colorPink);

        return rgbFrame;

    }



    // -------------------------  CALLED FUNCTIONS  ------------------------------------------------

    // Creates a class that allows create the connection event.
    private class ConnectedThread extends Thread {

        private final InputStream mmInStream ;
        private final OutputStream mmOutStream ;

        private ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null ;
            OutputStream tmpOut = null ;


            try {

                tmpIn = socket.getInputStream() ;
                tmpOut = socket.getOutputStream() ;

            } catch (IOException e) {

                Toast.makeText(ArtificialVision.this, "Something was wrong with Bluetooth socket", Toast.LENGTH_SHORT).show();

            }


            mmInStream = tmpIn ;
            mmOutStream = tmpOut ;

        }


        // It's for get data from connection with external devices.
        public void run() {

            byte[] buffer = new byte[256] ;
            int bytes ;


            // It remains in listening mode to determine data entry.
            while (true) {

                try {

                    bytes = mmInStream.read(buffer) ;
                    String readMessage = new String(buffer, 0, bytes) ;


                    // Sends the obtaining data to the event via Handler.
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget() ;

                } catch (IOException e) {

                    break ;

                }

            }

        }



        // Data send by bluetooth connection.

        private void write(String input) {

            try {

                mmOutStream.write(input.getBytes()) ;

            }
            catch (IOException e) {

                //If it isn't possible send data, connection will be close.

                Toast.makeText(getBaseContext(), "Failed connection", Toast.LENGTH_LONG).show() ;
                finish() ;

            }

        }




    }





}
