package com.aviapp.app.security.applocker.ui.rateus

import com.aviapp.app.security.applocker.repository.UserPreferencesRepository
import com.aviapp.app.security.applocker.ui.RxAwareViewModel
import javax.inject.Inject

class RateUsViewModel @Inject constructor(val userPreferencesRepository: UserPreferencesRepository) :
    RxAwareViewModel() {

    fun setUserRateUs() = userPreferencesRepository.setUserRateUs()
}