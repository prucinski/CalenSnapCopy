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
        return binding.root
    }

    private fun onLoginButtonPressed() {
        // There should be no whitespace in passwords and usernames, hence, use trim().
        val username = binding.usernameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

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
                // Login was successful
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()

                    val sh = requireActivity().getSharedPreferences(
                        getString(R.string.preferences_address),
                        AppCompatActivity.MODE_PRIVATE
                    )

                    if (sh != null) {
                        // Store JWT as a shared preference
                        val edit = sh.edit()
                        edit.putString("JWT", jwt)

                        requireActivity().runOnUiThread {
                            readProfile(jwt) { profile ->
                                val sh = requireActivity().getSharedPreferences(
                                    getString(R.string.preferences_address),
                                    AppCompatActivity.MODE_PRIVATE
                                )
                                if (sh != null) {
                                    val edit = sh.edit()
                                    // Set shared preference info about premium status
                                    edit.putBoolean(
                                        "isPremiumUser",
                                        profile != null && profile.business_user
                                    )
                                    edit.apply()
                                    val a = requireActivity()
                                    if (a is MainActivity) {
                                        a.resume()
                                        a.reloadEvents()
                                    }

                                    // Return to previous fragment
                                    a.supportFragmentManager?.popBackStack()
                                }
                            }
                        }
                        edit.apply()
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