package com.example.ocrhotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ocrhotel.databinding.FragmentTOSBinding
import io.noties.markwon.Markwon
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

class TOSFragment : Fragment() {

    private lateinit var _binding : FragmentTOSBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_t_o_s, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val license = resources.openRawResource(R.raw.license)

        val content: String = BufferedReader(InputStreamReader(license))
            .lines().parallel().collect(Collectors.joining("\n"))

        val textView = activity?.findViewById<TextView>(R.id.terms)

        // obtain an instance of Markwon
        val markwon : Markwon = Markwon.create(requireContext())

        // set markdown
        markwon.setMarkdown(textView!!, content);
    }
}