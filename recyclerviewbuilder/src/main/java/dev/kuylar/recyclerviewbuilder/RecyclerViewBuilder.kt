@file:Suppress("UNCHECKED_CAST")

package dev.kuylar.recyclerviewbuilder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import dev.kuylar.recyclerviewbuilder.views.EmptyListViewBinding
import dev.kuylar.recyclerviewbuilder.views.ItemNotFoundViewBinding
import dev.kuylar.recyclerviewbuilder.views.NullViewBinding

class RecyclerViewBuilder(private val context: Context) {
	val typesMap = mutableMapOf<Int, RecyclerViewItemType<Any>>()
	private var emptyListItem: RecyclerViewItemType<Any>? = null
	private var undefinedItem: RecyclerViewItemType<Any>? = null
	private var loadingItem: RecyclerViewItemType<Any>? = null
	private var layoutManager: RecyclerView.LayoutManager =
		LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
	private var scrollToBottomListener: ((adapter: ExtensibleRecyclerAdapter) -> Unit)? = null
	private var scrollToTopListener: ((adapter: ExtensibleRecyclerAdapter) -> Unit)? = null
	private var scrollToLeftListener: ((adapter: ExtensibleRecyclerAdapter) -> Unit)? = null
	private var scrollToRightListener: ((adapter: ExtensibleRecyclerAdapter) -> Unit)? = null

	inline fun <reified TItem, reified TBinding : ViewBinding> addView(crossinline bindMethod: (binding: TBinding, item: TItem, context: Context) -> Unit): RecyclerViewBuilder {
		val inflateMethod = TBinding::class.java.getDeclaredMethod(
			"inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
		)

		val id = typesMap.size
		val type = RecyclerViewItemType<Any>(
			id,
			clazz = TItem::class.java as Class<Any>,
			{ true },
			{ b, i, c -> bindMethod.invoke(b as TBinding, i as TItem, c) },
			{ layoutInflater, parent ->
				inflateMethod.invoke(
					null, layoutInflater, parent, false
				) as TBinding
			})
		typesMap[id] = type

		return this
	}

	inline fun <reified TItem, reified TBinding : ViewBinding> addView(
		noinline condition: (item: TItem) -> Boolean,
		crossinline bindMethod: (binding: TBinding, item: TItem, context: Context) -> Unit
	): RecyclerViewBuilder {
		val inflateMethod = TBinding::class.java.getDeclaredMethod(
			"inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
		)

		val id = typesMap.size
		val type = RecyclerViewItemType(
			id,
			clazz = TItem::class.java as Class<Any>,
			condition as (item: Any) -> Boolean,
			{ b, i, c -> bindMethod.invoke(b as TBinding, i as TItem, c) },
			{ layoutInflater, parent ->
				inflateMethod.invoke(
					null, layoutInflater, parent, false
				) as TBinding
			})
		typesMap[id] = type

		return this
	}

	inline fun <reified TItem> addView(
		@LayoutRes layout: Int,
		crossinline bindMethod: (binding: ViewBinding, item: TItem, context: Context) -> Unit
	): RecyclerViewBuilder {
		val id = typesMap.size
		val type = RecyclerViewItemType<Any>(
			id,
			clazz = TItem::class.java as Class<Any>,
			{ true },
			{ b, i, c -> bindMethod.invoke(b as NullViewBinding, i as TItem, c) },
			{ layoutInflater, parent ->
				NullViewBinding(layoutInflater.inflate(layout, parent, false))
			})
		typesMap[id] = type

		return this
	}

	inline fun <reified TItem> addView(
		@LayoutRes layout: Int,
		noinline condition: (item: TItem) -> Boolean,
		crossinline bindMethod: (binding: ViewBinding, item: TItem, context: Context) -> Unit
	): RecyclerViewBuilder {
		val id = typesMap.size
		val type = RecyclerViewItemType(
			id,
			clazz = TItem::class.java as Class<Any>,
			condition as (item: Any) -> Boolean,
			{ b, i, c -> bindMethod.invoke(b as NullViewBinding, i as TItem, c) },
			{ layoutInflater, parent ->
				NullViewBinding(layoutInflater.inflate(layout, parent, false))
			})
		typesMap[id] = type

		return this
	}

	fun setEmptyListItem(bindingClass: Class<ViewBinding>): RecyclerViewBuilder {
		val inflateMethod = bindingClass.getDeclaredMethod(
			"inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
		)
		emptyListItem = RecyclerViewItemType(
			ExtensibleRecyclerAdapter.NO_ITEMS_VIEW_TYPE,
			Any::class.java,
			{ true },
			{ _, _, _ -> },
			{ layoutInflater, parent ->
				inflateMethod(layoutInflater, parent, false) as ViewBinding
			})
		return this
	}

	fun setEmptyListItem(@LayoutRes layout: Int): RecyclerViewBuilder {
		emptyListItem = RecyclerViewItemType(
			ExtensibleRecyclerAdapter.NO_ITEMS_VIEW_TYPE,
			Any::class.java,
			{ true },
			{ _, _, _ -> },
			{ layoutInflater, parent ->
				NullViewBinding(layoutInflater.inflate(layout, parent, false))
			})
		return this
	}

