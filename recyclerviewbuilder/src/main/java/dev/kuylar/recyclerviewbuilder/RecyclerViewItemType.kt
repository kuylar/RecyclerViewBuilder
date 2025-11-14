package dev.kuylar.recyclerviewbuilder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

class RecyclerViewItemType<T>(
	val id: Int,
	val clazz: Class<Any>,
	val condition: (item: T) -> Boolean,
	val bindMethod: (binding: ViewBinding, item: T, context: Context) -> Unit,
	val inflateMethod: (LayoutInflater, ViewGroup) -> ViewBinding
)