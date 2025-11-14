package dev.kuylar.recyclerviewbuilder.views

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewbinding.ViewBinding

class EmptyListViewBinding(context: Context) : ViewBinding {
	override fun getRoot() = view
	val view = TextView(context)

	init {
		view.text = "No items"
		view.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
		val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
		view.layoutParams = layoutParams
	}
}