# RecyclerViewBuilder

A simple class for building RecyclerViews without having to make an adapter and a ViewHolder for each one.

## Usage

```kt
val adapter = RecyclerViewBuilder(this)
	.addView<String>(R.layout.item_text) { binding, item, context ->
		// If you use layout IDs instead of ViewBindings, you still
		// get a "viewbinding", which only has its root view set to
		// the view you specified
		binding.root.findViewById<TextView>(R.id.text).text = item
	}
	// You can assign different view types to specific conditions
	// You can also use ViewBindings instead of layout IDs by 
	// defining them as the second generic type in most methods
	.addView<String, ItemTextSpecialBinding>({
		it.startsWith('!')
	}) { binding, item, context ->
		binding.text.text = item.substring(1)
	}
	// Easier way of adding infinite scrolling
	.setScrollToBottomListener { adapter ->
		adapter.addItems((1..5).map { "Item $it" })
	}
	// If you want to customize the "No Items" view
	.setEmptyListItem(R.layout.item_empty_list)
	// If you use the builder correctly, you won't need this, but
	// if you want to customize the "RecyclerView item for object 
	// of type %s not found" message
	.setErrorItem(R.layout.item_error_item) { binding, item, context ->
		binding.root.findViewById<TextView>(R.id.title).text =
			"RecyclerView item for object of type %s not found".format(
				item.javaClass.name,
				item.toString()
			)
		binding.root.findViewById<TextView>(R.id.details).text = item.toString()
	}
	// Show a loading indicator too if the adapter is loading
	// (You'll have to manually call adapter.setLoading)
	.setLoadingItem(R.layout.item_loading)
	// This is the default, but if you need horizontal or reversed 
	// layout managers, you can set them here
	.setLinearLayoutManager(LinearLayoutManager.VERTICAL)
	// or just use something else!
	.setLayoutManager(GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false))
	// Build the adapter and assign it to a RecyclerView
	.build(binding.recycler)

adapter.addItem("Hello world!")
```