package de.unihannover.se.tauben2.view.report

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import de.unihannover.se.tauben2.R
import de.unihannover.se.tauben2.getViewModel
import de.unihannover.se.tauben2.model.entity.Case
import de.unihannover.se.tauben2.view.Singleton
import de.unihannover.se.tauben2.viewmodel.CaseViewModel
import kotlinx.android.synthetic.main.fragment_report02.view.*


class Report02Fragment : Fragment() {

    private val LOG_TAG = this::class.java.simpleName

    private var mCreatedCase: Case? = null

    companion object : Singleton<Report02Fragment>() {
        override fun newInstance() = Report02Fragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        mCreatedCase = args?.getParcelable("createdCase")
        Log.d(LOG_TAG, "Current Case object: ${mCreatedCase.toString()}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_report02, container, false)

        // this will reset the frame - no gud. change plox
        view.report_prev_step_button.setOnClickListener {
            Navigation.findNavController(context as Activity, R.id.nav_host).navigateUp()
        }

        view.report_send_button.setOnClickListener {
            sendCaseToServer()
            Report00Fragment.removeInstance()
            Report01Fragment.removeInstance()
            Report02Fragment.removeInstance()
        }

        return view
    }

    private fun sendCaseToServer() {
        val caseViewModel = getViewModel(CaseViewModel::class.java)
        // TODO uncomment when done with testing
        //caseViewModel?.sendCase(myNewCase)
    }
}