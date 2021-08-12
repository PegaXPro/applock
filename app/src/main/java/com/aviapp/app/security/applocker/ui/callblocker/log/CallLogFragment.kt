package com.aviapp.app.security.applocker.ui.callblocker.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.FragmentCallBlockerLogsBinding
import com.aviapp.app.security.applocker.ui.BaseFragment
import com.aviapp.app.security.applocker.util.delegate.inflate

class CallLogFragment : BaseFragment<CallLogViewModel>() {

    private val binding: FragmentCallBlockerLogsBinding by inflate(R.layout.fragment_call_blocker_logs)

    private val callLogsAdapter = CallLogAdapter()

    override fun getViewModel(): Class<CallLogViewModel> = CallLogViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding.recyclerViewCallLogs.adapter = callLogsAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getViewStateLiveData().observe(this.viewLifecycleOwner) {
            binding.viewState = it
            callLogsAdapter.setCallLogsItems(it.callLogsViewState)
        }
    }

    companion object {
        fun newInstance() = CallLogFragment()
    }

}