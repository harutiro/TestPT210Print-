package net.harutiro.test_pt_210_print

import android.R.attr.data
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.android.print.sdk.PrinterConstants
import com.android.print.sdk.PrinterConstants.Connect
import com.android.print.sdk.PrinterInstance
import java.io.IOException


class PosViewModel(private val context: Context): ViewModel(){

    val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter = bluetoothManager.adapter
    val bluetoothDeviceList = mutableStateListOf<BluetoothDevice>()
    val printUtils = PrintUtils()

    val TAG = "PosViewModel"

    var printer:PrinterInstance? = null
    var mDevice:BluetoothDevice? = null
    var rePair = false
    var hasRegBoundReceiver = false

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            if (BluetoothDevice.ACTION_FOUND == action) {
//                bluetoothDeviceList.clear()
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    bluetoothDeviceList.add(it)
                    Log.d(TAG, it.name ?: "null")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getParingDeviceList(){
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        bluetoothDeviceList.clear()
        pairedDevices?.forEach { device ->
            Log.d(TAG, device.name ?:"null")
            bluetoothDeviceList.add(device)
        }
    }

    @SuppressLint("MissingPermission")
    fun startBluetoothSearch(context: Context){
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        context.startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    fun getBluetoothDeviceList(context: Context){
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun stopBluetoothSearch(context: Context){
        context.unregisterReceiver(receiver)
        bluetoothAdapter.cancelDiscovery()
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                PrinterConstants.Connect.SUCCESS -> {
                    // 接続成功
                    Toast.makeText(context, "接続成功", Toast.LENGTH_SHORT).show()
                }
                PrinterConstants.Connect.FAILED -> {
                    // 接続失敗
                    Toast.makeText(context, "接続失敗", Toast.LENGTH_SHORT).show()
                }
                PrinterConstants.Connect.CLOSED -> {
                    // 接続終了
                    Toast.makeText(context, "接続終了", Toast.LENGTH_SHORT).show()
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun open(device: BluetoothDevice){
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            Log.i(TAG, "device.getBondState() is BluetoothDevice.BOND_NONE")
            pairOrRePairDevice(false, device)
        } else if (mDevice?.getBondState() == BluetoothDevice.BOND_BONDED) {
            if (rePair) {
                pairOrRePairDevice(true, device)
            } else {
                openPrinter()
            }
        }
    }

    // receive bound broadcast to open connect.
    private val boundDeviceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val device = intent
                    .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (mDevice?.equals(device) == true) {
                    return
                }
                when (device!!.bondState) {
                    BluetoothDevice.BOND_BONDING -> Log.i(TAG, "bounding......")
                    BluetoothDevice.BOND_BONDED -> {
                        Log.i(TAG, "bound success")
                        // if bound success, auto init BluetoothPrinter. open
                        // connect.
                        if (hasRegBoundReceiver) {
                            context.unregisterReceiver(this)
                            hasRegBoundReceiver = false
                        }
                        openPrinter()
                    }

                    BluetoothDevice.BOND_NONE -> if (rePair) {
                        rePair = false
                        Log.i(TAG, "removeBond success, wait create bound.")
                        pairOrRePairDevice(false, device)
                    } else if (hasRegBoundReceiver) {
                        context.unregisterReceiver(this)
                        hasRegBoundReceiver = false
                        // bond failed
                        handler.obtainMessage(Connect.FAILED).sendToTarget()
                        Log.i(TAG, "bound cancel")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun pairOrRePairDevice(isRePair: Boolean, device: BluetoothDevice): Boolean {
        var success = false
        try {
            if (!hasRegBoundReceiver) {
                mDevice = device
                val boundFilter = IntentFilter(
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED
                )
                context.registerReceiver(boundDeviceReceiver, boundFilter)
                hasRegBoundReceiver = true
            }

            if (isRePair) {
                // cancel bond
                val removeBondMethod = BluetoothDevice::class.java
                    .getMethod("removeBond")
                success = removeBondMethod.invoke(device) as Boolean
                Log.i(TAG, "removeBond is success? : $success")
            } else {
                // Input password
                // Method setPinMethod =
                // BluetoothDevice.class.getMethod("setPin");
                // setPinMethod.invoke(device, 1234);
                // create bond
                val createBondMethod = BluetoothDevice::class.java
                    .getMethod("createBond")
                success = createBondMethod.invoke(device) as Boolean
                Log.i(TAG, "createBond is success? : $success")
            }
        } catch (e: java.lang.Exception) {
            Log.i(TAG, "removeBond or createBond failed.")
            e.printStackTrace()
            success = false
        }
        return success
    }

    // use device to init printer.
    private fun openPrinter() {
        printer = PrinterInstance(context, mDevice, handler)
        // default is gbk...
        // mPrinter.setEncoding("gbk");
        printer?.openConnection()
    }

    @SuppressLint("MissingPermission")
    fun print(device: BluetoothDevice?){

    }

    fun printTest(device: BluetoothDevice?) {
        if (device == null) {
            Log.e(TAG, "BluetoothDevice is null")
            return
        }

        Thread {
            try {
//                open(device)
//                ConnectThread(device).start()
                printer = PrinterInstance(context, device, handler)
                printer?.openConnection()
                printer?.init()
                printer?.printText("Hello World")
                printer?.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2);
                printer?.closeConnection()
            } catch (e: Exception) {
                Log.e(TAG, "Error during printing", e)
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        val MY_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()
            Log.d(TAG, "bluetoothAdapter?.cancelDiscovery()")

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                Log.d(TAG, "socket.connect() success")

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
//                manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }
}