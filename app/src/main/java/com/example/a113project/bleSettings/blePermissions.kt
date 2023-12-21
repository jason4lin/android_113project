package com.example.a113project.bleSettings

import android.bluetooth.*
import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import android.Manifest
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.bluetooth.le.*
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.recreate

const val REQUEST_ENABLE_DIS = 300
const val RUNTIME_PERMISSION_REQUEST_CODE = 2
const val REQUEST_BLUETOOTH_SCAN_PERMISSION = 3
const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 4
const val REQUEST_ACCESS_FINE_LOCATION = 5
const val MY_BLUETOOTH_PERMISSION_REQUEST= 6


class blePermissions(
    private val context: Context,
    private val activity: Activity
) {

    private val mBluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // 確保ActivityResultLauncher在MainActivity中初始化並在此處使用
    lateinit var _enableBluetoothActivityResult: ActivityResultLauncher<Intent>
    lateinit var _enableDiscoverableActivityResult: ActivityResultLauncher<Intent>

    private var discoverable = false
    private var permissionDialog: AlertDialog? = null

    fun setEnableBluetoothActivityResult(launcher: ActivityResultLauncher<Intent>) {
        _enableBluetoothActivityResult = launcher
    }

    fun setEnableDiscoverableActivityResult(launcher: ActivityResultLauncher<Intent>) {
        _enableDiscoverableActivityResult = launcher
    }

    fun handleEnableBluetoothResult(result: ActivityResult) {
        // 處理啟用藍牙的結果
        if (result.resultCode == Activity.RESULT_OK) {
            // 藍牙已啟用
            makeDiscoverable()
        } else {
            // 藍牙啟用被取消
            Toast.makeText(context, "Bluetooth enabling canceled", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleEnableDiscoverableResult(result: ActivityResult) {
        // 處理可發現性的結果
        if (result.resultCode == Activity.RESULT_OK) {
            // 設備已設為可發現
            discoverable = true
        } else {
            // 可發現性被拒絕
            Toast.makeText(context, "Discoverability rejected", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun makeDiscoverable() {
        if (mBluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_LOCAL_NAME, "testphone")
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, REQUEST_ENABLE_DIS)
            }
            _enableDiscoverableActivityResult.launch(discoverableIntent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun requestRelevantRuntimePermissions() {
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
        activity.showPermissionDialog(
            "Location permission required",
            "Starting from Android M (6.0), the system requires apps to be granted location access in order to scan for BLE devices.",
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            RUNTIME_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestBluetoothPermissions() {
        activity.showPermissionDialog(
            "Bluetooth permissions required",
            "Starting from Android 12, the system requires apps to be granted Bluetooth access in order to scan for and connect to BLE devices.",
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT),
            RUNTIME_PERMISSION_REQUEST_CODE
        )
    }

    fun Activity.showPermissionDialog(title: String, message: String, permissions: Array<String>, requestCode: Int) {
        if (!isFinishing && !isDestroyed) {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    ActivityCompat.requestPermissions(this, permissions, requestCode)
                }
                .create()
                .show()
        }
    }

    fun Context.hasPermission(permissionType: String): Boolean =
        ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED

    fun Context.hasRequiredRuntimePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    fun hasBluetoothScanPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasBluetoothConnectPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun Activity.showPermissionDialog(title: String, message: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    requestCode
                )
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun requestBluetoothScanPermission() {
        showPermissionDialog(
            "Bluetooth Scan Permission Required",
            "Starting from Android 12, the system requires apps to be granted Bluetooth access in order to scan for BLE devices.",
            REQUEST_BLUETOOTH_SCAN_PERMISSION
        )
    }

    private fun requestBluetoothConnectPermission() {
        showPermissionDialog(
            "Bluetooth Connect Permission Required",
            "Starting from Android 12, the system requires apps to be granted Bluetooth access in order to connect to BLE devices.",
            REQUEST_BLUETOOTH_CONNECT_PERMISSION
        )
    }

    private fun showPermissionDialog(title: String, message: String, requestCode: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    requestCode
                )
            }
        val dialog = builder.create()
        dialog.show()
    }

    fun dismissDialogs() {
        permissionDialog?.dismiss()
    }

    fun hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun hasRequiredRuntimePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun Activity.requestRelevantRuntimePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            requestLocationPermission()
        } else {
            requestBluetoothPermissions()
        }
    }

}