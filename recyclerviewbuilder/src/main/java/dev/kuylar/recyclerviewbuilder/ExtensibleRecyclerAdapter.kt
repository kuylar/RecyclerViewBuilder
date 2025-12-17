package dev.kuylar.recyclerviewbuilder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class ExtensibleRecyclerAdapter(
	private val context: Context, private val typesMap: Map<Int, RecyclerViewItemType<Any>>
) : RecyclerView.Adapter<ExtensibleRecyclerAdapter.ViewHolder>() {
	class ViewHolder(view: View, private val bindMethod: (item: Any, context: Context) -> Unit) :
		RecyclerView.ViewHolder(view) {
		fun bind(item: Any, context: Context) {
			bindMethod.invoke(item, context)
		}
	}

	val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
	val items = ArrayList<Any>()
	private var loading = false

		override fun onCreateViewHolder(
		parent: ViewGroup, viewType: Int
	): ViewHolder {
		val type = typesMap[viewType]!!
		val binding = type.inflateMethod(layoutInflater, parent)
		val method = { item: Any, context: Context -> type.bindMethod(binding, item, context) }
		return ViewHolder(binding.root, method)
	}

	override fun getItemViewType(position: Int): Int {
		if (items.isEmpty()) return if (loading) LOADING_ITEM_VIEW_TYPE else NO_ITEMS_VIEW_TYPE
		val item = items[position]
		val types = typesMap
			.filter { it.key >= 0 }
			.filter { it.value.clazz.isInstance(item) }
			.filter { it.value.condition.invoke(item) }

		return types.firstNotNullOfOrNull { it.key } ?: UNDEFINED_ITEM_VIEW_TYPE
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (items.isEmpty()) return
		holder.bind(items[position], context)
	}

	override fun getItemCount() = max(1, items.size)

	fun setLoading(isLoading: Boolean) {
		loading = isLoading
		if (items.isEmpty()) notifyItemChanged(0)
	}

	fun addItem(item: Any) {
		// Remove the "no items" item
		if (items.isEmpty()) notifyItemRemoved(0)
		items.add(item)
		notifyItemInserted(items.size - 1)
	}

	fun addItems(newItems: Collection<Any>) {
		// Remove the "no items" item
		if (items.isEmpty()) notifyItemRemoved(0)
		items.addAll(newItems)
		notifyItemRangeInserted(items.size - newItems.size, newItems.size)
	}

	fun removeItem(item: Any) {
		val i = items.indexOf(item)
		if (i != -1) {
			items.removeAt(i)
			notifyItemRemoved(i)
		}
	}

	fun removeItems(removedItems: Collection<Any>) {
		removedItems.forEach { items.remove(it) }
	}

	fun insertItem(index: Int, item: Any) {
		items.add(index, item)
		notifyItemInserted(index)
	}

	fun insertMany(index: Int, addedItems: Collection<Any>) {
		items.addAll(index, addedItems)
		notifyItemRangeInserted(index, addedItems.size)
	}

	fun removeItemAt(i: Int) {
		if (items.isEmpty()) return
		items.removeAt(i)
		notifyItemRemoved(i)
	}

	fun removeItemsAt(i: Collection<Int>) {
		if (items.isEmpty()) return
		i.forEach { removeItemAt(it) }
	}

	fun clearItems() {
		val size = items.size
		items.clear()
		notifyItemRangeRemoved(0, size)
	}

	companion object {
		const val NO_ITEMS_VIEW_TYPE = -1
		const val UNDEFINED_ITEM_VIEW_TYPE = -2
		const val LOADING_ITEM_VIEW_TYPE = -3
	}
}