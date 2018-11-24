package de.unihannover.se.tauben2.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.unihannover.se.tauben2.R
import de.unihannover.se.tauben2.model.entity.Case
import de.unihannover.se.tauben2.model.network.Resource
import kotlinx.android.synthetic.main.card_case.view.*

@Deprecated("Replaced by CasesRecyclerFragment")
class AdapterList : RecyclerView.Adapter<AdapterList.ViewHolder>(), Observer<Resource<List<Case>>> {

    override fun onChanged(t: Resource<List<Case>>?) {

    }

    private var mExpandedPosition = -1
    // private val recyclerView: RecyclerView? = null

    private val titles = arrayOf("Chapter One",
            "Chapter Two", "Chapter Three", "Chapter Four",
            "Chapter Five", "Chapter Six", "Chapter Seven",
            "Chapter Eight")

    private val details = arrayOf("Item one details", "Item two details",
            "Item three details", "Item four details",
            "Item file details", "Item six details",
            "Item seven details", "Item eight details")

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //var itemTitle: TextView = itemView.findViewById(R.id.card_title)
        //var itemDetail: TextView = itemView.findViewById(R.id.card_subtitle)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.card_case, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        //viewHolder.itemTitle.text = titles[i]
        //viewHolder.itemDetail.text = details[i]
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        //super.onBindViewHolder(holder, position, payloads)

        val isExpanded = position == mExpandedPosition

        holder.itemView.expand_card.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.itemView.isActivated = isExpanded
        holder.itemView.setOnClickListener {
            mExpandedPosition = if (isExpanded) -1 else position
            // TransitionManager.beginDelayedTransition(recyclerView);
            notifyDataSetChanged()
        }

    }
}