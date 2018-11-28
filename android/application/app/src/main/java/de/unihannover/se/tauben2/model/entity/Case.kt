package de.unihannover.se.tauben2.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.unihannover.se.tauben2.model.MapMarkable
import de.unihannover.se.tauben2.view.recycler.RecyclerItem

/**
 * represents the case of an injured pigeon
 */
@Entity(tableName = "case"/*,
        foreignKeys = [
            ForeignKey(entity = InjuryEntity::class, parentColumns = ["id"], childColumns = ["injury_id"], onDelete = CASCADE)
        ]*/)
data class Case(@PrimaryKey var caseID: Int,
                var additionalInfo: String?,

                var isClosed: Boolean,
                var isWeddingPigeon: Boolean,
                var isCarrierPigeon: Boolean,

                var latitude: Double,
                var longitude: Double,

                var rescuer: String?,
                var priority: Int,
                var timestamp: Long,
                var phone: String,

                var wasFoundDead: Boolean?
//                var media: List<String>,
//                @ColumnInfo(name = "injury_id") var injury: Int
) : RecyclerItem, MapMarkable {

    override fun getMarker(): MarkerOptions = MarkerOptions().position(LatLng(latitude, longitude)).title("Priorität: $priority").snippet(additionalInfo)

    override fun getType() = RecyclerItem.Type.ITEM

    fun getSinceString(): String {
        val diff = (System.currentTimeMillis()/1000 - timestamp).toDouble() / 60 //in minutes
        var res = ""
        when {
            diff > 1440 -> return "${(diff/1440).toInt()}  " + if(diff < 2880) "Tag" else "Tagen"
            diff >= 60 -> res = "${(diff/60).toInt()} h"
        }
        return res + " ${ Math.round(diff % 60) } min"
    }
}
