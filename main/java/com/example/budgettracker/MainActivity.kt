package com.example.budgettracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.budgettracker.activities.AddTransactionActivity
import com.example.budgettracker.activities.LoginActivity
import com.example.budgettracker.databinding.ActivityMainBinding
import com.example.budgettracker.fragments.HomeFragment
import com.example.budgettracker.fragments.SettingsFragment
import com.example.budgettracker.fragments.StatisticsFragment
import com.example.budgettracker.fragments.TransactionsFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupBottomNavigation()
        setupFab()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    binding.fabAddTransaction.show()
                    true
                }
                R.id.nav_transactions -> {
                    loadFragment(TransactionsFragment())
                    binding.fabAddTransaction.show()
                    true
                }
                R.id.nav_statistics -> {
                    loadFragment(StatisticsFragment())
                    binding.fabAddTransaction.show()
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    binding.fabAddTransaction.hide()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFab() {
        binding.fabAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}