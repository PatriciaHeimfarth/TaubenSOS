package de.unihannover.se.tauben2.model.database.entity

import android.graphics.Color
import android.os.Parcelable
import android.widget.SeekBar
import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.unihannover.se.tauben2.App
import de.unihannover.se.tauben2.R
import de.unihannover.se.tauben2.model.database.Injury
import de.unihannover.se.tauben2.model.MapMarkable
import de.unihannover.se.tauben2.model.database.PigeonBreed
import de.unihannover.se.tauben2.view.recycler.RecyclerItem
import kotlinx.android.parcel.Parcelize

/**
 * represents the case of an injured pigeon
 */
@Parcelize
@Entity(tableName = "case"/*,
        foreignKeys = [
            ForeignKey(entity = InjuryEntity::class, parentColumns = ["id"], childColumns = ["injury_id"], onDelete = CASCADE)
        ]*/)
data class Case(@PrimaryKey var caseID: Int?,
                var additionalInfo: String?,
                var isClosed: Boolean?,

                var latitude: Double,
                var longitude: Double,

                var rescuer: String?,
                var reporter: String?,

                var priority: Int,

                var timestamp: Long,
                var phone: String,
                var wasNotFound: Boolean?,
                var wasFoundDead: Boolean?,

                var breed: String?,

                @Embedded
                var injury: Injury?,

                var media: List<String>

) : RecyclerItem, MapMarkable, Parcelable {

    fun getPigeonBreed() = PigeonBreed.fromString(breed)

    fun setPigeonBreed(pigeonBreed: PigeonBreed) {
        breed = PigeonBreed.fromPigeonBreed(pigeonBreed)
    }

    companion object {
        @Ignore
        fun getCleanInstance() = Case(null, null, null, 0.0, 0.0, null, null, -1, -1,
                "", null, null, null, Injury(false, false, false,
                false, false, false, false, false), listOf())
    }

    override fun getMarker(): MarkerOptions = MarkerOptions().position(LatLng(latitude, longitude)).title(App.context.getString(R.string.priority, priority.toString())).snippet(additionalInfo)

    override fun getType() = RecyclerItem.Type.ITEM

    fun setToCurrentTime() {
        timestamp = System.currentTimeMillis() / 1000
    }

    fun getStatusColor(): Int {
        var color = App.getColor(R.color.colorGreen)
        if(wasFoundDead == true)
            color = App.getColor(R.color.colorGray)
        else if (isClosed == false)
            color = if (rescuer != null) App.getColor(R.color.colorYellow) else App.getColor(R.color.colorRed)
        return color
    }

    fun getStatusColorTransparent(): Int {
        var color = App.getColor(R.color.colorGreenTransparent)
        if(wasFoundDead == true)
            color = App.getColor(R.color.colorGrayTransparent)
        else if (isClosed == false)
            color = if (rescuer != null) App.getColor(R.color.colorYellowTransparent) else App.getColor(R.color.colorRedTransparent)
        return color
    }

    fun onPrioritySeekbarValueChanged(seekBar: SeekBar, progressValue: Int, fromUser: Boolean) {
        priority = progressValue + 1
    }


    // Functions needed for two way data binding

//    fun isCarrier() = breed == PigeonBreed.CARRIER

//    fun setCarrier(value: Boolean) {
//        breed = if(value)
//            PigeonBreed.CARRIER
//        else
//            PigeonBreed.NO_SPECIFICATION
//    }
//
//    fun isCommonWood() = breed == PigeonBreed.COMMON_WOOD
//
//    fun isFeral() = breed == PigeonBreed.FERAL
//
//    fun isFancy() = breed == PigeonBreed.FANCY
//
//    fun hasNoSpecification() = breed == PigeonBreed.NO_SPECIFICATION
}
