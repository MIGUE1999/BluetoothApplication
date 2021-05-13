package com.example.bluetoothapplication;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTv, mPairedTv, mDispositivosEncontradosTv;
    ImageView mBLueIv;
    Button mOnBtn, mOffBtn, mDiscoverBtn, mPairedBtn, mDetectaBtn;

    public ArrayList<BluetoothDevice> dispositivosEncontrados;
    public BluetoothDeviceArrayAdapter mDeviceListAdapter;
    ListView lvDispositivosEncontrados;
    private boolean state = false;


    BluetoothAdapter mBlueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv = findViewById(R.id.pairedTv);
        mDispositivosEncontradosTv = findViewById(R.id.dispositivosEncontradosTv);
        mBLueIv = findViewById(R.id.bluetoothIv);
        mOnBtn = findViewById(R.id.onBtn);
        mOffBtn = findViewById(R.id.offBtn);
        mDiscoverBtn = findViewById(R.id.discoverableBtn);
        mPairedBtn = findViewById(R.id.pairedBtn);
        mDetectaBtn = findViewById(R.id.detectaBtn);
        lvDispositivosEncontrados = findViewById(R.id.lvNewDevices);
        dispositivosEncontrados = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            state  = true;
        }

        //habilito los botones para que una vez sean clickados hagan su funcion asociada
        mOnBtn.setOnClickListener(this);
        mOffBtn.setOnClickListener(this);
        mPairedBtn.setOnClickListener(this);
        mDiscoverBtn.setOnClickListener(this);
        mDetectaBtn.setOnClickListener(this);

        //Habilitar bluetooth desde la app
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        //Compruebo si el dispositivo es compatible con bluetooth
        muestraCompatibilidad();

        //coloco la imagen dependiendo del estado del bluetooth(on/off)
        muestraImagenBluetooth();

        //registrarEventosBluetooth();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        //mBlueAdapter.cancelDiscovery();
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




    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(mBlueAdapter.ACTION_STATE_CHANGED)) {

                final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (estado)
                {
                    // Apagado
                    case BluetoothAdapter.STATE_OFF:
                        mBLueIv.setImageResource(R.drawable.ic_action_off);
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    // Encendido
                    case BluetoothAdapter.STATE_ON:
                        mBLueIv.setImageResource(R.drawable.ic_action_on);
                        //Pido permiso para hacer visible el dispositivo
                        Intent discoverableintent = new  Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        /* El tiempo que es visible son 2 minutos, para cambiar ese tiempo a 5 min:
                         * intent.putExtra(BluetootAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                         * */
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        startActivityForResult(discoverableintent, REQUEST_DISCOVER_BT);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;

                    default:
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };


    /**
     * Broadcast Receiver for listing devices that are not yet paired
     *
     */

    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                dispositivosEncontrados.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new BluetoothDeviceArrayAdapter(context, R.layout.bluetooth_device_adapter_view, dispositivosEncontrados);
                lvDispositivosEncontrados.setAdapter(mDeviceListAdapter);
            }
        }
    };

    //Registra el evento del bluetooth con los filtros que hemos fijado en receiver
    private void registrarEventosBluetooth()
    {
        // Registramos el BroadcastReceiver que instanciamos previamente para
        // detectar las distintos acciones que queremos recibir
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filtro.addAction(BluetoothDevice.ACTION_FOUND);

        this.registerReceiver(mBroadcastReceiver1, filtro);
    }



    //toast message function
    private void showToast(String msg){
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.onBtn: {
                if (!mBlueAdapter.isEnabled()) {
                    //Cartel para permitir o denegar acceso a bluetooth
                    // Lanzamos el Intent que mostrara la interfaz de activacion del
                    // Bluetooth. La respuesta de este Intent se manejara en el metodo
                    // onActivityResult
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Log.d(TAG, "onClick: enabling bluetooth.");
                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1, BTIntent);

                } else{
                    showToast("Bluetooth ya esta activo");
                    Log.d(TAG, "onClick: enabling bluetooth.");
                }
                break;
            }
            case R.id.offBtn: {
                if (mBlueAdapter.isEnabled()) {
                    mBlueAdapter.disable();
                    showToast("Desactivando Bluetooth");
                    Log.d(TAG, "onClick: disabling bluetooth.");
                    mBLueIv.setImageResource(R.drawable.ic_action_off);
                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1, BTIntent);
                } else {
                    showToast("Bluetooth Desactivado");
                    Log.d(TAG, "onClick: Disabling bluetooth.");
                }
                break;
            }
            case R.id.pairedBtn: {
                if (mBlueAdapter.isEnabled()) {
                    mPairedTv.setText("Dispositivos emparejados: ");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        mPairedTv.append("\nDispoisitivo: " + device.getName() + ", MAC: " + device.getAddress());
                        Log.d(TAG, "onClick: pairedBtn ");

                    }
                } else {
                    //el bluetooth esta apagado por lo que no se pueden otener dispositivos pareados
                    showToast("Activa el Bluetooth para obtener dispositivos pareados");
                }
                break;
            }
            case R.id.discoverableBtn: {
                if (!mBlueAdapter.isDiscovering()) {
                    showToast("Haciendo que tu dispositivo sea reconocible por otros");
                    Log.d(TAG, "BotonDiscoverable: Haciendo que tu dispositivo sea reconocible por otros durante 2 minutos ");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    /* El tiempo que es visible son 2 minutos, para cambiar ese tiempo a 5 min:
                     * intent.putExtra(BluetootAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                     * */
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                    mBLueIv.setImageResource(R.drawable.ic_action_on);

                    IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                    registerReceiver(mBroadcastReceiver2,intentFilter);
                }

                break;
            }
            case R.id.detectaBtn: {
                Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

                if(mBlueAdapter.isDiscovering()){
                    mBlueAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Canceling discovery.");

                    //check BT permissions in manifest
                    checkBTPermissions();

                    mBlueAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
                if(!mBlueAdapter.isDiscovering()){

                    //check BT permissions in manifest
                    checkBTPermissions();
                    Log.d(TAG, "StartDiscovery: Esta buscando.");
                    mBlueAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }

                break;
            }

        }
    }

    public void muestraCompatibilidad() {
            if (mBlueAdapter == null) {
                mStatusBlueTv.setText("Dispositivo no compatible con Bluetooth");
            } else {
                mStatusBlueTv.setText("Dispositivo compatible con Bluetooth");
            }
        }

    public void muestraImagenBluetooth() {
        if (mBlueAdapter.isEnabled()) {
            mBLueIv.setImageResource(R.drawable.ic_action_on);
        } else {
            mBLueIv.setImageResource(R.drawable.ic_action_off);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}