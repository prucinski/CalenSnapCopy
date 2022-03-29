package com.example.ocrhotel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentContactUsBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ContactUsFragment : Fragment() {

    private var _binding: FragmentContactUsBinding? = null
    private var ourMail = "agentdiegoo@o2.pl"
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentContactUsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.postMessage.setOnClickListener {
            //checking if fields are filled out. Rather terrible but it's better than nothing.

            //TODO: This regex at the bottom is finicky and doesn't work
            if(binding.yourEmail.text.toString() == ""){
                Toast.makeText(context, "Error - please supply a valid email address!", Toast.LENGTH_SHORT).show()
            }
            else if (binding.yourSubject.text.toString() == ""){
                Toast.makeText(context, "Error - please supply a valid subject!", Toast.LENGTH_SHORT).show()
            }
            else if (binding.yourMessage.text.toString() == ""){
                Toast.makeText(context, "Error - please supply a valid message!", Toast.LENGTH_SHORT).show()
            }
            else{
                sendEmail()
                findNavController().navigateUp()
            }

        }


    }
    //TODO: Can someone verify this regex? Idk why but it doesn't work
    fun checkEmail() : Boolean{
        val mail = binding.postMessage.text
        val mailRegex = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$").toRegex()
        return mailRegex.matches(mail)
    }
    private fun sendEmail(){
        //via https://devofandroid.blogspot.com/2018/11/send-email-using-intent-android-studio.html
        val mailIntent = Intent(Intent.ACTION_SEND)
        mailIntent.data = Uri.parse("mailto:")
        mailIntent.type = "text/plain"
        mailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(ourMail))
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, binding.yourSubject.text.toString())
        //This is very poor design
        val newMessage = "Case identifier: " + binding.yourEmail.text.toString() + System.lineSeparator() + binding.yourMessage.text.toString()
        mailIntent.putExtra(Intent.EXTRA_TEXT, newMessage)
        try {
            //start email intent
            startActivity(Intent.createChooser(mailIntent, "Choose Email Client..."))
        }
        catch (e: Exception){
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }
}