package com.example.bluetoothtestapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.bluetoothtestapp.R
import com.example.bluetoothtestapp.databinding.ActivityMainBinding
import com.example.bluetoothtestapp.ui.extensions.makeGone
import com.example.bluetoothtestapp.ui.extensions.makeVisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment)

        setSupportActionBar(binding.toolbar)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.deviceDetailFragment)
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> binding.toolbar.makeVisible()
                R.id.deviceDetailFragment -> binding.toolbar.makeGone()
            }
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
