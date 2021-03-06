package de.unihannover.se.tauben2.model.database.entity

import android.os.Parcelable
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.DrawableRes
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Callback
import de.unihannover.se.tauben2.App
import de.unihannover.se.tauben2.R
import de.unihannover.se.tauben2.loadMedia
import de.unihannover.se.tauben2.model.MapMarkable
import de.unihannover.se.tauben2.model.database.Injury
import de.unihannover.se.tauben2.model.database.PigeonBreed
import de.unihannover.se.tauben2.model.database.Media
import de.unihannover.se.tauben2.view.recycler.RecyclerItem
import kotlinx.android.parcel.Parcelize
import java.lang.Exception

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

                var media: List<Media>

) : RecyclerItem, MapMarkable, Parcelable, DatabaseEntity() {

    override val refreshCooldown: Long
        get() = 0//900000 * 2 // 30 min


    fun getMediaUploadURL() = App.getBaseURL() + "case/$caseID/media"

    fun getMediaURL(mediaId: Int) = mediaId.let { getMediaUploadURL() + "/$mediaId" }

    fun getImageURL(media: Media?) = media?.let {
            if(media.getType().isVideo())
                getMediaURL(it.mediaID) + "/thumbnail"
            else
                getMediaURL(it.mediaID)
        }

    fun getMediaURLs(): List<String> {
        val result = mutableListOf<String>()
        media.forEach { getMediaURL(it.mediaID).let { url -> result.add(url) } }
        return result
    }

    fun loadMediaFromServerInto(media: Media?, imageView: ImageView, @DrawableRes placeHolder: Int? = R.drawable.ic_logo, fit: Boolean = true, callback: Callback? = null) {
        loadMedia(media?.let { getImageURL(it) }, placeHolder, imageView, fit, callback)
    }

    fun getPigeonBreed() = PigeonBreed.fromString(breed)

    fun setPigeonBreed(pigeonBreed: PigeonBreed) {
        breed = PigeonBreed.fromPigeonBreed(pigeonBreed)
    }

    companion object: AllUpdatable {

        override val refreshAllCooldown: Long
            get() = 900000 // 15 min

        @Ignore
        fun getCleanInstance() = Case(null, null, null, 0.0, 0.0, null, null, 1, -1,
                "", null, null, null, Injury(false, false, false,
                false, false, false, false, false), listOf())

    }

    override fun getMarker(): MarkerOptions {
        var bmD : BitmapDescriptor? = null
        if(isClosed == false){
            bmD = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
        if(isClosed == true){
            bmD = BitmapDescriptorFactory.defaultMarker(144.9f)
        }
        if(rescuer != null && isClosed == false){
            bmD = BitmapDescriptorFactory.defaultMarker(55.06f)
        }
        if(wasFoundDead == true || wasNotFound == true){
            bmD = BitmapDescriptorFactory.defaultMarker(220.0f)
        }
        return MarkerOptions().position(LatLng(latitude, longitude)).title(App.context.getString(R.string.priority, priority.toString())).snippet(additionalInfo).icon(bmD)
    }

    override fun getType() = RecyclerItem.Type.ITEM

    fun setToCurrentTime() {
        timestamp = System.currentTimeMillis() / 1000
    }

    fun getStatusColor(): Int {

        var color = App.getColor(R.color.colorGreen)

        if (wasFoundDead == true || wasNotFound == true)
            color = App.getColor(R.color.colorBlue)
        else if (isClosed == false)
            color = if (rescuer != null) App.getColor(R.color.colorYellow) else App.getColor(R.color.colorRed)
        return color
    }

    fun getStatusColorTransparent(): Int {

        var color = App.getColor(R.color.colorGreenTransparent)

        if(wasFoundDead == true || wasNotFound == true)
            color = App.getColor(R.color.colorBlueTransparent)
        else if (isClosed == false)
            color = if (rescuer != null) App.getColor(R.color.colorYellowTransparent) else App.getColor(R.color.colorRedTransparent)
        return color
    }

    fun onPrioritySeekbarValueChanged(seekBar: SeekBar, progressValue: Int, fromUser: Boolean) {
        priority = progressValue + 1
    }

    fun nextState(user: User) {
        if(rescuer == null)
            rescuer = user.username
        else
            isClosed = true
    }

    fun previousState(user: User) {
        if(isClosed == true)
             isClosed = false
        else
            rescuer = user.username

    }

    fun getAdditionalInfoString() = if(additionalInfo.isNullOrEmpty()) "-" else additionalInfo



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
