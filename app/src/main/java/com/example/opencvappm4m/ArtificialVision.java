package com.example.opencvappm4m;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    final int handlerState = 0 ;
    private BluetoothAdapter btAdapter = null ;
    private BluetoothSocket btSocket = null ;
    private StringBuilder DataStringIN = new StringBuilder() ;
    private ConnectedThread MyConexionBT ;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") ;

    private static String address = null ;

    private Mat rgbFrame ;
    private Mat grayFrame ;


    // -------------------------  ON CREATE  -------------------------------------------------------

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


        bluetoothIn = new Handler() {

            public void handleMessage(android.os.Message msg) {

                if (msg.what == handlerState) {

                    String readMessage = (String) msg.obj ;
                    DataStringIN.append(readMessage) ;

                    int endOfLineIndex = DataStringIN.indexOf("#") ;

                    if (endOfLineIndex > 0) {

                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex) ;
                        mTvBufferIn.setText(getString(R.string.Txt_datoMandado) + dataInPrint) ;
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



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID) ;

    }



    @Override
    protected void onResume() {
        super.onResume();

        if(!OpenCVLoader.initDebug()) {

            Toast.makeText(this, "There is a problem with OpenCV service", Toast.LENGTH_SHORT).show();

        } else {

            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);

        }


        //Consigue la direccion MAC desde DeviceListActivity via intent

        Intent intent = getIntent() ;


        //Consigue la direccion MAC desde DeviceListActivity via EXTRA

        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS) ;


        //Setea la direccion MAC

        BluetoothDevice device = btAdapter.getRemoteDevice(address) ;


        try {

            btSocket = createBluetoothSocket(device) ;

        } catch (IOException e) {

            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();

        }


        // Establece la conexión con el socket Bluetooth.

        try {

            btSocket.connect() ;

        } catch (IOException e) {

            try {

                btSocket.close() ;

            } catch (IOException e2) {

            }

        }


        MyConexionBT = new ConnectedThread(btSocket) ;
        MyConexionBT.start() ;


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


        try {

            // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket

            btSocket.close() ;

        } catch (IOException e2) {

        }


    }



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

        grayFrame = inputFrame.gray();

        Mat workFrame = new Mat();


        // Define colors.
        Scalar colorBlue = new Scalar(10, 255, 255);
        Scalar colorPink = new Scalar(380, 0, 300);

        // Variables for define changes in canny parameters.
        double alpha = 100;
        double beta = 80;


        // -------------------------  CANNY PART  --------------------------------------------------

        // Converting rgbFrame to gray scale.
        //Imgproc.cvtColor(rgbFrame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        // Aply Canny function to the frame.
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
        Imgproc.HoughLines(workFrame, lines, 1, Math.PI / 15, 200, 0, 0, -Math.PI / 3, Math.PI / 3); // runs the actual detection


        // Draw the lines
        for (int x = 0; x < lines.rows(); x++) {

            double rho = lines.get(x, 0)[0];
            double theta = lines.get(x, 0)[1];
            double a = Math.cos(theta);
            double b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;

            Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
            Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));

            // Will draw lines on the frame we choose, in this case rgbFrame.
            Imgproc.line(rgbFrame, pt1, pt2, colorBlue, 3, Imgproc.LINE_AA, 0);

        }


        // -------------------------  GARBAGE COLECTOR PART  ---------------------------------------

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

    //Crea la clase que permite crear el evento de conexion

    private class ConnectedThread extends Thread {

        private final InputStream mmInStream ;
        private final OutputStream mmOutStream ;

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null ;
            OutputStream tmpOut = null ;


            try {

                tmpIn = socket.getInputStream() ;
                tmpOut = socket.getOutputStream() ;

            } catch (IOException e) {

            }


            mmInStream = tmpIn ;
            mmOutStream = tmpOut ;

        }



        public void run() {

            byte[] buffer = new byte[256] ;
            int bytes ;


            // Se mantiene en modo escucha para determinar el ingreso de datos

            while (true) {

                try {

                    bytes = mmInStream.read(buffer) ;
                    String readMessage = new String(buffer, 0, bytes) ;


                    // Envia los datos obtenidos hacia el evento via handler

                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget() ;

                } catch (IOException e) {

                    break ;

                }

            }

        }



        //Envio de trama

        public void write(String input) {

            try {

                mmOutStream.write(input.getBytes()) ;

            }
            catch (IOException e) {

                //si no es posible enviar datos se cierra la conexión

                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show() ;
                finish() ;

            }

        }




    }





}
