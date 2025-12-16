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
import com.domain.BLEConnector


@Singleton
class BLEManager @Inject constructor(
    @ApplicationContext private val context: Context
) :  BLEConnector {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val bluetoothScanner by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private var bluetoothGatt: BluetoothGatt? = null

    private val serviceUUID = UUID.fromString("12345678-1234-1234-1234-1234567890ab")
    private val characteristicUUID = UUID.fromString("abcd1234-5678-90ab-cdef-1234567890ab")

    // =============================================================
    // 🔥 1) 스캔 단계 로그 강화
    // =============================================================
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

    // =============================================================
    // 🔥 2) BLE 전송 JSON을 그대로 출력하는 핵심
    // =============================================================
    @SuppressLint("MissingPermission")
    fun sendConfigJson(json: String, onDone: () -> Unit) {

        Log.d("BLE-SEND", "📦 전송 준비된 JSON: $json")

        val gatt = bluetoothGatt ?: run {
            Log.e("BLE-SEND", "❌ GATT 없음 → 전송 실패")
            onDone()
            return
        }

        val service = gatt.getService(serviceUUID) ?: run {
            Log.e("BLE-SEND", "❌ Service 없음")
            onDone()
            return
        }

        val ch = service.getCharacteristic(characteristicUUID) ?: run {
            Log.e("BLE-SEND", "❌ Characteristic 없음")
            onDone()
            return
        }

        val bytes = json.toByteArray()

        Log.d("BLE-SEND", "📩 JSON → ByteArray length=${bytes.size}")
        Log.d("BLE-SEND", "📩 Raw bytes=${bytes.joinToString()}")

        ch.value = bytes

        @Suppress("DEPRECATION")
        val result = gatt.writeCharacteristic(ch)

        Log.d("BLE-SEND", "📤 writeCharacteristic() 결과: $result")

        onDone()
    }

    // =============================================================
    // 🔥 3) suspend 버전도 로그 포함
    // =============================================================
    suspend fun sendConfigSuspend(json: String): Boolean =
        suspendCancellableCoroutine { cont ->
            sendConfigJson(json) { cont.resume(true) }
        }

    // =============================================================
    // 🔥 4) disconnect 로그
    // =============================================================
    @SuppressLint("MissingPermission")
    fun disconnectInternal() {
        Log.d("BLE", "🔌 disconnect() 실행 — GATT 닫힘")
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    override suspend fun scanAndConnect(): Boolean {
        return scanAndConnectSuspend()
    }

    override suspend fun sendConfig(json: String): Boolean {
        return sendConfigSuspend(json)
    }

    override fun disconnect() {
        disconnectInternal()
    }

}
