package com.aviapp.app.security.applocker.ui.vault.vaultlist

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.BuildConfig
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaEntity
import com.aviapp.app.security.applocker.databinding.ItemVaultListBinding
import com.aviapp.app.security.applocker.ui.image_viewer.ImageScrActivity
import com.aviapp.app.security.applocker.util.encryptor.CryptoProcess
import kotlinx.coroutines.CoroutineScope








class VaultListAdapter(
    val adHelper: AdHelper,
    val scope: CoroutineScope,
    val viewModel: VaultListViewModel
) : RecyclerView.Adapter<VaultListAdapter.VaultListItemViewHolder>() {

    var vaultMediaEntityClicked: ((VaultListItemViewState) -> Unit)? = null

    private val vaultList = arrayListOf<VaultMediaEntity>()

    fun updateVaultList(vaultList: List<VaultMediaEntity>) {
        this.vaultList.clear()
        this.vaultList.addAll(vaultList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = vaultList.size

    override fun onBindViewHolder(holder: VaultListItemViewHolder, position: Int) = holder.bind(
        vaultList[position]
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaultListItemViewHolder =
        VaultListItemViewHolder.create(parent, vaultMediaEntityClicked, adHelper, scope, viewModel)

    class VaultListItemViewHolder(
        private val binding: ItemVaultListBinding,
        private val vaultMediaEntityClicked: ((VaultListItemViewState) -> Unit)?,
        val adHelper: AdHelper,
        val scope: CoroutineScope,
        private val vaultViewModel: VaultListViewModel
    ) : RecyclerView.ViewHolder(binding.root) {

        init {

            binding.squareLayoutItem.setOnLongClickListener {
                vaultMediaEntityClicked?.invoke(binding.viewState!!)
                return@setOnLongClickListener true
            }
        }

        @SuppressLint("CheckResult")
        fun bind(vaultMediaEntity: VaultMediaEntity) {


            binding.loading.setOnClickListener {

            }

            adHelper.setOnClickListenerWithInter(
                binding.squareLayoutItem,
                scope,
                probability = 3,
                false
            ) {

                if (vaultMediaEntity.mediaType == "type_image") {
                    vaultViewModel.vaultMediaRepository.createPreviewFile1(vaultMediaEntity)
                        .subscribe { data ->
                            itemView.context.startActivity(
                                Intent(
                                    itemView.context,
                                    ImageScrActivity::class.java
                                ).also { it.putExtra("uri", data.decryptedPreviewCachePath) })
                        }
                } else {
                    binding.loading.visibility = View.VISIBLE
                    vaultViewModel.vaultMediaRepository.getMediaFileFromVault(vaultMediaEntity)
                        .subscribe { data ->

                            if (data is CryptoProcess.Processing) {
                                if (binding.loading.visibility != View.VISIBLE) {
                                    binding.loading.visibility = View.VISIBLE
                                }
                            } else {
                                binding.loading.visibility = View.GONE
                            }

                            if (data is CryptoProcess.Complete) {

                                val uri: Uri = FileProvider.getUriForFile(
                                    itemView.context.applicationContext,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    data.file
                                )

                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setDataAndType(uri, "video/*")
                                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                itemView.context.startActivity(intent)
                            }

                        }
                }
            }

            binding.viewState = VaultListItemViewState(vaultMediaEntity)
            binding.executePendingBindings()
        }

        companion object {

            fun create(
                parent: ViewGroup,
                vaultMediaEntityClicked: ((VaultListItemViewState) -> Unit)?,
                adHelper: AdHelper,
                scope: CoroutineScope,
                viewModel: VaultListViewModel
            ): VaultListItemViewHolder {
                val binding: ItemVaultListBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_vault_list,
                    parent,
                    false
                )
                return VaultListItemViewHolder(
                    binding,
                    vaultMediaEntityClicked,
                    adHelper = adHelper,
                    scope,
                    viewModel
                )
            }
        }
    }
}