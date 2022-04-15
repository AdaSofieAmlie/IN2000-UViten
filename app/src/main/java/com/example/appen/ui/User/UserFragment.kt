package com.example.appen.ui.User

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appen.R
import com.example.appen.databinding.FragmentNotificationsBinding

class UserFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(UserViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.profilTvOverspinner
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = "Hei, her kommer profilsiden"
        }

        val spinner: Spinner = binding.profilSpinner
        spinner.onItemSelectedListener = this
        val seekBar1: SeekBar = binding.profilSeekBar
        val seekBar1Value = 0
        val seekBar2: SeekBar = binding.profilSeekBar2
        val seekBar2Value = 0
        val seekBar3: SeekBar = binding.profilSeekBar3
        val seekBar3Value = 0

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_profil,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        /*
        x+y+z
        switchcasee/when for endelig verdi?
        when(x){

            2 -> println("This is 2")

            3,4,5,6,7,8 -> println("When x is any number from 3,4,5,6,7,8")

            in 9..15 -> println("When x is something from 9 to 15")

            //if you want to perform some action
            in 20..25 -> {
                val action = "Perform some action"
                println(action)
            }

            else -> println("When x does not belong to any of the above case")

        }
        */
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}
