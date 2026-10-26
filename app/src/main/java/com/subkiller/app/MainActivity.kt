package com.subkiller.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.subkiller.app.ads.AdManager
import com.subkiller.app.databinding.ActivityMainBinding
import com.subkiller.app.util.NotificationScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                NotificationScheduler.scheduleDailyReminder(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AdMob SDK
        AdManager.initialize(this)

        // Setup Navigation with BottomNavigationView
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // Request POST_NOTIFICATIONS on Android 13+ and schedule reminders
        checkAndRequestNotificationPermission()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationScheduler.scheduleDailyReminder(this)
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            NotificationScheduler.scheduleDailyReminder(this)
        }
    }
}
