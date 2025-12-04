package com.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

@Singleton
class BLEManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val bluetoothScanner by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private var bluetoothGatt: BluetoothGatt? = null

    private val serviceUUID = UUID.fromString("12345678-1234-1234-1234-1234567890ab")
    private val characteristicUUID = UUID.fromString("abcd1234-5678-90ab-cdef-1234567890ab")

    @SuppressLint("MissingPermission")
    fun scanAndConnect(
        onConnected: () -> Unit,
        onFailed: () -> Unit
    ) {
        // 권한 체크 (SCAN / CONNECT)
        val hasPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PERMISSION_GRANTED

        if (!hasPermission) {
            Log.e("BLE", "권한 없음 → scan 중단")
            onFailed()
            return
        }

        val scanner = bluetoothScanner ?: run {
            Log.e("BLE", "bluetoothScanner == null")
            onFailed()
            return
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device ?: return
                val name = device.name ?: return

                Log.d("BLE", "스캔 발견: $name")

                if (name == "PillBox") {
                    scanner.stopScan(this)

                    device.connectGatt(context, false, object : BluetoothGattCallback() {
                        override fun onConnectionStateChange(
                            gatt: BluetoothGatt,
                            status: Int,
                            newState: Int
                        ) {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                bluetoothGatt = gatt
                                gatt.discoverServices()
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                bluetoothGatt = null
                            }
                        }

                        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                            Log.d("BLE", "서비스 발견!")
                            onConnected()
                        }
                    })
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BLE", "스캔 실패: $errorCode")
                onFailed()
            }
        }

        scanner.startScan(callback)
    }

    suspend fun scanAndConnectSuspend(): Boolean =
        suspendCancellableCoroutine { cont ->
            scanAndConnect(
                onConnected = { cont.resume(true) },
                onFailed = { cont.resume(false) }
            )
        }

    @SuppressLint("MissingPermission")
    fun sendConfigJson(json: String, onDone: () -> Unit) {
        val gatt = bluetoothGatt ?: return onDone()
        val service = gatt.getService(serviceUUID) ?: return onDone()
        val characteristic = service.getCharacteristic(characteristicUUID) ?: return onDone()

        characteristic.value = json.toByteArray()
        @Suppress("DEPRECATION")
        gatt.writeCharacteristic(characteristic)
        onDone()
    }

    suspend fun sendConfigSuspend(json: String): Boolean =
        suspendCancellableCoroutine { cont ->
            sendConfigJson(json) { cont.resume(true) }
        }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
