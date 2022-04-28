package com.example.ocrhotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ocrhotel.databinding.FragmentLoginBinding
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.loginButton.setOnClickListener {
            onLoginButtonPressed()
        }

        // Make the form click the button when enter is pressed.
        binding.passwordInput.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    onLoginButtonPressed()
                    true
                }
                else -> false
            }
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

        // Make sure no blank username or password is provided
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(
                context,
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
                a.runOnUiThread {
                    // Inform user
                    Toast.makeText(a, "Login successful!", Toast.LENGTH_SHORT).show()
                    readProfile(jwt) { profile -> // Now, the profile is loaded
                        a.reloadEvents()
                        if (profile != null) { // Update activity variables
                            a.premiumAccount = profile.premium_user
                            a.businessAccount = profile.business_user
                            a.scans = profile.remaining_free_uses
                            (activity as MainActivity).runOnUiThread{
                                a.updateAds()
                            }
                        }
                        // Return to previous fragment
                        a.supportFragmentManager.popBackStack()
                    }
                }

            } else {
                // In this case, there was an issue with logging in
                activity?.runOnUiThread {
                    Toast.makeText(
                        context,
                        "Wrong username or password.",
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