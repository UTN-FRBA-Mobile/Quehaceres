package dadm.frba.utn.edu.ar.quehaceres.fragments

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dadm.frba.utn.edu.ar.quehaceres.R
import dadm.frba.utn.edu.ar.quehaceres.api.Api
import dadm.frba.utn.edu.ar.quehaceres.fragments.AvailableTasksFragment.Listener

class AvailableTasksAdapter(
        private val mValues: List<Api.Task>,
        private val mListener: Listener?)
    : RecyclerView.Adapter<AvailableTasksAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Api.Task
            mListener?.onAvailableTaskClicked(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_availabletasks, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
//        holder.mCoinsView.text = item.coins
        holder.mTaskView.text = item.name

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mCoinsView: TextView = mView.findViewById(R.id.tv_coins)
        val mTaskView: TextView = mView.findViewById(R.id.tv_task)

        override fun toString(): String {
            return super.toString() + " '" + mTaskView.text + "'"
        }
    }
}
