package com.juanjoseabuin.ualacitymobilechallenge.presentation.utils

import androidx.lifecycle.ViewModel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object ViewModelDelegate {
    inline fun <reified T : ViewModel> viewModel(
        crossinline factory: () -> T
    ): ReadOnlyProperty<Any?, T> {
        return object : ReadOnlyProperty<Any?, T> {
            private var viewModel: T? = null

            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return viewModel ?: factory().also { viewModel = it }
            }
        }
    }
}

