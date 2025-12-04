package com.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
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
        val hasPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PERMISSION_GRANTED

        if (!hasPermission) {
            Log.e("BLE", "❌ 권한 없음 → scan 중단")
            onFailed()
            return
        }

        val scanner = bluetoothScanner ?: run {
            Log.e("BLE", "❌ bluetoothScanner == null")
            onFailed()
            return
        }

        // 🚨 setLegacy(true) 절대 쓰면 안 됨 → 스캔 자체가 무효 처리됨
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = emptyList<ScanFilter>()

        val callback = object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val record = result.scanRecord
                val name = record?.deviceName ?: result.device.name

                Log.d("BLE", "🔍 스캔 발견: name=$name, addr=${result.device.address}")

                if (name?.contains("PillBox") == true) {
                    Log.d("BLE", "🎯 PillBox 발견! 연결 시도")
                    scanner.stopScan(this)

                    result.device.connectGatt(context, false, object : BluetoothGattCallback() {

                        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                Log.d("BLE", "🔵 GATT 연결됨 → 서비스 검색")
                                bluetoothGatt = gatt
                                gatt.discoverServices()
                            }
                        }

                        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                            Log.d("BLE", "✔ 서비스 발견")
                            onConnected()
                        }
                    })
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BLE", "❌ 스캔 실패: $errorCode")
                onFailed()
            }
        }

        Log.d("BLE", "🚀 스캔 시작!!")
        scanner.startScan(filters, settings, callback)
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

        val gatt = bluetoothGatt ?: run {
            Log.e("BLE", "❌ GATT 없음 → 전송 실패")
            onDone()
            return
        }

        val service = gatt.getService(serviceUUID) ?: run {
            Log.e("BLE", "❌ Service 없음")
            onDone()
            return
        }

        val ch = service.getCharacteristic(characteristicUUID) ?: run {
            Log.e("BLE", "❌ Characteristic 없음")
            onDone()
            return
        }

        ch.value = json.toByteArray()

        @Suppress("DEPRECATION")
        gatt.writeCharacteristic(ch)

        Log.d("BLE", "📩 JSON 전송 완료 → $json")
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
