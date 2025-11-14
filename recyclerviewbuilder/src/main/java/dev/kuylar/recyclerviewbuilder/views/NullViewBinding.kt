package dev.kuylar.recyclerviewbuilder.views

import android.view.View
import androidx.viewbinding.ViewBinding

class NullViewBinding(private val view: View) : ViewBinding {
	override fun getRoot() = view
}