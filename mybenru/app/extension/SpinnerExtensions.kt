package com.mybenru.app.extension

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

/**
 * Sets an item selection listener for a Spinner
 */
fun Spinner.setOnItemSelectedListener(listener: (parent: AdapterView<*>, view: View?, position: Int, id: Long) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            listener(parent, view, position, id)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Not used
        }
    }
}