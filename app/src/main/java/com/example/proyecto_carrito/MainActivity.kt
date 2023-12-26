package com.example.proyecto_carrito

//Paquetes a importar
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.materialswitch.MaterialSwitch
import java.io.IOException
import java.util.UUID

//Constante global -> verificar si el dispositivo BT (si es posible habilitar la comunicación bt en el dispositivo)
const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity() {
    //Variables para la comunicación Bluetooth

    //BluetoothAdapter
    lateinit var mBtAdapter: BluetoothAdapter
    var mAddressDevices: ArrayAdapter<String>? = null
    var mNameDevices: ArrayAdapter<String>? = null

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var m_bluetoothSocket: BluetoothSocket? = null

        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAddressDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        val btnActivar = findViewById<Button>(R.id.btnActivar)
        val btnDispositivosBt = findViewById<Button>(R.id.btnDispositivosBT)
        val btnLeft = findViewById<Button>(R.id.btnIzquierda)
        val btnRight = findViewById<Button>(R.id.btnDerecha)

//        controlar con botones
        val btnApagarRojo = findViewById<Button>(R.id.btnApagarRojo)
        val btnEncenderRojo = findViewById<Button>(R.id.btnEncenderRojo)
        val btnApagarVerde = findViewById<Button>(R.id.btnApagarVerde)
        val btnEncenderVerde = findViewById<Button>(R.id.btnEncenderVerde)

//        controlar con switches
        val swRojo = findViewById<MaterialSwitch>(R.id.swRojo)
        val swVerde = findViewById<MaterialSwitch>(R.id.swVerde)

        val btnConectar = findViewById<Button>(R.id.btnConectar)
        val btnDesconectar = findViewById<Button>(R.id.btnDesconectar)
        val btnAdelante = findViewById<Button>(R.id.btnAdelante)
        val btnAtras = findViewById<Button>(R.id.btnAtras)
        val SpinnerCar = findViewById<Spinner>(R.id.spinnerCarrito)

        //--------Código para proceder con la comunicación con el dispositivo Bluetooth------------
        val someActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == REQUEST_ENABLE_BT) {
                Log.i("MainActivity", "ACTIVIDAD REGISTRADA")
            }
        }

        //Inicializacion del bluetooth adapter
        mBtAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        //Checar si esta encendido o apagado
        if (mBtAdapter == null) {
            Toast.makeText(
                this,
                "Bluetooth no está disponible en este dipositivo",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(this, "Bluetooth está disponible en este dispositivo", Toast.LENGTH_LONG)
                .show()
        }
        //--------Código para proceder con la comunicación con el dispositivo Bluetooth------------

        //------------------------------------------------------------------------------------------

        //Boton Encender bluetooth
        btnActivar.setOnClickListener {
            if (mBtAdapter.isEnabled) {
                //Si ya está activado
                Toast.makeText(this, "Bluetooth ya se encuentra activado", Toast.LENGTH_LONG).show()
            } else {
                //Encender Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("MainActivity", "ActivityCompat#requestPermissions")
                }
                someActivityResultLauncher.launch(enableBtIntent)
            }
        }

        //Boton apagar bluetooth
        btnDesconectar.setOnClickListener {
            if (!mBtAdapter.isEnabled) {
                //Si ya está desactivado
                Toast.makeText(this, "Bluetooth ya se encuentra desactivado", Toast.LENGTH_LONG)
                    .show()
            } else {
                //Encender Bluetooth
                mBtAdapter.disable()
                Toast.makeText(this, "Se ha desactivado el bluetooth", Toast.LENGTH_LONG).show()
            }
        }

        //Boton dispositivos emparejados
        btnDispositivosBt.setOnClickListener {
            if (mBtAdapter.isEnabled) {

                val pairedDevices: Set<BluetoothDevice>? = mBtAdapter?.bondedDevices
                mAddressDevices!!.clear()
                mNameDevices!!.clear()

                pairedDevices?.forEach { device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    mAddressDevices!!.add(deviceHardwareAddress)
                    //........... EN ESTE PUNTO GUARDO LOS NOMBRE A MOSTRARSE EN EL COMBO BOX
                    mNameDevices!!.add(deviceName)
                }

                //ACTUALIZO LOS DISPOSITIVOS
                SpinnerCar.setAdapter(mNameDevices)
                //idSpinDisp.setAdapter(mNameDevices)
            } else {
                val noDevices = "Ningun dispositivo pudo ser emparejado"
                mAddressDevices!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                Toast.makeText(this, "Primero vincule un dispositivo bluetooth", Toast.LENGTH_LONG)
                    .show()
            }
        }

        btnConectar.setOnClickListener {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {

                    val IntValSpin = SpinnerCar.selectedItemPosition
                    m_address = mAddressDevices!!.getItem(IntValSpin).toString()
                    Toast.makeText(this, m_address, Toast.LENGTH_LONG).show()
                    // Cancel discovery because it otherwise slows down the connection.
                    mBtAdapter?.cancelDiscovery()
                    val device: BluetoothDevice = mBtAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()
                }

                Toast.makeText(this, "CONEXION EXITOSA", Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "CONEXION EXITOSA")

            } catch (e: IOException) {
                //connectSuccess = false
                e.printStackTrace()
                Toast.makeText(this, "ERROR DE CONEXION", Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "ERROR DE CONEXION")
            }
        }

//        apagar con botones
//        btnEncenderRojo.setOnClickListener {
//            sendCommand("U")
//        }
//
//        btnApagarRojo.setOnClickListener {
//            sendCommand("u")
//        }
//
//        btnEncenderVerde.setOnClickListener {
//            sendCommand("W")
//        }
//
//        btnApagarVerde.setOnClickListener {
//            sendCommand("w")
//        }

//        apagar con switches
        swRojo.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sendCommand("U")
            } else {
                sendCommand("u")
            }
        }

        swVerde.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sendCommand("W")
            } else {
                sendCommand("w")
            }
        }

        //------Avance el carrito-------
        var isMovingForward = false
        var isMovingBackward = false
        var isMovingLeft = false
        var isMovingRight = false

        //
        btnAdelante.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    sendCommand("F")
                    isMovingForward = true
                }
                MotionEvent.ACTION_UP -> {
                    if (isMovingForward) {
                        sendCommand("S")
                        isMovingForward = false
                    }
                }
            }
            true
        }

        btnAtras.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    sendCommand("B")
                    isMovingBackward = true
                }
                MotionEvent.ACTION_UP -> {
                    if (isMovingBackward) {
                        sendCommand("S")
                        isMovingBackward = false
                    }
                }
            }
            true
        }

        btnLeft.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    sendCommand("L")
                    isMovingLeft = true
                }
                MotionEvent.ACTION_UP -> {
                    if (isMovingLeft) {
                        sendCommand("S")
                        isMovingLeft = false
                    }
                }
            }
            true
        }

        btnRight.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    sendCommand("R")
                    isMovingRight = true
                }
                MotionEvent.ACTION_UP -> {
                    if (isMovingRight) {
                        sendCommand("S")
                        isMovingRight = false
                    }
                }
            }
            true
        }

    }

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}