package com.example.a113project

import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.Manifest


class ScanResultAdapter(
    private val items: List<ScanResult>,
    private val onClickListener: ((device: ScanResult) -> Unit)
) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_scan_result, parent, false)
        return ViewHolder(view, onClickListener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    class ViewHolder(
        private val view: View,
        private val onClickListener: ((device: ScanResult) -> Unit)
    ) : RecyclerView.ViewHolder(view) {

        private val deviceNameTextView: TextView = view.findViewById(R.id.device_name)
        private val macAddressTextView: TextView = view.findViewById(R.id.mac_address)
        private val signalStrengthTextView: TextView = view.findViewById(R.id.signal_strength)


        fun bind(result: ScanResult) {
            // Check for BluetoothDevice name permission
            if (ContextCompat.checkSelfPermission(view.context, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
            ) {
                deviceNameTextView.text = result.device.name ?: "Unnamed"
            } else {
                // Handle the case where the permission is not granted
                deviceNameTextView.text = "Permission Denied"
            }
            macAddressTextView.text = result.device.address
            signalStrengthTextView.text = "${result.rssi} dBm"
            view.setOnClickListener { onClickListener.invoke(result) }
        }
    }
}