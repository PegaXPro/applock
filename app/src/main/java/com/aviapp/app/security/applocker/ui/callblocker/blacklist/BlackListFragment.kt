package com.aviapp.app.security.applocker.ui.callblocker.blacklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.callblocker.blacklist.BlackListItemEntity
import com.aviapp.app.security.applocker.databinding.FragmentCallBlockerBlacklistBinding
import com.aviapp.app.security.applocker.ui.BaseFragment
import com.aviapp.app.security.applocker.ui.callblocker.blacklist.delete.BlackListItemDeleteDialog
import com.aviapp.app.security.applocker.util.delegate.inflate

class BlackListFragment : BaseFragment<BlackListViewModel>() {

    private val binding: FragmentCallBlockerBlacklistBinding by inflate(R.layout.fragment_call_blocker_blacklist)

    private val blackListAdapter = BlackListAdapter()

    override fun getViewModel(): Class<BlackListViewModel> = BlackListViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.recyclerViewBlackList.adapter = blackListAdapter
        blackListAdapter.onItemClicked = this@BlackListFragment::showDeleteDialog
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getViewStateLiveData().observe(this.viewLifecycleOwner) {
            binding.viewState = it
            blackListAdapter.setBlackListItems(it.blackListViewStateList)
        }
    }


    private fun showDeleteDialog(blackListItem: BlackListItemEntity) {
        activity?.let {
            BlackListItemDeleteDialog.newInstance(blackListItem.blacklistId)
                .show(it.supportFragmentManager, "")
        }
    }

    companion object {
        fun newInstance() = BlackListFragment()
    }

}