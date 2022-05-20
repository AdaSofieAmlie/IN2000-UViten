package com.example.appen.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import android.util.Log
import com.example.appen.R
import com.example.appen.databinding.FragmentProfilBinding
import com.example.appen.ui.home.SharedPreferencesUser

class UserFragment : Fragment(), AdapterView.OnItemSelectedListener {
    //Binding
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    //hudtype / score for anbefaling
    private lateinit var seekBar1Value: SeekBar
    //lagreKnapp
    private lateinit var lagre : Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.profilTvOverspinner
        textView.text = resources.getString(R.string.ProfilOverskrift)
        //lagreknapp
        lagre = binding.profilLagre
        lagre.setOnClickListener{
            //Lagrer informasjonen via Save()
            save()
            Toast.makeText(requireContext(), "Lagret", Toast.LENGTH_LONG).show()
        }
        return root
    }

    //onResume henter frem lagrede verdier i sharedpreferences
    override fun onResume() {
        super.onResume()
        seekBar1Value = binding.profilSeekBar
        val hoydemeter: CheckBox = binding.hoydemeter
        val snoo: CheckBox = binding.snoo2

        seekBar1Value.progress = SharedPreferencesUser.getSliderValue(requireContext())
        hoydemeter.isChecked = SharedPreferencesUser.getTooglesValue(requireContext(), 1)
        snoo.isChecked = SharedPreferencesUser.getTooglesValue(requireContext(), 2)

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

    //lagrer verdiene i sharedpreferences
    private fun save(){
        //Toggles
        val hoydemeter: CheckBox = binding.hoydemeter
        val snoo2: CheckBox = binding.snoo2


        Log.d("Test UserFragment save()", seekBar1Value.toString())
        SharedPreferencesUser.setSliderValue(seekBar1Value.progress,requireContext())
        SharedPreferencesUser.setTooglesValue(hoydemeter.isChecked, 1, requireContext())
        SharedPreferencesUser.setTooglesValue(snoo2.isChecked, 2, requireContext())

    }
}
