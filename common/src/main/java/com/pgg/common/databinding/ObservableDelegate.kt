package com.pgg.common.databinding

import androidx.databinding.BaseObservable
import kotlin.reflect.KProperty

fun <T> ObservableDelegate(id: Int): ObservableDelegate<T?> = ObservableDelegate(id, null)

class ObservableDelegate<T>(
    @JvmField val id: Int,
    @JvmField var value: T,
    private val notifyEqualValues: Boolean = false
) {
    operator fun getValue(ref: BaseObservable, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(ref: BaseObservable, property: KProperty<*>, value: T) {
        if (!notifyEqualValues && this.value == value) {
            return
        }
        this.value = value
        ref.notifyPropertyChanged(id)
    }
}