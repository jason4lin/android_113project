package com.example.a113project.bleSettings

//import com.example.a113project.bleSettings.BluetoothViewModel

import android.Manifest
import android.R.attr.value
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.a113project.MainActivity
import com.example.a113project.ScanResultAdapter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID


private const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

@SuppressLint("MissingPermission")
class BluetoothLeScannerManager(
    private val context: Context,
    private val permissionRequestCallback: PermissionRequestCallback
){

    var bluetoothGatt: BluetoothGatt? = null

    var isScanning: Boolean = false
        private set

    private val mBluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        mBluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Log.i("ScanCallback", "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                if (result.device.name != null)
                    scanResults.add(result)
                scanResultAdapter.notifyDataSetChanged()

                //connectToDevice(result.device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }

    fun startScan() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bleScanner?.startScan(null, scanSettings, scanCallback)
            isScanning = true
        } else {
            // 權限未獲得，需要在 Activity 中處理
            permissionRequestCallback.onRequestPermissionNeeded()
            Log.e("BluetoothLeScannerManager", "Location permission not granted")
        }
    }

    interface PermissionRequestCallback {
        fun onRequestPermissionNeeded()
    }

    fun stopScan() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bleScanner?.stopScan(scanCallback)
            isScanning = false
            Log.e("stopBleScan","a")
        }
    }

    fun readCharacteristic() {
        val service = bluetoothGatt?.getService(UUID.fromString("ac670292-6e02-4ec7-ab78-8f3b8352e078"))
        val characteristic = service?.getCharacteristic(UUID.fromString("c7ac3e78-caf4-426d-9f2a-b558df862457"))
        if (characteristic != null) {
            val value = characteristic.value
            if (value != null && value.size >= 4) {
                val valueAsString = bytesToHex(value)
                val floatValues = parseHexToFloatArray(valueAsString)
                Log.i("but read Data", "數值: ${floatValues.contentToString()}")
                // 現在您可以繼續處理這個值，例如顯示或儲存
            } else {
                Log.i("but read Data", "數據無效或太短")
            }
        } else {
            Toast.makeText(context, "特征未找到", Toast.LENGTH_SHORT).show()
        }
    }

    fun parseByteArray(byteArrayString: String): FloatArray? {
        try {
            // 將字串轉換為字節數組
            val byteArray = byteArrayString.toByteArray(Charsets.ISO_8859_1)

            // 檢查字節數組的長度是否為4的倍數（每個浮點數4字節）
            if (byteArray.size % 4 != 0) {
                return null // 不是有效的浮點數陣列
            }

            // 創建一個浮點數陣列
            val floatArray = FloatArray(byteArray.size / 4)

            // 解析字節數組中的浮點數
            for (i in 0 until byteArray.size step 4) {
                val intBits = byteArrayToInt(byteArray.sliceArray(i until i + 4))
                val floatValue = Float.fromBits(intBits)
                floatArray[i / 4] = floatValue
            }

            return floatArray
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun byteArrayToInt(byteArray: ByteArray): Int {
        return byteArray[0].toInt() and 0xFF shl 24 or
                (byteArray[1].toInt() and 0xFF) shl 16 or
                (byteArray[2].toInt() and 0xFF) shl 8 or
                (byteArray[3].toInt() and 0xFF)
    }

    private val scanResults = mutableListOf<ScanResult>()

    val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            // User tapped on a scan result
            if (isScanning) {
                stopScan()
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
                bluetoothGatt = connectGatt(context.applicationContext,false,gattCallback)
            }
        }
    }
    fun parseHexFloatString(hexString: String): List<Float> {
        val result = mutableListOf<Float>()
        val hexBytes = hexString.chunked(8) // 每個浮點數 8 個字符 (32 位元)

        for (hex in hexBytes) {
            // 轉換十六進位字串為浮點數
            val intVal = hex.toLong(16).toInt()
            val floatValue = Float.fromBits(intVal)
            result.add(floatValue)
        }

        return result
    }


    fun convertByteArrayToString(byteArray: ByteArray): String {
        val stringBuilder = StringBuilder()
        for (byte in byteArray) {
            stringBuilder.append(String.format("%02X", byte))
        }
        return stringBuilder.toString()
    }


    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    private var tempBuffer = mutableListOf<Byte>()
    private val combinedArraySize = 4 // 或者任何您需要的大小
    private var isDataEnd = false // 標誌數據結束
    private var dataProcessor: DataProcessor? = null
    private var dataReceiver: DataReceiver? = null

    fun setDataReceiver(receiver: DataReceiver) {
        dataReceiver = receiver
    }


    fun setDataProcessor(processor: DataProcessor) {
        this.dataProcessor = processor
    }
    interface DataProcessor {
        fun processReceivedData(data: ByteArray)
    }
    interface DataReceiver {
        fun onReceiveData(data: ByteArray)
    }

    private fun processCombinedArray(combinedArray: ByteArray) {
        // 在這裡處理合成的數據
        dataProcessor?.processReceivedData(combinedArray)
        dataReceiver?.onReceiveData(combinedArray)
    }

    private fun checkForEndData(value: ByteArray): Boolean {
        // 判斷是否為結束數據的邏輯
        return false
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status === BluetoothGatt.GATT_SUCCESS) {
                if (value != null && value.size >= 4) {
                    val data = convertBytesToFloats(value)
                    tempBuffer.addAll(value.toList())
                    if (tempBuffer.size >= combinedArraySize) {
                        processCombinedArray(tempBuffer.toByteArray())
                        tempBuffer.clear() // 清除暫存陣列以準備下一批數據
                    }
                    // 檢查是否為結束數據
                    if (checkForEndData(value)) {
                        isDataEnd = true
                        // 處理結束情況
                    }
                    Log.e("OnRead", "OnRead byte length: ${value.size}")
                    Log.e("OnRead", "OnRead flaot length: ${data.size}")
                    Log.i("on read Data", "數值: ${bytesToHex(value)}")
                    Log.i("on read Data", "數值: ${data.contentToString()}")
                    // 現在您可以繼續處理這個值，例如解析成浮點數
                } else {
                    Log.i("on read Data", "數據無效或太短")
                }
            } else {
                Log.e("on read Data", "讀取失敗，狀態: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            //call read

            val date = gatt.readCharacteristic(characteristic)
            tempBuffer.addAll(value.toList())
            if (tempBuffer.size >= combinedArraySize) {
                processCombinedArray(tempBuffer.toByteArray())
                tempBuffer.clear() // 清除暫存陣列以準備下一批數據
            }
            // 檢查是否為結束數據
            if (checkForEndData(value)) {
                isDataEnd = true
                // 處理結束情況
            }
            //直接用onchanged的值
//            if (value != null && value.size >= 4) {
//                val data = convertBytesToFloats(value)
//                Log.i("on change Data", "HEX數值: ${bytesToHex(value)}")
//                Log.i("on change Data", "數值: ${data.contentToString()}")
//            } else {
//                Log.i("on change Data", "數據無效或太短")
//            }
        }

        fun convertBytesToFloats(bytes: ByteArray): FloatArray {
            val byteBuffer = ByteBuffer.wrap(bytes)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN) // 或 ByteOrder.BIG_ENDIAN，取決於來源數據的字節順序
            val floatBuffer = byteBuffer.asFloatBuffer()
            val floats = FloatArray(floatBuffer.capacity())
            floatBuffer.get(floats)
            return floats
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // TODO: Store a reference to BluetoothGatt
                    bluetoothGatt = gatt
                    gatt.discoverServices()
                    readService()
                    // 請求新的MTU大小
                    val newMtuSize = 517; // 你希望請求的MTU大小
                    val mtuResult = gatt.requestMtu(newMtuSize);
                    //(context as? MainActivity)?.startCameraActivity()
                    if (mtuResult) {
                        Log.i("BluetoothGattCallbackMTU", "Requested MTU size of " + newMtuSize);
                    } else {
                        Log.e("BluetoothGattCallbackMTU", "Failed to request MTU");
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                with(gatt) {
                    Log.w("BluetoothGattCallback", "Discovered ${services.size} services for ${device.address}")
                    printGattTable()
                }
                    val char = gatt.getService(UUID.fromString("ac670292-6e02-4ec7-ab78-8f3b8352e078")).getCharacteristic(UUID.fromString("c7ac3e78-caf4-426d-9f2a-b558df862457"))
                    enableNotifications(gatt, char)
                    //Log.i("readChar","${gatt.readCharacteristic(char)}")
            } else {
                Log.e("BluetoothGattCallback", "Service discovery failed with status $status")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val characteristic = descriptor.characteristic
                Log.i("readChar","${gatt.readCharacteristic(characteristic)}")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback", "Wrote to characteristic $uuid | value: ${value}")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for $uuid!")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback", "Characteristic write failed for $uuid, error: $status")
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothMTU", "MTU changed to: $mtu")
            } else {
                Log.e("BluetoothMTU", "MTU change failed")
            }
        }

    }





    fun parseHexToFloatArray(hexString: String?): FloatArray {
        // 每個浮點數是4字節，所以每8個十六進制字符代表一個浮點數
        val floatCount = hexString!!.length / 8
        val floats = FloatArray(floatCount)

        for (i in 0 until floatCount) {
            val intBits = hexString.substring(i * 8, (i + 1) * 8).toLong(16).toInt()
            floats[i] = Float.fromBits(intBits)
        }

        return floats
    }

    private fun bytesToHex(bytes: ByteArray): String? {
        val builder = java.lang.StringBuilder()
        for (b in bytes) {
            builder.append(String.format("%02x", b))
        }
        return builder.toString()
    }

    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

    fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        Log.i("enableNotifications", "Enabling notifications for ${characteristic.uuid}")

        // 檢查特徵是否支持通知或指示
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            Log.e("enableNotifications", "${characteristic.uuid} doesn't support notifications/indications")
            return
        }

        // 設置特徵通知
        val notificationEnabled = gatt.setCharacteristicNotification(characteristic, true)
        if (!notificationEnabled) {
            Log.e("enableNotifications", "Failed to enable notification for ${characteristic.uuid}")
            return
        }

        // 獲取 CCCD 並寫入適當的值
        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val descriptor = characteristic.getDescriptor(cccdUuid)
        descriptor?.let {
            val payload = if (characteristic.isNotifiable()) {
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else {
                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            }
            descriptor.value = payload
            val descriptorWriteResult = gatt.writeDescriptor(descriptor)
            if (!descriptorWriteResult) {
                Log.e("enableNotifications", "Failed to write descriptor for ${characteristic.uuid}")
            }
        } ?: Log.e("enableNotifications", "CCCD descriptor not found for ${characteristic.uuid}")
    }

    fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            Log.e("ConnectionManager", "${characteristic.uuid} doesn't support indications/notifications")
            return
        }

        val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (bluetoothGatt?.setCharacteristicNotification(characteristic, false) == false) {
                Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }
            writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        } ?: Log.e("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
    }

    fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        bluetoothGatt?.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        } ?: error("Not connected to a BLE device!")
    }

    private fun readService() {
        // 用你要连接的服务的 UUID 替换下面的 UUID
        val serviceUuid = UUID.fromString("ac670292-6e02-4ec7-ab78-8f3b8352e078")
        val charUuid = UUID.fromString("c7ac3e78-caf4-426d-9f2a-b558df862457")
        val serviceChar = bluetoothGatt?.getService(serviceUuid)?.getCharacteristic(charUuid)
        if (serviceChar?.isReadable() == true) {
            bluetoothGatt?.readCharacteristic(serviceChar)
        }
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }
}