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
        var seekBar1Value = binding.profilSeekBar.progress.toDouble()
        seekBar1Value = (seekBar1Value+1) * 16.67
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
        beskyttelseScore = seekBar1Value.toInt() + seekBar2Value

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
        Log.d("BeskyttelseScore", beskyttelseScore.toString())
        //Toogles
        when(uvTime){
            in 0.0F..2.0F     -> anbefalSpf6()

            in 2.0F..4.0F     -> {
                when(beskyttelseScore){
                    in 0..20 -> anbefalSpf20()
                    in 20..50 -> anbefalSpf15()
                    in 50..70 -> anbefalSpf10()
                    in 70..100 -> anbefalSpf6()

                }
            }
            in 4.0F..6.0F     -> {
                when(beskyttelseScore){
                    in 0..20 -> anbefalSpf30()
                    in 20..50 -> anbefalSpf20()
                    in 50..70 -> anbefalSpf15()
                    in 70..100 -> anbefalSpf10()

                }
            }
            in 6.0F..8.0F    -> {
                when(beskyttelseScore){
                    in 0..20 -> anbefalSpf40()
                    in 20..50 -> anbefalSpf30()
                    in 50..70 -> anbefalSpf20()
                    in 70..100 -> anbefalSpf15()

                }
            }
            in 8.0F..11.0F   -> {
                when(beskyttelseScore){
                    in 0..20 -> anbefalSpf50()
                    in 20..50 -> anbefalSpf40()
                    in 50..100 -> anbefalSpf30()
                }
            }
        }
    }
    fun anbefalSpf6(){
        val tv = binding.uvTestTv
        tv.text = "Anbefaler Spf 6"
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
    fun anbefalSpf40(){
        val tv = binding.uvTestTv
        tv.text = "Anbefaler Spf 40"
    }
    fun anbefalSpf50(){
        val tv = binding.uvTestTv
        tv.text = "Anbefaler Spf 50"
    }

}
