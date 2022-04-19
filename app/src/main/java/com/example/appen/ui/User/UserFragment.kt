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
    //aktivitet
    lateinit var spinner: Spinner
    //hudtype & klær
    var seekBar1Value = 0
    var seekBar2Value = 0
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
        spinner = binding.profilSpinner
        /*
        val seekBar3: SeekBar = binding.profilSeekBar3
        val seekBar3Value = binding.profilSeekBar3.progress
        */

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_profil,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        lagre = binding.profilLagre
        lagre.setOnClickListener{
            kalkulerAnbefaling()
        }
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
        var spinnerverdi = 0
        val uvTime : Float = 4.0F
        val seekBar1Value= binding.profilSeekBar.progress
        val seekBar2Value= binding.profilSeekBar2.progress
        var tv = binding.uvTestTv
        when(spinner.selectedItem){
            "Velg Aktivitet"-> spinnerverdi = 0
            "Bytur"         -> spinnerverdi = 4
            "Parken"        -> spinnerverdi = 1
            "Fjelltur"      -> spinnerverdi = 2
            "Stranda"       -> spinnerverdi = 0
        }
        // max score 12 - 4+4+4
        // Ikke ulik verdi for faktorene
        beskyttelseScore = spinnerverdi+seekBar1Value+seekBar2Value
        Log.d("BeskyttelseScore", beskyttelseScore.toString())

        // max beskyttelseScore 12
        // when eller if (>/<)
        when(uvTime.toInt()){
            in 0..2     -> {
                when(beskyttelseScore){
                    in 0..2 -> anbefalSpf15()
                    in 2..5 -> anbefalSpf30()

                }
            }
            in 2..5     -> {
                if(beskyttelseScore>=10){
                    anbefalSpf15()
                } else {
                    anbefalSpf20()
                }
            }
            in 5..7     -> {
                when(beskyttelseScore){


                }
            }
            in 7..10    -> {
                when(beskyttelseScore){


                }
            }
            in 10..11   -> {
                when(beskyttelseScore){


                }
            }
        }
    }
    fun anbefalSpf10(){
        val tv = binding.uvTestTv
        tv.text = "Anbefaler Spf 10"
    }
    fun anbefalSpf15(){
        val tv = binding.uvTestTv
        tv.text = "Anbefaler Spf 15"
    }
    fun anbefalSpf20(){
        val tv = binding.uvTestTv
        tv.text = "Anbefaler Spf 20"
    }
    fun anbefalSpf30(){
        val tv = binding.uvTestTv
        tv.text = "Anbefaler Spf 30"
    }
    fun anbefalSpf50(){
        val tv = binding.uvTestTv
        tv.text = "Anbefaler Spf 50"
    }

}
