package com.jcoronado.minimalbitcoinwidget

import android.app.Activity
import android.view.View
import android.widget.AdapterView

class SpinnerActivity : Activity(), AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if (parent != null) {
            when (position) {
                0 -> {
                    println("USD Selected")
                }
                1 -> println("GBP Selected")
                2 -> println("EUR Selected")
                else -> {
                    println("Error")
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("Nothing selected")
    }
}