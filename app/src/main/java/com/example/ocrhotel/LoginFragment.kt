package com.example.ocrhotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ocrhotel.databinding.FragmentLoginBinding
import android.util.Log
import android.widget.Toast

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

        login(username, password) { jwt ->
            if (jwt != null) {
                Log.w("JWT SUCCESS: ", jwt)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
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