package com.example.appen.ui.Home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val _beskyttelsesScore = MutableLiveData<Int>().apply {
        value = 0
    }
    val text: LiveData<String> = _text
}