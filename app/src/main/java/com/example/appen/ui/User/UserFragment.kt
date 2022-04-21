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
import android.util.Log
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
    var location = activity as Location?

    //Score
    var beskyttelseScore = 0
    //aktivitet
    lateinit var spinner: Spinner
    //hudtype & klær
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

            kalkulerAnbefaling()

        }
        return root
    }

    override fun onResume() {
        super.onResume()
        seekBar1Value = binding.profilSeekBar
        val fjell: ToggleButton = binding.fjell
        val snoo: ToggleButton = binding.snoo
        val sand: ToggleButton = binding.sand
        val vann: ToggleButton = binding.vann
        seekBar1Value.progress = sharedPreferencesUser.getSliderValue(requireContext())
        fjell.isChecked = sharedPreferencesUser.getTooglesValue(requireContext(), 1)
        snoo.isChecked = sharedPreferencesUser.getTooglesValue(requireContext(), 2)
        sand.isChecked = sharedPreferencesUser.getTooglesValue(requireContext(), 3)
        vann.isChecked = sharedPreferencesUser.getTooglesValue(requireContext(), 4)
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

    fun onProgressChanged(seek: SeekBar,
                                    progress: Int, fromUser: Boolean) {
        // write custom code for progress is changed
    }

    fun onStartTrackingTouch(seek: SeekBar) {
        // write custom code for progress is started
    }

    fun onStopTrackingTouch(seek: SeekBar) {
        // write custom code for progress is stopped
    }

    fun kalkulerAnbefaling(){
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        var spinnerverdi = 0
        val uvTime : Float = 4.0F
        var seekBar1ValueRegning = binding.profilSeekBar.progress.toDouble()
        seekBar1ValueRegning = (seekBar1ValueRegning+1) * 16.67
        /*
        var seekBar2Value= binding.profilSeekBar2.progress
        seekBar2Value = seekBar2Value * 8
         */

        /*
        var tv = binding.uvTestTv
        when(spinner.selectedItem){
            "Velg Aktivitet"-> spinnerverdi = 0
            "Bytur"         -> spinnerverdi = 4
            "Parken"        -> spinnerverdi = 1
            "Fjelltur"      -> spinnerverdi = 2
            "Stranda"       -> spinnerverdi = 0
        }

         */
        // max score 12 - 4+4+4
        // Ikke ulik verdi for faktorene
        beskyttelseScore = seekBar1ValueRegning.toInt()

        // max beskyttelseScore 12
        // when eller if (>/<)

        //Toogles
        val fjell: ToggleButton = binding.fjell
        val snoo: ToggleButton = binding.snoo
        val sand: ToggleButton = binding.sand
        val vann: ToggleButton = binding.vann

        if(fjell.isChecked) {
            beskyttelseScore -= 10
        }
        if(snoo.isChecked) {
            beskyttelseScore -= 10
        }
        if(sand.isChecked) {
            beskyttelseScore -= 10
        }
        if(vann.isChecked) {
            beskyttelseScore -= 10
        }
        homeViewModel._beskyttelsesScore.value = beskyttelseScore
        Log.d("BeskyttelseScore", beskyttelseScore.toString())
        sharedPreferencesUser.setSliderValue(seekBar1Value.progress,requireContext())
        sharedPreferencesUser.setTooglesValue(fjell.isChecked, 1, requireContext())
        sharedPreferencesUser.setTooglesValue(snoo.isChecked, 2, requireContext())
        sharedPreferencesUser.setTooglesValue(sand.isChecked, 3, requireContext())
        sharedPreferencesUser.setTooglesValue(vann.isChecked, 4, requireContext())
    }
}
