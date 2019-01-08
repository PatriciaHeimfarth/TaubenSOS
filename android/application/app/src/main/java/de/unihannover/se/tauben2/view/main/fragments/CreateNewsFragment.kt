package de.unihannover.se.tauben2.view.main.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import de.unihannover.se.tauben2.R
import de.unihannover.se.tauben2.databinding.FragmentCreateNewsBinding
import de.unihannover.se.tauben2.getViewModel
import de.unihannover.se.tauben2.model.database.entity.News
import de.unihannover.se.tauben2.setSnackBar
import de.unihannover.se.tauben2.view.Singleton
import de.unihannover.se.tauben2.viewmodel.NewsViewModel
import de.unihannover.se.tauben2.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.fragment_create_news.*
import kotlinx.android.synthetic.main.fragment_create_news.view.*

class CreateNewsFragment : Fragment(){

    companion object: Singleton<CreateNewsFragment>() {
        override fun newInstance() = CreateNewsFragment()
    }

    private lateinit var mBinding: FragmentCreateNewsBinding
    private lateinit var mCreatedNews: News

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_news, container, false)
        val view = mBinding.root
        view.btn_send_news.setOnClickListener {
            sendNewsToServer(view)
            Navigation.findNavController(it.context as Activity, R.id.nav_host).navigate(R.id.newsFragment, CreateNewsFragment.bundle)
        }
        return view
    }

    private fun sendNewsToServer(view : View) {

        getViewModel(NewsViewModel::class.java)?.let {

            val userViewModel = getViewModel(UserViewModel::class.java)

            if(userViewModel?.getOwnerUsername()!=null) {
                mCreatedNews = News(null, userViewModel.getOwnerUsername(), System.currentTimeMillis()/1000, txt_news_body.text.toString(), -1, txt_news_title.text.toString())
                mCreatedNews.setToCurrentTime()
                Log.d("SENT NEWS", "news sent: $mCreatedNews")
                it.sendNews(mCreatedNews)
                setSnackBar(view, "Newspost successfully created", null)
            }
            else{
                setSnackBar(view, "Error: not logged in", null)
            }
        }
    }
}