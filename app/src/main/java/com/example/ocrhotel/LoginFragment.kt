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
            Log.w("BUTTON", "pressed")
            onLoginButtonPressed()
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
                Log.w("JWT SUCCESS: ", jwt)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()


                    val sh = activity?.getSharedPreferences(
                        getString(R.string.preferences_address),
                        AppCompatActivity.MODE_PRIVATE
                    )

                    if (sh != null) {
                        // Store JWT as a shared preference
                        val edit = sh.edit()
                        edit.putString("JWT", jwt)
                        edit.apply()
                    }

                }
            } else {
                Log.w("JWT FAILURE: ", jwt)
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