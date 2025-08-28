package ru.yourok.tinstaller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ru.yourok.tinstaller.content.Link

class PagerLinksFragment : Fragment() {
    var list: List<Link> = emptyList()

    companion object {
        fun newInstance(list: List<Link>): PagerLinksFragment {
            val pageFragment = PagerLinksFragment()
            pageFragment.list = list
            return pageFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.page_layout, null)
        view.findViewById<RecyclerView>(R.id.rvAppList)?.apply {
            setHasFixedSize(true)
            layoutManager = FocusLinearLayoutManager(
                context,
                androidx.recyclerview.widget.GridLayoutManager.VERTICAL,
                false
            )
            adapter = LinksAdapter(list)
        }
        return view
    }

    fun updateList() {
        (view?.findViewById<RecyclerView>(R.id.rvAppList)?.adapter as AppsAdapter?)?.notifyDataSetChanged()
    }
}