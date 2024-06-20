package net.harutiro.test_pt_210_print

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AndroidRuntimeException
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.android.print.sdk.CanvasPrint
import com.android.print.sdk.PrinterConstants
import com.android.print.sdk.PrinterInstance
import com.android.print.sdk.PrinterType
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder


class PosViewModel(private val context: Context): ViewModel(){

    val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter = bluetoothManager.adapter
    val bluetoothDeviceList = mutableStateListOf<BluetoothDevice>()
    val printUtils = PrintUtils()

    val TAG = "PosViewModel"

    var printer:PrinterInstance? = null

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
    fun printerConnect(device: BluetoothDevice?){
        Log.d(TAG, "Printing with device: ${device?.name}")

        if (device == null) {
            Log.e(TAG, "BluetoothDevice is null")
            return
        }

        Thread {
            try {
                printer = PrinterInstance(context, device, handler)
                printer?.openConnection()
                printer?.init()
            } catch (e: Exception) {
                Log.e(TAG, "Error during printing", e)
            }
        }.start()
    }

    fun printImage(){
        Thread {
            try {
                val cp = CanvasPrint()
                cp.init(PrinterType.T9)

                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ramen)
                val resizedBitmap = printUtils.convertToBlackWhite(bitmap) // 適切なサイズに変換
                Log.d("PrintUtils", "bitmap: $bitmap width: ${bitmap.width} height: ${bitmap.height}")
                Log.d("PrintUtils", "resizedBitmap: $resizedBitmap width: ${resizedBitmap.width} height: ${resizedBitmap.height}")
                cp.drawImage(resizedBitmap)

                printer?.printText("Print Custom Image:\n")
                printer?.printImage(cp.canvasImage)
                printer?.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2)
            } catch (e: Exception) {
                Log.e(TAG, "Error during printing", e)
            }
        }.start()
    }

    fun printQRCode() {
        Thread {
            try {
                //QRコード化する文字列
                val data = "https://www.google.com"

                //QRコード画像の大きさを指定(pixel)
                val size = 400

                try {
                    val barcodeEncoder = BarcodeEncoder()
                    //QRコードをBitmapで作成
                    val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)

                    val cp = CanvasPrint()
                    cp.init(PrinterType.T9)

                    val resizedBitmap = printUtils.convertToBlackWhite(bitmap) // 適切なサイズに変換
                    Log.d("PrintUtils", "bitmap: $bitmap width: ${bitmap.width} height: ${bitmap.height}")
                    Log.d("PrintUtils", "resizedBitmap: $resizedBitmap width: ${resizedBitmap.width} height: ${resizedBitmap.height}")
                    cp.drawImage(resizedBitmap)

                    printer?.printText("Print Custom Image:\n")
                    printer?.printImage(cp.canvasImage)
                    printer?.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2)

                } catch (e: WriterException) {
                    throw AndroidRuntimeException("Barcode Error.", e)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during printing", e)
            }
        }.start()
    }

    fun printBusinessCard(){
        Thread {
            try {

                //写真を載せる
                val cpImage = CanvasPrint()
                cpImage.init(PrinterType.T9)

                val bitmapImage = BitmapFactory.decodeResource(context.resources, R.drawable.my_image)
                val resizedBitmapImage = printUtils.convertToBlackWhite(bitmapImage) // 適切なサイズに変換
                cpImage.drawImage(resizedBitmapImage)

                printer?.printImage(cpImage.canvasImage)
                printer?.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2)

                val sb = StringBuffer()
                // printer?.setPrinter(BluetoothPrinter.COMM_LINE_HEIGHT, 80);
                printer?.setPrinter(
                    PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_CENTER
                )
                // タイトル部分
                printer?.setCharacterMultiple(1, 1)
                printer?.printText("harutiro\n")
                printer?.printText("makino haruto\n")
                printer?.setCharacterMultiple(0, 0)
                printer?.printText("\n==============================\n")

                // 主な情報
                printer?.printText("\n I am a super Mobile Developer\n")
                printer?.printText("\n==============================\n")

                // SNS情報など
                printer?.setPrinter(
                    PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT
                )
                printer?.setCharacterMultiple(0, 0)
                sb.append("X(Twitter): @harutiro\n")
                sb.append("Zenn: @harutiro\n")
                sb.append("Qiita: @harutiro\n")
                sb.append("GitHub: @harutiro\n")
                sb.append("Mail: dev@mail.harutiro.net\n")
                printer?.printText(sb.toString())
                printer?.printText("\n==============================\n")

                // WebサイトのQRコード
                printer?.setPrinter(
                    PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_CENTER
                )
                //QRコード化する文字列
                val data = "https://www.harutiro.net"

                //QRコード画像の大きさを指定(pixel)
                val size = 200

                val barcodeEncoder = BarcodeEncoder()
                //QRコードをBitmapで作成
                val bitmap =
                    barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)

                val cp = CanvasPrint()
                cp.init(PrinterType.T9)

                val resizedBitmap = printUtils.convertToBlackWhite(bitmap) // 適切なサイズに変換
                Log.d(
                    "PrintUtils",
                    "bitmap: $bitmap width: ${bitmap.width} height: ${bitmap.height}"
                )
                Log.d(
                    "PrintUtils",
                    "resizedBitmap: $resizedBitmap width: ${resizedBitmap.width} height: ${resizedBitmap.height}"
                )
                cp.drawImage(resizedBitmap)
                printer?.printText("web: https://www.harutiro.net\n")
                printer?.printImage(cp.canvasImage)
                printer?.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2)
            } catch (e: Exception) {
                Log.e(TAG, "Error during printing", e)
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun printTest() {
        Thread {
            try {
                printer?.printText("Hello World")
                printer?.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2)
            } catch (e: Exception) {
                Log.e(TAG, "Error during printing", e)
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun closeConnection(device: BluetoothDevice?) {
        Log.d(TAG, "Printing with device: ${device?.name}")

        if (device == null) {
            Log.e(TAG, "BluetoothDevice is null")
            return
        }

        Thread {
            try {
                printer = PrinterInstance(context, device, handler)
                printer?.closeConnection()
                printer = null
            } catch (e: Exception) {
                Log.e(TAG, "Error during printing", e)
            }
        }.start()
    }
}