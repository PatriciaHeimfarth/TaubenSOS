package de.unihannover.se.tauben2.view.report

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.FileProvider
import com.squareup.picasso.Picasso
import de.unihannover.se.tauben2.R
import de.unihannover.se.tauben2.model.PicassoVideoRequestHandler
import de.unihannover.se.tauben2.setSnackBar
import de.unihannover.se.tauben2.view.SquareImageView
import kotlinx.android.synthetic.main.activity_report.*
import kotlinx.android.synthetic.main.fragment_report_media.view.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MediaReportFragment : ReportFragment() {

    private lateinit var v: View

    private lateinit var picassoInstance: Picasso

    init {
        pagePos = PagePos.FIRST
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 0
        private const val REQUEST_VIDEO_CAPTURE = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        v = inflater.inflate(R.layout.fragment_report_media, container, false)

        context?.let {
            picassoInstance = Picasso.Builder(it.applicationContext)
                    .addRequestHandler(PicassoVideoRequestHandler()).build()
        }

        setBtnListener(R.id.fragment_report_location, null)

        (activity as ReportActivity).prev_btn.setOnClickListener {
            (activity as ReportActivity).finish()
        }

        val alertBuilder = AlertDialog.Builder(context).apply {
            setTitle(getString(R.string.what_kind_of_media))
            setItems(arrayOf(getString(R.string.take_photo), getString(R.string.record_video))){ _, i ->
                if(mCreatedCase.media.size > 3) {
                    setSnackBar(v, getString(R.string.maximum_reached))
                    return@setItems
                }
                when(i){
                    0 -> dispatchTakeMediaIntent()
                    1 -> dispatchTakeMediaIntent(true)
                }
            }
        }

        v.report_media_add_button.setOnClickListener {
            alertBuilder.show()
        }

        createBlankImages(v)

        loadMedia()

        return v
    }

    private fun dispatchTakeMediaIntent(isVideo: Boolean = false) {

        val intentAction = if(isVideo) MediaStore.ACTION_VIDEO_CAPTURE else MediaStore.ACTION_IMAGE_CAPTURE
        val filePrefix = if(isVideo) "VIDEO" else "JPEG"
        val fileSuffix = if(isVideo) ".mp4" else ".jpg"
        val requestCode = if(isVideo) REQUEST_VIDEO_CAPTURE else REQUEST_IMAGE_CAPTURE

        Intent(intentAction).also { takeMediaIntent ->
            // Ensure that there's a camera activity to handle the intent
            activity?.packageManager?.let {
                takeMediaIntent.resolveActivity(it)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createMediaFile(filePrefix, fileSuffix)
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also { file ->
                        val photoURI: Uri? = context?.let { context ->
                            FileProvider.getUriForFile(
                                    context,
                                    "de.unihannover.se.tauben2.fileprovider",
                                    file
                            )
                        }
                        takeMediaIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takeMediaIntent, requestCode)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createMediaFile(prefix: String, suffix: String): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMAN).format(Date())
        val storageDir: File? = context?.filesDir
        return File.createTempFile(
                "${prefix}_${timeStamp}_", /* prefix */
                suffix, /* suffix */
                storageDir /* directory */
        ).apply {
            mCreatedCase.media += absolutePath.getFileName()
        }
    }

    // triggered after capturing a photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if(resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    compressImage(mCreatedCase.media.last(), 80, 1000, 1000)
                }
            }
            loadMedia()
        }
    }

    private fun loadMedia() {

        for (i in 0 until v.image_layout.childCount) {

            val layout = (v.image_layout.getChildAt(i) as ConstraintLayout)
            val image = layout.getChildAt(0) as ImageView

            layout.visibility = View.INVISIBLE

            if (image is SquareImageView && i < mCreatedCase.media.size) {

                val mediaLink = if (URLUtil.isValidUrl(mCreatedCase.media[i])) mCreatedCase.media[i]
                else context?.getFileStreamPath(mCreatedCase.media[i])?.absolutePath

                val suffix = mediaLink?.split(".")?.last()
                if(suffix != "jpg") {
//                    MediaMetadataRetriever().apply {
//                        setDataSource(mediaLink, hashMapOf<String, String>())
//                    }
                    picassoInstance.load(PicassoVideoRequestHandler.SCHEME_VIDEO + ":" + mediaLink)?.into(image)
                } else {
                    if (URLUtil.isValidUrl(mediaLink)) Picasso.get().load(mediaLink).into(image)
                    else Picasso.get().load(File(mediaLink)).into(image)
                }


                layout.visibility = View.VISIBLE
            }
        }
    }

    private fun deleteImage(image: SquareImageView) {
        for (i in 0 until v.image_layout.childCount) {
            if ((v.image_layout.getChildAt(i) as ConstraintLayout).getChildAt(0) == image) {
                // TODO remove this and do it properly
                if (!URLUtil.isValidUrl(mCreatedCase.media[i]))
                    (mCreatedCase.media as MutableList<String>).removeAt(i)
            }
        }
        loadMedia()
    }

    private fun createBlankImages(view: View) {

        //  LinearLayout [
        //      3x ConstraintLayout [
        //          Image
        //          Button
        //      ]
        //  ]


        (0..2).forEach {

            val image = SquareImageView(view.context).apply {
                id = View.generateViewId()
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            val button = ImageButton(view.context).apply {
                id = View.generateViewId()
                setImageResource(R.drawable.ic_close)
//                setPadding(0,0,0,0)
                background = ColorDrawable(Color.TRANSPARENT)
                setOnClickListener {
                    deleteImage(image)
                }
            }

            val constraintLayout = ConstraintLayout(view.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.0f).apply {

                    setMargins(2,2,2,2)
                }
                addView(image)
                addView(button)
                visibility = View.INVISIBLE
            }

            view.image_layout.addView(constraintLayout)

            ConstraintSet().apply {
                clone(constraintLayout)
                connect(button.id, ConstraintSet.TOP, image.id, ConstraintSet.TOP)
                connect(button.id, ConstraintSet.END, image.id, ConstraintSet.END)
                applyTo(constraintLayout)
            }

        }
    }

    /**
     * Helper function for extracting the filename to a given filepath
     **/
    private fun String.getFileName(): String {
        return this.substringAfterLast("/")
    }

    /**
     * Compress and resize a given image
     * @param fileName Name of the file for locating
     * @param quality 0-100 where 0 means maximum compression
     * @param width new width of the image
     * @param height new height of the image
     */
    private fun compressImage(fileName: String, quality: Int, width: Int, height: Int) {
        val rawImage = context?.getFileStreamPath(fileName)?.absolutePath

        val resized = BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(rawImage, this)

            inSampleSize = calculateInSampleSize(this, width, height)
            inJustDecodeBounds = false

            BitmapFactory.decodeFile(rawImage, this)
        }

        val fileOutStream = context?.openFileOutput(fileName, Context.MODE_PRIVATE)
        resized.compress(Bitmap.CompressFormat.JPEG, quality, fileOutStream)
    }

    /**
     * calculate in sample size parameter for loading a scaled bitmap into memory
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}