	fun <TBinding> setUndefinedItem(
		bindingClass: Class<TBinding>,
		bindMethod: (binding: TBinding, failedItem: Any, context: Context) -> Unit
	): RecyclerViewBuilder {
		val inflateMethod = bindingClass.getDeclaredMethod(
			"inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
		)
		undefinedItem = RecyclerViewItemType(
			ExtensibleRecyclerAdapter.UNDEFINED_ITEM_VIEW_TYPE,
			Any::class.java,
			{ true },
			{ binding, item, context -> bindMethod(binding as TBinding, item, context) },
			{ layoutInflater, parent ->
				inflateMethod(layoutInflater, parent, false) as ViewBinding
			})
		return this
	}

	fun setUndefinedItem(
		@LayoutRes layout: Int,
		bindMethod: (binding: NullViewBinding, failedItem: Any, context: Context) -> Unit
	): RecyclerViewBuilder {
		undefinedItem = RecyclerViewItemType(
			ExtensibleRecyclerAdapter.UNDEFINED_ITEM_VIEW_TYPE,
			Any::class.java,
			{ true },
			{ binding, item, context -> bindMethod(binding as NullViewBinding, item, context) },
			{ layoutInflater, parent ->
				NullViewBinding(layoutInflater.inflate(layout, parent, false))
			})
		return this
	}

	fun setLoadingItem(bindingClass: Class<ViewBinding>): RecyclerViewBuilder {
		val inflateMethod = bindingClass.getDeclaredMethod(
			"inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
		)
		loadingItem = RecyclerViewItemType(
			ExtensibleRecyclerAdapter.LOADING_ITEM_VIEW_TYPE,
			Any::class.java,
			{ true },
			{ _, _, _ -> },
			{ layoutInflater, parent ->
				inflateMethod(layoutInflater, parent, false) as ViewBinding
			})
		return this
	}

	fun setLoadingItem(@LayoutRes layout: Int): RecyclerViewBuilder {
		loadingItem = RecyclerViewItemType(
			ExtensibleRecyclerAdapter.LOADING_ITEM_VIEW_TYPE,
			Any::class.java,
			{ true },
			{ _, _, _ -> },
			{ layoutInflater, parent ->
				NullViewBinding(layoutInflater.inflate(layout, parent, false))
			})
		return this
	}

	fun setLayoutManager(layoutManager: RecyclerView.LayoutManager): RecyclerViewBuilder {
		this.layoutManager = layoutManager
		return this
	}

	fun setLinearLayoutManager(
		orientation: Int, reverseLayout: Boolean = false
	): RecyclerViewBuilder {
		setLayoutManager(LinearLayoutManager(context, orientation, reverseLayout))
		return this
	}

	fun setScrollToBottomListener(listener: (adapter: ExtensibleRecyclerAdapter) -> Unit): RecyclerViewBuilder {
		scrollToBottomListener = listener
		return this
	}

	fun setScrollToTopListener(listener: (adapter: ExtensibleRecyclerAdapter) -> Unit): RecyclerViewBuilder {
		scrollToTopListener = listener
		return this
	}

	fun setScrollToLeftListener(listener: (adapter: ExtensibleRecyclerAdapter) -> Unit): RecyclerViewBuilder {
		scrollToLeftListener = listener
		return this
	}

	fun setScrollToRightListener(listener: (adapter: ExtensibleRecyclerAdapter) -> Unit): RecyclerViewBuilder {
		scrollToRightListener = listener
		return this
	}

	fun build(recyclerView: RecyclerView): ExtensibleRecyclerAdapter {
		val tm = typesMap.toMutableMap()
		tm[ExtensibleRecyclerAdapter.NO_ITEMS_VIEW_TYPE] = emptyListItem ?: RecyclerViewItemType(
			ExtensibleRecyclerAdapter.NO_ITEMS_VIEW_TYPE,
			Any::class.java,
			{ true },
			{ _, _, _ -> },
			{ _, _ ->
				return@RecyclerViewItemType EmptyListViewBinding(context)
			})
		tm[ExtensibleRecyclerAdapter.UNDEFINED_ITEM_VIEW_TYPE] =
			undefinedItem ?: RecyclerViewItemType(
				ExtensibleRecyclerAdapter.UNDEFINED_ITEM_VIEW_TYPE,
				Any::class.java,
				{ true },
				{ binding: ViewBinding, type, _ ->
					(binding.root as TextView).text =
						"RecyclerView item for object of type %s not found\n%s".format(
							type.javaClass.name, type.toString()
						)
				},
				{ _, _ ->
					return@RecyclerViewItemType ItemNotFoundViewBinding(context)
				})
		if (loadingItem != null) tm[ExtensibleRecyclerAdapter.LOADING_ITEM_VIEW_TYPE] =
			loadingItem!!
		val adapter = ExtensibleRecyclerAdapter(context, tm.toMap())
		recyclerView.layoutManager = layoutManager
		recyclerView.adapter = adapter


		recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)

				val layoutManager = recyclerView.layoutManager as LinearLayoutManager? ?: return
				val totalItemCount = layoutManager.getItemCount()
				val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
				val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

				if (layoutManager.orientation == RecyclerView.VERTICAL) {
					if (dy > 0) {
						if (totalItemCount <= (lastVisibleItemPosition + 1)) {
							scrollToBottomListener?.invoke(adapter)
						}
					}

					if (dy < 0) {
						if (firstVisibleItemPosition == 0) {
							scrollToTopListener?.invoke(adapter)
						}
					}
				} else {
					if (dx > 0) {
						if (totalItemCount <= (lastVisibleItemPosition + 1)) {
							scrollToRightListener?.invoke(adapter)
						}
					}

					if (dx < 0) {
						if (firstVisibleItemPosition == 0) {
							scrollToLeftListener?.invoke(adapter)
						}
					}
				}
			}
		})

		return adapter
	}
}