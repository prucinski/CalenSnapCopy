package com.example.ocrhotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import com.example.ocrhotel.databinding.FragmentSignupBinding

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)

        binding.signupButton.setOnClickListener {
            onSignupButtonPressed()
        }
        return binding.root
    }

    private fun onSignupButtonPressed() {
        // There should be no whitespace in usernames, hence, use trim().
        val username = binding.usernameSignupInput.text.toString().trim()
        val password = binding.passwordSignupInput.text.toString()

        // Make sure not blank username or password is provided
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Please provide username and password.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        createProfile(username, password) { signupSuccess ->
            if (signupSuccess) {
                // If the signup was successful, automatically log in the user
                login(username, password) { jwt ->
                    if (jwt != null) {
                        val a = requireActivity() as MainActivity
                        a.jwt = jwt

                        // Login was successful
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Login successful!",
                                Toast.LENGTH_SHORT
                            ).show()
                            requireActivity().runOnUiThread {
                                readProfile(jwt) { profile ->
                                    // Update local profile information
                                    val a = requireActivity()
                                    if (a is MainActivity) {
                                        a.reloadEvents()
                                        if (profile != null) {
                                            a.premiumAccount = profile.premium_user
                                            a.businessAccount = profile.business_user
                                            a.scans = profile.remaining_free_uses
                                        }
                                    }

                                    requireActivity().runOnUiThread{
                                        // Navigate to the Home fragment
                                        val navHostFragment =
                                            requireActivity().supportFragmentManager.findFragmentById(
                                                R.id.main_content
                                            ) as NavHostFragment
                                        val navController = navHostFragment.navController
                                        navController.navigate(R.id.home)
                                    }

                                }
                            }
                        }
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Could not login. Please try to log in manually.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            } else {
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Could not create account.",
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