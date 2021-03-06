package de.unihannover.se.tauben2.view.report

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import de.unihannover.se.tauben2.App
import de.unihannover.se.tauben2.R
import de.unihannover.se.tauben2.databinding.FragmentReportBreedBinding
import de.unihannover.se.tauben2.getDpValue
import de.unihannover.se.tauben2.model.database.PigeonBreed
import de.unihannover.se.tauben2.model.database.entity.Case
import kotlinx.android.synthetic.main.fragment_report_breed.*
import kotlinx.android.synthetic.main.fragment_report_breed.view.*


class BreedReportFragment : ReportFragment() {

    private val layoutId = R.layout.fragment_report_breed
    private lateinit var mBinding: FragmentReportBreedBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        mBinding.createdCase = mCreatedCase

        setBtnListener(R.id.fragment_report_comment, R.id.fragment_report_priority)

        mBinding.root.show_examples.setOnClickListener {
            //Pop up for more info
            val alertDialogBuilder = AlertDialog.Builder(context)

            alertDialogBuilder.setTitle(R.string.kind_of_pigeon)

            alertDialogBuilder.setMessage(R.string.breed_report_info)

            val alertDialog = alertDialogBuilder.create()

            alertDialog.show()
        }

        PigeonBreed.values().forEach { createRadioButton(it) }

        return mBinding.root
    }

    private fun createRadioButton(breed: PigeonBreed) {
        val color = App.context.resources.getColor(R.color.colorPrimaryDark)
        val csList = ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled)),
                intArrayOf(color, color)
        )
        val button = MaterialRadioButton(context).apply {
            id = breed.ordinal
            text = context.getText(breed.titleResource)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            buttonTintList = csList
            isChecked = breed == mCreatedCase?.getPigeonBreed()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setTextAppearance(R.style.TextAppearance_MaterialComponents_Body1)
            }
            setOnClickListener {
                if(this.isChecked)
                    mCreatedCase.setPigeonBreed(breed)
            }
        }
        mBinding.root.radio_group.addView(button)
        if(button.layoutParams is ViewGroup.MarginLayoutParams)
            (button.layoutParams as ViewGroup.MarginLayoutParams).topMargin = getDpValue(8)


    }

}