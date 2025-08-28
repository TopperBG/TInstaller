package ru.yourok.tinstaller

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.yourok.tinstaller.app.App
import ru.yourok.tinstaller.content.Link

class LinksAdapter(val list: List<Link>) : RecyclerView.Adapter<LinksAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vi = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        vi.nextFocusRightId = R.id.btnInstall
        val holder = ViewHolder(vi)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.view
        val itm = list[position]

        if (!itm.title.isNullOrEmpty()) {
            view.findViewById<TextView>(R.id.tvTitle)?.text = itm.title
            view.findViewById<TextView>(R.id.tvDescription)?.text = itm.description
        } else if (!itm.description.isNullOrEmpty()) {
            view.findViewById<TextView>(R.id.tvTitle)?.text = itm.description
        } else {
            view.findViewById<TextView>(R.id.tvTitle)?.text = itm.url
        }

        if (itm.app_review != null && itm.app_review.isNotEmpty()) {
            view.findViewById<ImageView>(R.id.btnReview).visibility = View.VISIBLE
            view.findViewById<ImageView>(R.id.btnReview).setOnClickListener {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(itm.app_review))
                webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                view.context.startActivity(webIntent)
            }
        } else {
            view.findViewById<ImageView>(R.id.btnReview).visibility = View.GONE
        }

        view.findViewById<ProgressBar>(R.id.pbLoad)?.visibility = View.GONE
        view.findViewById<ImageView>(R.id.ivDone)?.visibility = View.GONE
        view.findViewById<CheckBox>(R.id.cbSelectApp)?.visibility = View.GONE

        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(itm.url))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.getContext().startActivity(intent)
        }

        if (itm.app_review != null && itm.app_review.isNotEmpty()) {
            view.setOnLongClickListener {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(itm.app_review))
                webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                view.context.startActivity(webIntent)
                true
            }
        }
    }

    override fun getItemCount() = list.size
}
