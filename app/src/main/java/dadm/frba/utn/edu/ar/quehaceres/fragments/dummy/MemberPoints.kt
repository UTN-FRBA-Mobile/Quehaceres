package dadm.frba.utn.edu.ar.quehaceres.fragments.dummy


import android.content.res.Resources
import dadm.frba.utn.edu.ar.quehaceres.R
import java.util.ArrayList
import java.util.HashMap



object MemberPoints {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<MemberPointsItem> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, MemberPointsItem> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }


    private fun addItem(item: MemberPointsItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createDummyItem(position: Int): MemberPointsItem {
        return MemberPointsItem(position.toString(), "Item " + position, makeDetails(position))
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A Member-Points representing a piece of content.
     */
    data class MemberPointsItem(val id: String, val name: String, val weeklyPoints: String) {
        override fun toString(): String {
            return name + Resources.getSystem().getString(R.string.member_points_qty) + weeklyPoints + Resources.getSystem().getString(R.string.points)
        }
    }

}