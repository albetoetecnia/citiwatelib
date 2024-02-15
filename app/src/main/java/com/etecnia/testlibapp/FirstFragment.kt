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
import com.citisend.citiwastelib.Error.TIME_OUT_DOOR
import com.citisend.citiwastelib.Error.TIME_OUT_LOCK
import com.citisend.citiwastelib.State
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
        citiConnect = CitiConnect(activity, 10, 20);
        binding.simulate.setOnClickListener {
            citiConnect.simulateWaste()
        }
        binding.buttonFirst.setOnClickListener {
            if (!citiConnect.isMonitoring) {
                binding.buttonFirst.setBackgroundColor(Color.RED)
                binding.buttonFirst.text = "PARAR"
                binding.textviewFirst.text = ""
                this.citiConnect.discover({ name, state ->
                    Log.d("DISCOVER", name)
                    if (state == State.EVENT_LOCK_CLOSED_PRESENCE) {
                        // Presencia detectada, proceder a crear beacon para apertura
                        this.binding.textviewFirst.text = "Presencia detectada"
                    }
                    if (state == State.EVENT_LOCK_OPENED) {
                        // Presencia detectada, proceder a crear beacon para apertura
                        this.binding.textviewFirst.text =
                            "${this.binding.textviewFirst.text}${System.getProperty("line.separator")}Pasador abierto"
                    }
                    if (state == State.EVENT_LOCK_CLOSED_DOOR_OPENED) {
                        // Presencia detectada, proceder a crear beacon para apertura
                        this.binding.textviewFirst.text =
                            "${this.binding.textviewFirst.text}${System.getProperty("line.separator")}Puerta abierta"
                        binding.buttonFirst.setBackgroundColor(Color.MAGENTA)
                        binding.buttonFirst.text = "DESCUBRIR"
                    }
                }, { error ->
                    citiConnect.destroy()
                    binding.buttonFirst.setBackgroundColor(Color.MAGENTA)
                    binding.buttonFirst.text = "DESCUBRIR"
                    if (error == TIME_OUT_DOOR) {
                        this.binding.textviewFirst.text = "Tiempo excedido en la apertura de PUERTA"
                    } else if (error == TIME_OUT_LOCK) {
                        this.binding.textviewFirst.text = "Tiempo excedido en la apertura del PASADOR"
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