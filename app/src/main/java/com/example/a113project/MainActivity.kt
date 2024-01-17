package com.example.a113project

import android.bluetooth.*
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.*
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import timber.log.Timber
import android.app.AlertDialog
import android.bluetooth.le.ScanFilter
import com.example.a113project.bleSettings.BluetoothLeScannerManager
import com.example.a113project.bleSettings.blePermissions
//import com.example.a113project.bleSettings.BluetoothViewModel
import com.example.a113project.databinding.ActivityMainBinding
import java.util.UUID

private const val REQUEST_ENABLE_DIS = 300
private const val RUNTIME_PERMISSION_REQUEST_CODE = 2
private const val REQUEST_BLUETOOTH_SCAN_PERMISSION = 3
private const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 4
private const val REQUEST_ACCESS_FINE_LOCATION = 5
private const val MY_BLUETOOTH_PERMISSION_REQUEST= 6


class MainActivity : AppCompatActivity(), BluetoothLeScannerManager.PermissionRequestCallback,
    BluetoothLeScannerManager.DataReceiver {

    private lateinit var bluetoothLeScannerManager: BluetoothLeScannerManager
    private lateinit var bluetoothPermissions: blePermissions
    private lateinit var scanButton: Button
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothLeScannerManager = BluetoothLeScannerManager(this,this)
        bluetoothLeScannerManager = BluetoothLeScannerManager(this, this).apply {
            setDataReceiver(this@MainActivity)
        }
        bluetoothPermissions = blePermissions(this,this).apply {
            setEnableBluetoothActivityResult(enableBluetoothActivityResult)
            setEnableDiscoverableActivityResult(enableDiscoverableActivityResult)
        }
        initView()
        requestPermissionsAndEnableBluetooth()
        startCameraActivity()
    }


    private fun initView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        binding.scanResultsRecyclerView.apply {
            adapter = bluetoothLeScannerManager.scanResultAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            isNestedScrollingEnabled = false
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
    }

    private fun setupButtons() {
        scanButton = findViewById(R.id.scan_button)
        scanButton.setOnClickListener {
            if (bluetoothLeScannerManager.isScanning) {
                bluetoothLeScannerManager.stopScan()
                scanButton.text = "Start Scan"
            } else {
                scanButton.text = "Stop Scan"
                bluetoothLeScannerManager.startScan()
            }
        }

        val btnReadCharacteristic = findViewById<Button>(R.id.btnReadCharacteristic)
        btnReadCharacteristic.setOnClickListener {
            readCharacteristic()
        }
    }


    override fun onReceiveData(data: ByteArray) {
        // 在這裡處理接收到的數據，例如更新UI
    }

    private fun readCharacteristic() {
        bluetoothLeScannerManager.readCharacteristic()
    }

    fun startCameraActivity() {
        val intent = Intent(this, Camera::class.java)
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        bluetoothLeScannerManager.stopScan()
        bluetoothPermissions.dismissDialogs()
        unregisterReceiverIfNotNull(broadcastReceiver)
    }

    private fun unregisterReceiverIfNotNull(receiver: BroadcastReceiver?) {
        receiver?.let { unregisterReceiver(it) }
    }

    override fun onRequestPermissionNeeded() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
    }

    private val mBluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }


    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun Context.hasRequiredRuntimePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RUNTIME_PERMISSION_REQUEST_CODE -> {
                val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

                if (allPermissionsGranted) {
                    bluetoothLeScannerManager.startScan()
                } else {
                    // 至少一個請求的權限被拒絕
                    // 在這裡顯示解釋為什麼需要這些權限的訊息
                    // 並可能引導用戶到設置中開啟權限
                    handleDeniedPermissions()
                }
            }
            // 處理其他可能的請求代碼
        }
    }

    private fun handleDeniedPermissions() {
        // 顯示解釋對話框或Toast訊息
        Toast.makeText(this, "需要權限以繼續", Toast.LENGTH_SHORT).show()

        // 可以在這裡加入引導用戶到設置頁面的邏輯
    }

    private var broadcastReceiver: BroadcastReceiver? = null

    private val enableBluetoothActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 處理結果
            bluetoothPermissions.handleEnableBluetoothResult(result)
        }

    private val enableDiscoverableActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 處理結果
            bluetoothPermissions.handleEnableDiscoverableResult(result)
        }


    private fun requestPermissionsAndEnableBluetooth() {
        if (!bluetoothPermissions.hasRequiredRuntimePermissions()) {
            enableBluetooth()
            bluetoothPermissions.requestRelevantRuntimePermissions()
        } else {
            enableBluetooth()
        }
    }

    private fun enableBluetooth() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "This device does not support Bluetooth", Toast.LENGTH_SHORT)
                .show()
        } else if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                enableBluetoothActivityResult.launch(enableBtIntent)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    MY_BLUETOOTH_PERMISSION_REQUEST
                )
            }
        } else {
            makeDiscoverable()
        }
    }


    @SuppressLint("MissingPermission")
    private fun makeDiscoverable() {
        // 檢查 BLUETOOTH_SCAN 權限
        val hasScanPermission = bluetoothPermissions.hasBluetoothScanPermission()

        // 檢查 BLUETOOTH_CONNECT 權限
        val hasConnectPermission = bluetoothPermissions.hasBluetoothConnectPermission()

        // 如果權限已經授予，執行相應的操作
        if (hasScanPermission && hasConnectPermission) {
            if (mBluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_LOCAL_NAME, "testphone")
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, REQUEST_ENABLE_DIS)
                enableDiscoverableActivityResult.launch(discoverableIntent)
            }
        } else {
            // 如果沒有 BLUETOOTH_SCAN 或 BLUETOOTH_CONNECT 權限，請求相應的權限
            val permissionsToRequest = mutableListOf<String>()
            if (!hasScanPermission) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (!hasConnectPermission) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }

}

