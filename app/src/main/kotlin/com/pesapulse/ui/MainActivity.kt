package com.pesapulse.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.pesapulse.data.local.AppDatabase
import com.pesapulse.databinding.ActivityMainBinding
import com.pesapulse.util.HistoricalSmsImporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_SMS] == true && 
            permissions[Manifest.permission.RECEIVE_SMS] == true) {
            importHistoricalSms()
        } else {
            Toast.makeText(this, "SMS permissions are required for full functionality", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setupNavigation()
        checkPermissions()
        authenticateUser()
    }

    private fun authenticateUser() {
        if (com.pesapulse.util.BiometricHelper.isBiometricAvailable(this)) {
            com.pesapulse.util.BiometricHelper.showBiometricPrompt(
                this,
                onSuccess = { /* User authenticated */ },
                onError = { /* Handle error */ }
            )
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS
        )
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            // Permissions already granted
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun importHistoricalSms() {
        CoroutineScope(Dispatchers.Main).launch {
            HistoricalSmsImporter.importLast6Months(this@MainActivity, database)
            Toast.makeText(this@MainActivity, "M-Pesa history imported", Toast.LENGTH_SHORT).show()
        }
    }
}
