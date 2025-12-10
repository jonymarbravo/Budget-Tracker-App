package com.example.budgettracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.budgettracker.activities.LoginActivity
import com.example.budgettracker.databinding.FragmentSettingsBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserInfo()
        setupClickListeners()
    }

    private fun loadUserInfo() {
        val user = auth.currentUser
        binding.tvUserEmail.text = user?.email ?: "Not available"

        // Load user name from Firestore
        user?.uid?.let { userId ->
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: "User"
                    binding.tvUserName.text = name
                }
        }
    }

    private fun setupClickListeners() {
        binding.cardChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.cardDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        binding.cardLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(
            com.example.budgettracker.R.layout.dialog_change_password,
            null
        )

        val etCurrentPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.budgettracker.R.id.etCurrentPassword
        )
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.budgettracker.R.id.etNewPassword
        )
        val etConfirmPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.budgettracker.R.id.etConfirmPassword
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPwd = etCurrentPassword.text.toString()
                val newPwd = etNewPassword.text.toString()
                val confirmPwd = etConfirmPassword.text.toString()

                if (validatePasswordChange(currentPwd, newPwd, confirmPwd)) {
                    changePassword(currentPwd, newPwd)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validatePasswordChange(current: String, new: String, confirm: String): Boolean {
        if (current.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new != confirm) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        val email = user?.email ?: return

        // Re-authenticate user
        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Update password
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to change password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone!")
            .setPositiveButton("Delete") { _, _ ->
                showPasswordConfirmationDialog()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPasswordConfirmationDialog() {
        val dialogView = layoutInflater.inflate(
            com.example.budgettracker.R.layout.dialog_confirm_password,
            null
        )

        val etPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.budgettracker.R.id.etPassword
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Password")
            .setMessage("Enter your password to confirm account deletion")
            .setView(dialogView)
            .setPositiveButton("Confirm") { _, _ ->
                val password = etPassword.text.toString()
                if (password.isNotEmpty()) {
                    deleteAccount(password)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount(password: String) {
        val user = auth.currentUser
        val email = user?.email ?: return
        val userId = user.uid

        // Re-authenticate
        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Delete from Firestore first
                firestore.collection("users").document(userId).delete()
                    .addOnSuccessListener {
                        // Then delete auth account
                        user.delete()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                goToLogin()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to delete account: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                goToLogin()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun goToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}