package com.aviapp.app.security.applocker.ui.background

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.FragmentBackgroundsBinding
import com.aviapp.app.security.applocker.ui.BaseFragment
import com.aviapp.app.security.applocker.ui.background.analytics.BackgroundAnalytics
import com.aviapp.app.security.applocker.util.delegate.inflate
import kotlinx.coroutines.launch

class BackgroundsFragment : BaseFragment<BackgroundsFragmentViewModel>() {

    private val binding: FragmentBackgroundsBinding by inflate(R.layout.fragment_backgrounds)
    private lateinit var backgroundsAdapter: BackgroundsAdapter

    override fun getViewModel(): Class<BackgroundsFragmentViewModel> = BackgroundsFragmentViewModel::class.java
    lateinit var adHandler: AdHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adHandler = (requireActivity() as BackgroundsActivity).adHelper
        backgroundsAdapter = BackgroundsAdapter(viewModel.database, lifecycleScope, adHandler, requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding.recyclerViewBackgrounds.adapter = backgroundsAdapter
        backgroundsAdapter.onItemSelected = { onBackgroundItemSelected(it) }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getBackgroundViewStateLiveData().observe(this.viewLifecycleOwner) {
            backgroundsAdapter.setViewStateList(it)
        }
    }


    private fun onBackgroundItemSelected(selectedItemViewState: GradientItemViewState) {

        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_look_theme)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT)
        val installButton = dialog.findViewById<Button>(R.id.installThemeButton)
        val cancelButton = dialog.findViewById<TextView>(R.id.cancelThemeButton)
        dialog.window?.setBackgroundDrawable(selectedItemViewState.getGradiendDrawable(requireActivity()))

        installButton.setOnClickListener {
            lifecycleScope.launch {
                adHandler.showInterstitial(requireActivity(), 3) {
                    viewModel.onSelectedItemChanged(selectedItemViewState)
                    activity?.let { BackgroundAnalytics.sendBackgroundChangedEvent(it, selectedItemViewState.id) }
                }
            }
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()


        /*adHandler.requestReward(requireActivity(), 3) {
            if (it) {
                viewModel.onSelectedItemChanged(selectedItemViewState)
                activity?.let { BackgroundAnalytics.sendBackgroundChangedEvent(it, selectedItemViewState.id) }
            } else {
                viewModel.onSelectedItemChanged(selectedItemViewState)
                activity?.let { BackgroundAnalytics.sendBackgroundChangedEvent(it, selectedItemViewState.id) }
            }
        }*/

    }

    companion object {

        fun newInstance(): Fragment = BackgroundsFragment()
    }
}