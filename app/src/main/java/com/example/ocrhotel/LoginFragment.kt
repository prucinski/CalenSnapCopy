package com.example.ocrhotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ocrhotel.databinding.FragmentLoginBinding
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.loginButton.setOnClickListener {
            onLoginButtonPressed()
        }

        binding.goToSignupButton.setOnClickListener {
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.signupFragment)
        }
        return binding.root
    }

    private fun onLoginButtonPressed() {
        // There should be no whitespace in usernames, hence, use trim().
        val username = binding.usernameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()

        // Make sure not blank username or password is provided
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Please provide username and password.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        login(username, password) { jwt ->
            if (jwt != null) {
                val a = requireActivity() as MainActivity
                a.jwt = jwt

                // Login was successful
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    requireActivity().runOnUiThread {
                        readProfile(jwt) { profile ->
                            val a = requireActivity()
                            if (a is MainActivity) {
                                a.reloadEvents()
                                if (profile != null) {
                                    a.premiumAccount = profile.premium_user
                                    a.businessAccount = profile.business_user
                                    a.scans = profile.remaining_free_uses
                                }
                            }
                            // Return to previous fragment
                            a.supportFragmentManager.popBackStack()
                        }
                    }
                }

            } else {
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Username or password was wrong.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}