package net.harutiro.test_pt_210_print

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.harutiro.test_pt_210_print.ui.theme.TestPT210PrintTheme

class MainActivity : ComponentActivity() {

    private var posViewModel: PosViewModel? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        posViewModel = PosViewModel(this)

        setContent {
            TestPT210PrintTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Greeting(
                            modifier = Modifier.padding(innerPadding),
                            onStartBluetooth = {
                                posViewModel?.startBluetoothSearch(this@MainActivity)
                            },
                            onGetBluetoothDeviceList = {
                                posViewModel?.getBluetoothDeviceList(this@MainActivity)
                            },
                            getBluetoothDeviceList = {
                                posViewModel?.getParingDeviceList()
                            }
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            items(posViewModel?.bluetoothDeviceList ?: emptyList()){
                                Text(it.name ?: "null")
                                Button(onClick = {
                                    posViewModel?.printTest(it)
                                }) {
                                    Text("test Connect")
                                }
                                Button(onClick = {
                                    posViewModel?.print(it)
                                }) {
                                    Text("バーコード接続")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        posViewModel?.stopBluetoothSearch(this)
    }
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    onStartBluetooth: () -> Unit,
    onGetBluetoothDeviceList: () -> Unit,
    getBluetoothDeviceList: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = onStartBluetooth) {
            Text("Start Bluetooth Search")
        }
        Button(onClick = onGetBluetoothDeviceList) {
            Text("Get Bluetooth Device List")
        }
        Button(onClick = getBluetoothDeviceList) {
            Text("Get Bluetooth Device List")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestPT210PrintTheme {
        Greeting(onStartBluetooth = {}, onGetBluetoothDeviceList = {}, getBluetoothDeviceList = {})
    }
}