
package com.example.cellinfoapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.telephony.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var telephonyManager: TelephonyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize location and telephony services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        // Check permissions and get data
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ), 1)
        } else {
            getLocationData()
            getCellInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocationData()
            getCellInfo()
        }
    }

    private fun getLocationData() {
        val locationTask: Task<Location> = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener { location ->
            if (location != null) {
                val textViewLocation = findViewById<TextView>(R.id.textViewLocation)
                val textViewTime = findViewById<TextView>(R.id.textViewTime)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentTime = dateFormat.format(Date())

                textViewLocation.text = "Location: ${location.latitude}, ${location.longitude}"
                textViewTime.text = "Time: $currentTime"
            }
        }
    }

    private fun getCellInfo() {
        val allCellInfo: List<CellInfo> = telephonyManager.allCellInfo
        if (allCellInfo.isNotEmpty()) {
            val cellInfo = allCellInfo[0]  // Assuming we just take the first cell info

            val textViewNetworkType = findViewById<TextView>(R.id.textViewNetworkType)
            val textViewCellId = findViewById<TextView>(R.id.textViewCellId)
            val textViewSignalParams = findViewById<TextView>(R.id.textViewSignalParams)

            textViewNetworkType.text = "Network Type: ${getNetworkTypeString(telephonyManager.networkType)}"
            textViewCellId.text = "Cell ID: ${cellInfo.cellIdentity.toString()}"
            textViewSignalParams.text = "Signal Parameters: ${getSignalParameters(cellInfo)}"
        }
    }

    private fun getNetworkTypeString(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            else -> "Unknown"
        }
    }

    private fun getSignalParameters(cellInfo: CellInfo): String {
        return when (cellInfo) {
            is CellInfoLte -> {
                val cellSignalStrengthLte = cellInfo.cellSignalStrength
                val cellIdentityLte = cellInfo.cellIdentity as CellIdentityLte
                "RSRP: ${cellSignalStrengthLte.rsrp}, RSRQ: ${cellSignalStrengthLte.rsrq}, TAC: ${cellIdentityLte.tac}, CI: ${cellIdentityLte.ci}, PCI: ${cellIdentityLte.pci}, PLMN-Id: ${cellIdentityLte.mobileNetworkOperator}"
            }
            is CellInfoWcdma -> {
                val cellSignalStrengthWcdma = cellInfo.cellSignalStrength
                val cellIdentityWcdma = cellInfo.cellIdentity as CellIdentityWcdma
                "RSCP: ${cellSignalStrengthWcdma.dbm}, Ec/No: ${cellSignalStrengthWcdma.ecno}, LAC: ${cellIdentityWcdma.lac}, CID: ${cellIdentityWcdma.cid}, PLMN-Id: ${cellIdentityWcdma.mobileNetworkOperator}"
            }
            is CellInfoGsm -> {
                val cellSignalStrengthGsm = cellInfo.cellSignalStrength
                val cellIdentityGsm = cellInfo.cellIdentity as CellIdentityGsm
                "RSSI: ${cellSignalStrengthGsm.dbm}, LAC: ${cellIdentityGsm.lac}, CID: ${cellIdentityGsm.cid}, PLMN-Id: ${cellIdentityGsm.mobileNetworkOperator}"
            }
            else -> "Unknown signal parameters"
        }
    }
}



