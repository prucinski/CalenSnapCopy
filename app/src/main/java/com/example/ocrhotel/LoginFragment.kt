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

        return binding.root
    }

    private fun onLoginButtonPressed() {
        val username = binding.usernameInput.text.toString()
        val password = binding.passwordInput.text.toString()

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
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()


                    val sh = requireActivity().getSharedPreferences(
                        getString(R.string.preferences_address),
                        AppCompatActivity.MODE_PRIVATE
                    )

                    if (sh != null) {
                        // Store JWT as a shared preference
                        val edit = sh.edit()
                        edit.putString("JWT", jwt)

                        activity?.runOnUiThread {
                            readProfile(jwt) { profile ->
                                // Set shared preference info about premium status
                                if (profile != null && profile.business_user) {
                                    edit.putBoolean("isPremiumUser", true)
                                } else {
                                    edit.putBoolean("isPremiumUser", false)
                                }
                                edit.apply()
                                val a = (activity as MainActivity?)
                                Log.w("ACTIVITY MAIN", a.toString())
                                a?.resume()
                            }
                        }
                        edit.apply()
                    }
                    // Move to home, update the tables before moving.
                    (activity as MainActivity?)!!.jwtAndPopulateTables()
                    val navController = NavHostFragment.findNavController(this)
                    navController.navigate(R.id.home)
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