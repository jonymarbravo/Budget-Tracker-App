//Package: activities
//file name: SignupActivity.kt

package com.example.budgettracker.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgettracker.databinding.ActivitySignupBinding
import com.example.budgettracker.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val name = binding.etSignupName.text.toString().trim()
            val email = binding.etSignupEmail.text.toString().trim()
            val password = binding.etSignupPassword.text.toString().trim()
            val confirmPassword = binding.etSignupConfirmPassword.text.toString().trim()

            if (validateInputs(name, email, password, confirmPassword)) {
                createAccount(name, email, password)
            }
        }

        binding.tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isEmpty()) {
            binding.etSignupName.error = "Name is required"
            binding.etSignupName.requestFocus()
            return false
        }

        if (name.length < 3) {
            binding.etSignupName.error = "Name must be at least 3 characters"
            binding.etSignupName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            binding.etSignupEmail.error = "Email is required"
            binding.etSignupEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignupEmail.error = "Please enter valid email"
            binding.etSignupEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.etSignupPassword.error = "Password is required"
            binding.etSignupPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.etSignupPassword.error = "Password must be at least 6 characters"
            binding.etSignupPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.etSignupConfirmPassword.error = "Please confirm password"
            binding.etSignupConfirmPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            binding.etSignupConfirmPassword.error = "Passwords do not match"
            binding.etSignupConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun createAccount(name: String, email: String, password: String) {
        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    saveUserToFirestore(userId, name, email)
                } else {
                    showLoading(false)
                    val errorMessage = task.exception?.message ?: "Signup failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, name: String, email: String) {
        val user = User(
            uid = userId,
            email = email,
            name = name,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                showLoading(false)

                // IMPORTANT: Sign out after successful signup
                auth.signOut()

                Toast.makeText(
                    this,
                    "Account created successfully! Please login.",
                    Toast.LENGTH_LONG
                ).show()

                // Go back to login screen
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.progressBarSignup.visibility = View.VISIBLE
            binding.btnSignup.isEnabled = false
        } else {
            binding.progressBarSignup.visibility = View.GONE
            binding.btnSignup.isEnabled = true
        }
    }
}