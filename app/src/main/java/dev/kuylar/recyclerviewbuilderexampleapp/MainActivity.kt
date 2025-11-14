package dev.kuylar.recyclerviewbuilderexampleapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dev.kuylar.recyclerviewbuilder.RecyclerViewBuilder
import dev.kuylar.recyclerviewbuilderexampleapp.databinding.ActivityMainBinding
import dev.kuylar.recyclerviewbuilderexampleapp.databinding.ItemErrorItemBinding
import dev.kuylar.recyclerviewbuilderexampleapp.databinding.ItemTextSpecialBinding

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

		val adapter = RecyclerViewBuilder(this)
			// Special instance in case you want different bind handling
			.addView<String, ItemTextSpecialBinding>({
				it.startsWith('!')
			}) { binding, item, context ->
				binding.text.text = item.substring(1)
			}
			// For each item of type String, automatically inflate an ItemTextBinding
			// and bind it using the method below
			.addView<String>(R.layout.item_text) { binding, item, context ->
				binding.root.findViewById<TextView>(R.id.text).text = item
			}
			.setScrollToBottomListener { adapter ->
				adapter.addItems((1 .. 5).map { "Item $it" })
			}
			// Optional, set default views for when a type of object isn't handled
			.setUndefinedItem(ItemErrorItemBinding::class.java) { binding, item, context ->
				binding.title.text =
					"RecyclerView item for object of type %s not found\n%s".format(
						item.javaClass.name,
						item.toString()
					)
			}
			// Or just use layout IDs
			.setUndefinedItem(R.layout.item_error_item) { binding, item, context ->
				// If a layout ID is passed in, a null viewbinding is
				// created, which only has the root view set to your
				// layout's root item.
				binding.root.findViewById<TextView>(R.id.title).text =
					"RecyclerView item for object of type %s not found".format(
						item.javaClass.name,
						item.toString()
					)
				binding.root.findViewById<TextView>(R.id.details).text = item.toString()
			}
			// Same works for when there are no items to show
			.setEmptyListItem(R.layout.item_empty_list)
			// Show a loading indicator too if the adapter is loading
			.setLoadingItem(R.layout.item_loading)
			// Optional, but it exists :thumbs_up:
			.setLinearLayoutManager(LinearLayoutManager.VERTICAL)
			.build(binding.recycler)

		binding.addbutton.setOnClickListener {
			adapter.addItem("New Item")
		}
		binding.addinvalidbutton.setOnClickListener {
			adapter.addItem(1)
		}
		binding.addspecialbutton.setOnClickListener {
			adapter.addItem("!Special Item")
		}
		binding.removebutton.setOnClickListener {
			adapter.removeItemAt(adapter.itemCount - 1)
		}
		var loading = false
		binding.loadingbutton.setOnClickListener {
			loading = !loading
			adapter.setLoading(loading)
		}
	}
}