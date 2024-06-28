package com.example.madcamp_week1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar1 = findViewById<MaterialToolbar>(R.id.topAppBar1)
        setSupportActionBar(toolbar1)

        val toolbar2 = findViewById<MaterialToolbar>(R.id.topAppBar2)
        setSupportActionBar(toolbar2)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        // Ensure all tabs navigate to their respective destinations
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.dashboardFragment -> {
                    navController.navigate(R.id.dashboardFragment)
                    true
                }
                R.id.notificationsFragment -> {
                    navController.navigate(R.id.notificationsFragment)
                    true
                }
                else -> false
            }
        }
    }
}
