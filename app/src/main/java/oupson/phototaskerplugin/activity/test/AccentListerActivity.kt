package oupson.phototaskerplugin.activity.test

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_accent_lister.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import oupson.phototaskerplugin.R
import oupson.phototaskerplugin.helper.OverlayHelper

class AccentListerActivity : AppCompatActivity() {
    class AccentAdapter(c: Context, resource : Int, list: MutableList<Pair<Int, String>>) : ArrayAdapter<Pair<Int, String>>(c, resource, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.test_accent_list_item, parent, false)
            getItem(position)?.let { item ->
                view.findViewById<TextView?>(R.id.test_accent_name)?.apply {
                    text = item.second
                    setBackgroundColor(item.first)
                }
            }
            return view
        }
    }

    private val colorList = mutableListOf<Pair<Int, String>>()
    private val adapter : AccentAdapter by lazy {
        AccentAdapter(this, 0, colorList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accent_lister)

        accentListView.adapter = adapter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            GlobalScope.launch(Dispatchers.IO) {
                OverlayHelper.getColorList(this@AccentListerActivity, false).forEach {
                    colorList.add(it.key to it.value)
                }
                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                }
            }
        } else {
            Toast.makeText(this, R.string.unsupported_device, Toast.LENGTH_SHORT).show()
        }
    }
}