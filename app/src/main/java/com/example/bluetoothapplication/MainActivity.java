package com.example.bluetoothapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTv, mPairedTv;
    ImageView mBLueIv;
    Button mOnBtn, mOffBtn, mDiscoverBtn, mPairedBtn;

    BluetoothAdapter mBlueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv = findViewById(R.id.pairedTv);
        mBLueIv = findViewById(R.id.bluetoothIv);
        mOnBtn = findViewById(R.id.onBtn);
        mOffBtn = findViewById(R.id.offBtn);
        mDiscoverBtn = findViewById(R.id.discoverableBtn);
        mPairedBtn = findViewById(R.id.pairedBtn);

        //Habilitar bluetooth desde la app
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        //Compruebo si el dispositivo es compatible con bluetooth

        if(mBlueAdapter == null){
            mStatusBlueTv.setText("Dispositivo no compatible con Bluetooth");
        }
        else{
            mStatusBlueTv.setText("Dispositivo compatible con Bluetooth");
        }

        //coloco la imagen dependiendo del estado del bluetooth(on/off)

        if(mBlueAdapter.isEnabled()){
            mBLueIv.setImageResource(R.drawable.ic_action_on);
        }
        else {
            mBLueIv.setImageResource(R.drawable.ic_action_off);
        }

        // click el el boton On
        mOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mBlueAdapter.isEnabled()){
                    //Cartel para permitir o denegar acceso a bluetooth

                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);

                }
                else showToast("Bluetooth ya esta activo");
            }
        });

        // Hacer que tu dispositivo sea reconocible con bluetooth
        mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mBlueAdapter.isDiscovering()){
                    showToast("Haciendo que tu dispositivo sea reconocible por otros");
                    Intent intent = new  Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    /* El tiempo que es visible son 2 minutos, para cambiar ese tiempo a 5 min:
                    * intent.putExtra(BluetootAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    * */
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });

        //off btn click
        mOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBlueAdapter.isEnabled()){
                    mBlueAdapter.disable();
                    showToast("Desactivando Bluetooth");
                    mBLueIv.setImageResource(R.drawable.ic_action_off);
                }
                else{
                    showToast("Bluetooth Desactivado");
                }
            }
        });

        //get paired devices btn click
        mPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBlueAdapter.isEnabled()) {
                    mPairedTv.setText("Dispositivos pareados: ");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        mPairedTv.append("\nDispoisitivo: " + device.getName() + "," + device);
                    }
                }
                else{
                    //el bluetooth esta apagado por lo que no se pueden otener dispositivos pareados
                    showToast("Activa el Bluetooth para obtener dispositivos pareados");
                }
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case(REQUEST_ENABLE_BT):
                if(resultCode == RESULT_OK){        //El usuario ha permitido el acceso a Bluetooth
                    //El bluetooth esta activado
                    mBLueIv.setImageResource(R.drawable.ic_action_on);
                    showToast("Bluetooth activado");
                }
                else{
                    //El usuario deniega el acceso a bluetooth
                    showToast("No se pudo activar el Bluetooth");

                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //toast message function
    private void showToast(String msg){
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show();
    }

}