package com.etecnia.testlibapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.citisend.citiwastelib.CitiConnect
import com.citisend.citiwastelib.State
import com.etecnia.testlibapp.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var citiConnect: CitiConnect
    private var _binding: FragmentFirstBinding? = null
    private var presencia: Boolean? = false
    private var identificacion: Boolean? = false

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
        citiConnect = CitiConnect(activity, 5888, 111, 4);
        binding.simulate.setOnClickListener {
           // citiConnect.simulateWaste()
        }
        binding.simulate2.setOnClickListener {
           // citiConnect.simulateWasteSucceeded()
        }
        binding.simulate3.setOnClickListener {
           // citiConnect.simulateWasteRejected()
        }
        binding.buttonFirst.setOnClickListener {
            presencia = false;
            identificacion = false
            if (!citiConnect.isMonitoring) {
                binding.buttonFirst.setBackgroundColor(Color.RED)
                binding.buttonFirst.text = "PARAR"
                binding.textviewFirst.text = ""
                this.citiConnect.discover({ name, state ->
                    Log.d("DISCOVER", name)
                    if (presencia == false) {
                        if (state == State.EVENT_PRESENCE_DETECTED) {
                            this.presencia = true
                            this.binding.textviewFirst.text = "Presencia detectada"
                        }
                    } else {
                        if (state == State.EV2_SUCCEED_EVENT) {
                            identificacion = true
                            this.binding.textviewFirst.text =
                                "${this.binding.textviewFirst.text}${System.getProperty("line.separator")} Identificación realizada"
                            binding.buttonFirst.setBackgroundColor(Color.MAGENTA)
                            binding.buttonFirst.text = "DESCUBRIR"
                        }
                        if (state == State.EV2_ERROR_ID_REJECTED_POLITICS_DEVICE_MODE) {
                            this.binding.textviewFirst.text =
                                "${this.binding.textviewFirst.text}${System.getProperty("line.separator")} EV2_ERROR_ID_REJECTED_POLITICS_DEVICE_MODE"
                            binding.buttonFirst.setBackgroundColor(Color.MAGENTA)
                            binding.buttonFirst.text = "DESCUBRIR"
                        }
                        if (state == State.EV2_ERROR_ID_REJECTED_WRONG_PROJECT) {
                            this.binding.textviewFirst.text =
                                "${this.binding.textviewFirst.text}${System.getProperty("line.separator")} EV2_ERROR_ID_REJECTED_WRONG_PROJECT"
                            binding.buttonFirst.setBackgroundColor(Color.MAGENTA)
                            binding.buttonFirst.text = "DESCUBRIR"
                        }
                    }
                }, { error ->
                    binding.buttonFirst.setBackgroundColor(Color.MAGENTA)
                    binding.buttonFirst.text = "DESCUBRIR"
                    if (error == TIME_OUT) {
                        this.binding.textviewFirst.text =
                            "${this.binding.textviewFirst.text}${System.getProperty("line.separator")} Tiempo excedido en la Identificación"
                    }
                })
            } else {
                binding.buttonFirst.setBackgroundColor(Color.MAGENTA)
                binding.buttonFirst.text = "DESCUBRIR"
                this.binding.textviewFirst.text = ""
                citiConnect.destroy()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        citiConnect.destroy()
        _binding = null
    }
}