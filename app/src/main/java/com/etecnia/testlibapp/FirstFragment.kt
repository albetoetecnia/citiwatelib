package com.etecnia.testlibapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.citisend.citiwastelib.CitiConnect
import com.etecnia.testlibapp.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var citiConnect: CitiConnect
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        citiConnect = CitiConnect(activity);
        binding.buttonFirst.setOnClickListener {
            this.citiConnect.discover {
                Log.d("DISCOVER", it)
                this.binding.textviewFirst.text = binding.textviewFirst.text.toString() + " " + it
                citiConnect.sendOpenSignal()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        citiConnect.destroy()
        _binding = null
    }
}