package com.example.opencvappm4m;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;



public class MainActivity extends AppCompatActivity {


    // Depuración de LOGCAT

    private static final String TAG = "DispositivosBT";


    // Declaracion de variables

    private BluetoothAdapter mBtAdapter;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    ListView mLv_pairedDevices;
    //Button mBtnConnect ;
    TextView mTv_BtnConnect ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {

        super.onResume();


        VerificarEstadoBT() ;

        CameraPermissions() ;


        // Inicializa la array que contendra la lista de los dispositivos bluetooth vinculados

        ArrayAdapter<String> mPairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.nombre_dispositivos);



        // Vinculacion para ListView y Finds

        mLv_pairedDevices = findViewById(R.id.Lv_pairedDevices);
        mLv_pairedDevices.setAdapter(mPairedDevicesArrayAdapter);
        mLv_pairedDevices.setOnItemClickListener(mDeviceClickListener);

        //mBtnConnect = findViewById(R.id.BtnConnect) ;
        mTv_BtnConnect = findViewById(R.id.Tv_BtnConnect) ;


        // Obtiene el adaptador local Bluetooth adapter

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        Set <BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        mPairedDevicesArrayAdapter.clear() ;


        // Añade los dispositivos ya emparejados al array

        if (pairedDevices.size() > 0) {

            mTv_BtnConnect.setText(getText(R.string.Txt_Tv_BtnConnect));

            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }

        }  else {

            mTv_BtnConnect.setText(getText(R.string.Txt_Tv_BtnConnect_no));

        }

    }

    // Configura un (on-click) para la lista

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            // Obtener la dirección MAC del dispositivo, que son los últimos 17 caracteres en la vista

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);



            // Realiza un intent para iniciar la siguiente actividad
            // mientras toma un EXTRA_DEVICE_ADDRESS que es la dirección MAC.

            Intent i = new Intent(MainActivity.this, ArtificialVision.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);

        }
    };



    // -------------------------  CALLED FUNCTIONS  ------------------------------------------------

    private void VerificarEstadoBT() {

        // Comprueba que el dispositivo tiene Bluetooth y que está encendido.

        mBtAdapter= BluetoothAdapter.getDefaultAdapter();

        if(mBtAdapter==null) {

            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();

        } else {

            if (mBtAdapter.isEnabled()) {

                Log.d(TAG, "...Bluetooth Activado...");

            } else {

                //Solicita al usuario que active Bluetooth

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }


    private void CameraPermissions() {

        int MY_PERMISSIONS_REQUEST_CAMERA=0;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                /* gg */

            } else {

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA );

            }

        }

    }



}

