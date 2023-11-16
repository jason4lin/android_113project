package com.example.a113project

import android.bluetooth.*
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.*
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import androidx.core.content.getSystemService
import java.util.Set;
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import timber.log.Timber
import android.app.AlertDialog
import android.os.ParcelUuid
import android.bluetooth.le.ScanFilter
import android.view.ViewGroup
import com.example.a113project.databinding.ActivityMainBinding


private const val REQUEST_ENABLE_DIS = 30
private const val RUNTIME_PERMISSION_REQUEST_CODE = 2
private const val REQUEST_BLUETOOTH_SCAN_PERMISSION = 3
private const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 4
private const val REQUEST_ACCESS_FINE_LOCATION = 5
private const val MY_BLUETOOTH_PERMISSION_REQUEST= 6


class MainActivity : AppCompatActivity() {

    var bluetoothGatt: BluetoothGatt? = null

    private val bleScanner by lazy {
        mBluetoothAdapter.bluetoothLeScanner
    }

    private val scanResults = mutableListOf<ScanResult>()

    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            // User tapped on a scan result
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
                //connectGatt(context, false, gattCallback)
            }
        }
    }

    //private lateinit var scanResultAdapter: ScanResultAdapter

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

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

    private fun startBleScan() {
        if (!hasRequiredRuntimePermissions()) {
            requestRelevantRuntimePermissions()
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            var builder = ScanFilter.Builder()
            builder.setDeviceName(null)
            // 已經有所需的權限，開始進行 BLE 掃描
            scanResults.clear()
            scanResults.filter { device ->
                !device.device.name.equals("Unnamed")
            }
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
            Log.e("startBleScan","${isScanning}")
        } else {
            // 權限尚未獲得，可以進行額外的處理，例如再次要求權限或提示用戶
            // 此處應增加相應的邏輯
            Log.e("startBleScan","b")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun stopBleScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // 已經有所需的權限，開始進行 BLE 掃描
            bleScanner.stopScan(scanCallback)
            isScanning = false
            Log.e("stopBleScan","a")
        }
    }

    private fun Activity.requestRelevantRuntimePermissions() {
        if (hasRequiredRuntimePermissions()) { return }
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                requestLocationPermission()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestBluetoothPermissions()
            }
        }
    }

    private fun requestLocationPermission() {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Location permission required")
                .setMessage("Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        RUNTIME_PERMISSION_REQUEST_CODE
                    )
                }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    private fun requestBluetoothPermissions() {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Bluetooth permissions required")
            builder.setMessage("Starting from Android 12, the system requires apps to be granted " +
                    "Bluetooth access in order to scan for and connect to BLE devices.")
            builder.setCancelable(false)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RUNTIME_PERMISSION_REQUEST_CODE -> {
                val containsPermanentDenial = permissions.zip(grantResults.toTypedArray()).any {
                    it.second == PackageManager.PERMISSION_DENIED &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(this, it.first)
                }
                val containsDenial = grantResults.any { it == PackageManager.PERMISSION_DENIED }
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                when {
                    containsPermanentDenial -> {
                        // TODO: Handle permanent denial (e.g., show AlertDialog with justification)
                        // Note: The user will need to navigate to App Settings and manually grant
                        // permissions that were permanently denied
                    }
                    containsDenial -> {
                        requestRelevantRuntimePermissions()
                    }
                    allGranted && hasRequiredRuntimePermissions() -> {
                        startBleScan()
                    }
                    else -> {
                        // Unexpected scenario encountered when handling permissions
                        recreate()
                    }
                }
            }
            MY_BLUETOOTH_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableBluetooth()
                } else {
                    // Handle the case where Bluetooth permission is denied
                    Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
//                Log.e("123", "scanRESULTS: ${scanResults.size}")
//                var i = 0;
//                for(i in 0 until scanResults.size) {
//                    if (scanResults.get(i).device.name != null) {
//                        Log.e("456", "$i: ${scanResults.get(i).device.name}")
//                    } else {
//                        Log.e("456", "$i: null")
//                    }
//                }
//                Log.e("length", "size: ${scanResults.size}")

                scanResultAdapter.notifyDataSetChanged()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { scanButton.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private var discoverable = false
    private var broadcastReceiver: BroadcastReceiver? = null
    //private var filter: IntentFilter? = null

    // 創建一個 ActivityResultLauncher 以處理啟動啟用藍牙的 Intent
    private val enableBluetoothActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e("BA","${result}")

            if (result.resultCode == RESULT_OK) {
                Log.d("Working:", "Bluetooth enabled")
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
                makeDiscoverable()
            } else {
                Toast.makeText(this, "Bluetooth enabling canceled", Toast.LENGTH_SHORT).show()
            }
        }

    // 創建一個 ActivityResultLauncher 以處理啟動裝置可被發現性的 Intent
    private val enableDiscoverableActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d("Working", "Made discoverable")
                Toast.makeText(this, "Made discoverable", Toast.LENGTH_SHORT).show()
                discoverable = true
            } else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Discoverability rejected", Toast.LENGTH_SHORT).show()
            }
        }

    private lateinit var scanButton: Button // 將按鈕變數宣告為類別級別變數
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //scanResultAdapter = ScanResultAdapter
        binding.scanResultsRecyclerView.adapter = scanResultAdapter
        binding.scanResultsRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.scanResultsRecyclerView.isNestedScrollingEnabled = false

        val animator = binding.scanResultsRecyclerView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        scanButton = findViewById(R.id.scan_button) // 初始化按鈕 (將這行程式碼移到 onCreate 開頭)
        scanButton.setOnClickListener {
            if (isScanning) {
                stopBleScan()
            } else {
                startBleScan()
            }
        }
        //setupRecyclerView()
        requestPermissionsAndEnableBluetooth()
        //setUpBT()
    }


    private fun requestPermissionsAndEnableBluetooth() {
        if (!hasRequiredRuntimePermissions()) {
            enableBluetooth()
            requestRelevantRuntimePermissions()
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
/*
    private fun setupRecyclerView() {
        scan_results_recycler_view.apply {
            adapter = scanResultAdapter
            layoutManager  = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }
    */
/*
    private fun setUpBT() {
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "This device does not support Bluetooth", Toast.LENGTH_SHORT)
                .show()
        } else if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 已經有權限，可以啟動 Intent
                enableBluetoothActivityResult.launch(enableBtIntent)
                Toast.makeText(this, "Please enable Bluetooth NOW!!!!!", Toast.LENGTH_SHORT).show()
            } else {
                // 需要請求權限
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    MY_BLUETOOTH_PERMISSION_REQUEST
                )
            }
            //enableBluetoothActivityResult.launch(enableBtIntent)
            Toast.makeText(this, "Please enable Bluetooth NOW!!!!!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Yay", Toast.LENGTH_SHORT).show()
            makeDiscoverable()
        }
    }
*/
    private fun makeDiscoverable() {
        // 在這裡檢查 BLUETOOTH_SCAN 權限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 再檢查 BLUETOOTH_CONNECT 權限
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 如果權限已經授予，執行相應的操作
                if (mBluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                    discoverableIntent.putExtra(
                        BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                        REQUEST_ENABLE_DIS
                    )
                    enableDiscoverableActivityResult.launch(discoverableIntent)
                }
            } else {
                // 如果沒有 BLUETOOTH_CONNECT 權限，請求該權限
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_BLUETOOTH_CONNECT_PERMISSION
                )
            }
        } else {
            // 如果沒有 BLUETOOTH_SCAN 權限，請求該權限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                REQUEST_BLUETOOTH_SCAN_PERMISSION
            )
        }
    }

    override fun onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
            broadcastReceiver = null
        }
        super.onDestroy()
    }
}
