package com.aviapp.app.security.applocker.util.delegate

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import android.view.ViewGroup
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Inflate<in R : Fragment, out T : ViewDataBinding>(@LayoutRes private val layoutRes: Int) : ReadOnlyProperty<R, T> {

    private var binding: T? = null

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        if (binding == null) {
            val inflater = thisRef.layoutInflater
            val container = thisRef.view as ViewGroup?
            binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        }
        return binding!!
    }
}

fun <R : Fragment, T : ViewDataBinding> inflate(@LayoutRes layoutRes: Int): Inflate<R, T> {
    return Inflate(layoutRes)
}