package de.unihannover.se.tauben2.view.main.fragments.cases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.leinardi.android.speeddial.SpeedDialActionItem
import de.unihannover.se.tauben2.*
import de.unihannover.se.tauben2.model.database.entity.Case
import de.unihannover.se.tauben2.view.LoadingObserver
import de.unihannover.se.tauben2.view.Singleton
import de.unihannover.se.tauben2.view.main.fragments.MapViewFragment
import de.unihannover.se.tauben2.view.recycler.CasesRecyclerFragment
import de.unihannover.se.tauben2.viewmodel.CaseViewModel
import de.unihannover.se.tauben2.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.fragment_cases.*
import kotlinx.android.synthetic.main.fragment_cases.view.*

class CasesAdminFragment : CasesFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = super.onCreateView(inflater, container, savedInstanceState)

        activity?.title = view?.context?.getString(R.string.all_cases)
        loadCases(Filter.ALL)

        view?.let {
            initFab(it, recyclerFragment, mapsFragment)
        }

        return view
    }

    // Filter - Floating Action Button
    private fun initFab (view : View, recyclerFragment : CasesRecyclerFragment, mapsFragment : MapViewFragment) {

        view.dialOverlay.visibility = View.VISIBLE

//        view.speedDial.

        view.speedDial.apply {

            visibility = View.VISIBLE

            // Add Filter Buttons
            // my cases
            addActionItem(
                    SpeedDialActionItem.Builder(R.id.cases_filter_my, R.drawable.ic_assignment_ind_white_24dp)
                            .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.White, null))
                            // TODO: y u no work...
//                             .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.White, null))
                            .setLabel(getString(R.string.my_cases))
                            .create()
            )
            // closed cases
            addActionItem(
                    SpeedDialActionItem.Builder(R.id.cases_filter_closed, R.drawable.ic_assignment_turned_in_white_24dp)
                            .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.White, null))
                            .setLabel(getString(R.string.closed_cases))
                            .create()
            )
            // open cases
            addActionItem(
                    SpeedDialActionItem.Builder(R.id.cases_filter_open, R.drawable.ic_assignment_late_white_24dp)
                            .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.White, null))
                            .setLabel(getString(R.string.open_cases))
                            .create()
            )
            // all cases
            addActionItem(
                    SpeedDialActionItem.Builder(R.id.cases_filter_all, R.drawable.ic_assignment_white_24dp)
                            .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.White, null))
                            .setLabel(getString(R.string.all_cases))
                            .create()
            )

            // onClickListener
            setOnActionSelectedListener { speedDialActionItem ->
                when (speedDialActionItem.id) {
                    R.id.cases_filter_my -> {
                        activity?.title = context.getString(R.string.my_cases)
                        loadCases(Filter.MY)
                        false
                    }
                    R.id.cases_filter_closed -> {
                        activity?.title = context.getString(R.string.closed_cases)
                        loadCases(Filter.CLOSED)
                        false
                    }
                    R.id.cases_filter_open -> {
                        activity?.title = context.getString(R.string.open_cases)
                        loadCases(Filter.OPEN)
                        false
                    }
                    R.id.cases_filter_all -> {
                        activity?.title = context.getString(R.string.all_cases)
                        loadCases(Filter.ALL)
                        false // true to keep the Speed Dial open
                    }
                    else -> false
                }
            }
        }
    }
}