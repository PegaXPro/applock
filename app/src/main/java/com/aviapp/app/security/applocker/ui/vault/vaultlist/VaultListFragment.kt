package com.aviapp.app.security.applocker.ui.vault.vaultlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType
import com.aviapp.app.security.applocker.databinding.FragmentVaultListBinding
import com.aviapp.app.security.applocker.ui.BaseFragment
import com.aviapp.app.security.applocker.ui.vault.removalconfirmationdialog.RemovalConfirmationDialog
import com.aviapp.app.security.applocker.util.delegate.inflate
import kotlinx.coroutines.launch
import org.koin.android.scope.compat.ScopeCompat.lifecycleScope

class VaultListFragment : BaseFragment<VaultListViewModel>() {

    private val binding: FragmentVaultListBinding by inflate(R.layout.fragment_vault_list)
    private lateinit var vaultListAdapter: VaultListAdapter

    override fun getViewModel(): Class<VaultListViewModel> = VaultListViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mediaType = arguments?.getSerializable(KEY_MEDIA_TYPE) as VaultMediaType
        viewModel.setVaultMediaType(mediaType)
        vaultListAdapter = VaultListAdapter(adHelper, lifecycleScope, viewModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.recyclerViewVaultList.adapter = vaultListAdapter
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getVaultListViewStateLiveData().observe(this.viewLifecycleOwner) {
            binding.viewState = it
            binding.executePendingBindings()
            vaultListAdapter.updateVaultList(it.vaultList)
        }

        vaultListAdapter.vaultMediaEntityClicked = this@VaultListFragment::onVaultMediaEntityClicked
    }

    private fun onVaultMediaEntityClicked(selectedVaultMediaEntity: VaultListItemViewState) {

        activity?.let {
            lifecycleScope.launch {
                adHelper.showInterstitial(requireActivity(), 3) {
                    RemovalConfirmationDialog.newInstance(selectedVaultMediaEntity.vaultMediaEntity)
                        .show(it.supportFragmentManager, "")
                }
            }
        }
    }

    companion object {

        private const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE"
        fun newInstance(vaultMediaType: VaultMediaType): Fragment {
            return VaultListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_MEDIA_TYPE, vaultMediaType)
                }
            }
        }
    }

}