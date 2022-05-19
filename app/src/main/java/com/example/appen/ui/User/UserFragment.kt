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
import Uv
import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.appen.Location
import com.example.appen.MainActivity
import com.example.appen.ui.Home.HomeViewModel
import com.example.appen.ui.Home.sharedPreferencesUser
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

class UserFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //Score
    var beskyttelseScore = 0
    //hudtype
    lateinit var seekBar1Value: SeekBar
    //lagreKnapp
    lateinit var lagre : Button

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
            textView.text = "Juster informasjonen så den passer deg!"
        }
        lagre = binding.profilLagre
        lagre.setOnClickListener{

            save()

        }
        return root
    }

    companion object{
        val user = this
    }

    override fun onResume() {
        super.onResume()
        seekBar1Value = binding.profilSeekBar
        val hoydemeter: CheckBox = binding.hoydemeter
        val snoo: CheckBox = binding.snoo2

        seekBar1Value.progress = sharedPreferencesUser.getSliderValue(requireContext())
        hoydemeter.isChecked = sharedPreferencesUser.getTooglesValue(requireContext(), 1)
        snoo.isChecked = sharedPreferencesUser.getTooglesValue(requireContext(), 2)

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

    fun save(){
        //Toogles
        val hoydemeter: CheckBox = binding.hoydemeter
        val snoo2: CheckBox = binding.snoo2


        Log.d("BeskyttelseScore", beskyttelseScore.toString())
        sharedPreferencesUser.setSliderValue(seekBar1Value.progress,requireContext())
        sharedPreferencesUser.setTooglesValue(hoydemeter.isChecked, 1, requireContext())
        sharedPreferencesUser.setTooglesValue(snoo2.isChecked, 2, requireContext())

    }
}
