package de.unihannover.se.tauben2.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import de.unihannover.se.tauben2.R
import de.unihannover.se.tauben2.view.main.MainActivity

class SquareImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : ImageView(context, attrs, defStyleAttr, defStyleRes) {

    val thumbView = this
    val imageRes = drawable
    lateinit var startBounds : RectF
    var mScale: Float = 0.0f

    private var mCurrentAnimator: Animator? = null

    private var mShortAnimationDuration: Int = 0

    private var mImageZoomListeners: MutableSet<ImageZoomListener> = mutableSetOf()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) = super.onMeasure(widthMeasureSpec, widthMeasureSpec)

    fun addImageZoomListener(imageZoomListener: ImageZoomListener) {
        mImageZoomListeners.add(imageZoomListener)
    }

    fun addImageZoomListener(onImageZoom: () -> Unit, onImageZoomExit: () -> Unit) {
        mImageZoomListeners.add(object : ImageZoomListener {
            override fun onImageZoomExit() {
                onImageZoomExit()
            }

            override fun onImageZoom() {
                onImageZoom()
            }
        })
    }

    fun zoomImage(expandedImageView: ImageView, mainLayout: FrameLayout, viewGroupContent: ViewGroup, activity: MainActivity) {
        this.setOnClickListener {
            if(resources.getDrawable(R.drawable.ic_logo, null) == this.drawable)
                return@setOnClickListener
            activity.zoomMode = true
            zoomImageFromThumb(expandedImageView, mainLayout, viewGroupContent, activity)
        }
        mShortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
    }

    private fun zoomImageFromThumb(expandedImageView: ImageView, mainLayout: FrameLayout, viewGroupContent: ViewGroup, activity: MainActivity) {
        mCurrentAnimator?.cancel()

        expandedImageView.setImageDrawable(imageRes)

        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        thumbView.getGlobalVisibleRect(startBoundsInt)
        mainLayout.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2f
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }
        mScale = startScale
        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f
        expandedImageView.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.pivotX = 0f
        expandedImageView.pivotY = 0f

        viewGroupContent.visibility = View.GONE


        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        mCurrentAnimator = AnimatorSet().apply {
            play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left, finalBounds.left)).apply {
                with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f))
            }
            duration = mShortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    mCurrentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    mCurrentAnimator = null
                }
            })
            mImageZoomListeners.forEach { it.onImageZoom() }
            start()
        }



        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        expandedImageView.setOnClickListener {
            mCurrentAnimator?.cancel()

            viewGroupContent.visibility = View.VISIBLE

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            mCurrentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale))
                }
                duration = mShortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.GONE
                        mCurrentAnimator = null
                        mImageZoomListeners.forEach { listener -> listener.onImageZoomExit() }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.GONE
                        mCurrentAnimator = null
                        mImageZoomListeners.forEach { listener -> listener.onImageZoomExit() }
                    }
                })
                start()
                activity.zoomMode = false
            }
        }
    }

    fun zoomOut(expandedImageView: ImageView, viewGroupContent: ViewGroup, activity: MainActivity) {
        mCurrentAnimator?.cancel()

        viewGroupContent.visibility = View.VISIBLE

        mCurrentAnimator = AnimatorSet().apply {
            play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left)).apply {
                with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, mScale))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, mScale))
            }
            duration = mShortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    thumbView.alpha = 1f
                    expandedImageView.visibility = View.GONE
                    mCurrentAnimator = null
                    mImageZoomListeners.forEach { listener -> listener.onImageZoomExit() }
                }

                override fun onAnimationCancel(animation: Animator) {
                    thumbView.alpha = 1f
                    expandedImageView.visibility = View.GONE
                    mCurrentAnimator = null
                    mImageZoomListeners.forEach { listener -> listener.onImageZoomExit() }
                }
            })
            start()
            activity.zoomMode = false
        }
    }

    interface ImageZoomListener {
        fun onImageZoom()
        fun onImageZoomExit()
    }
}