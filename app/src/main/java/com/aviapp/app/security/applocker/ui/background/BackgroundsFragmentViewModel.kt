package com.aviapp.app.security.applocker.ui.background

import androidx.lifecycle.MutableLiveData
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.aviapp.app.security.applocker.data.database.AppLockerDatabase
import com.aviapp.app.security.applocker.ui.RxAwareViewModel
import javax.inject.Inject

class BackgroundsFragmentViewModel @Inject constructor(val appLockerPreferences: AppLockerPreferences, val database: AppLockerDatabase, ) : RxAwareViewModel() {

    private val backgroundViewStateLiveData = MutableLiveData<List<GradientItemViewState>>()

    init {
        val selectedBackgroundId = appLockerPreferences.getSelectedBackgroundId()
        val gradientBackViewStateList = GradientBackgroundDataProvider.gradientViewStateList
        gradientBackViewStateList.forEach {
            if (it.id == selectedBackgroundId) {
                it.isChecked = true
            }
        }
        backgroundViewStateLiveData.value = gradientBackViewStateList
    }

    fun getBackgroundViewStateLiveData() = backgroundViewStateLiveData

    fun onSelectedItemChanged(selectedItemViewState: GradientItemViewState) {

        val modifiedList = backgroundViewStateLiveData.value
        modifiedList?.forEach { it.isChecked = it.id == selectedItemViewState.id }
        modifiedList?.let {
            backgroundViewStateLiveData.value = it
        }

        appLockerPreferences.setSelectedBackgroundId(selectedItemViewState.id)
    }
}