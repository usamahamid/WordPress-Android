package org.wordpress.android.models.networkresource

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import kotlin.reflect.KProperty

class ListStateLiveDataDelegate<T: Any>(
        initialState: ListNetworkResource<T> = ListNetworkResource.Init(),
        private val liveData: MutableLiveData< ListNetworkResource<T>> =
                MutableLiveData()) {

    init {
        liveData.value = initialState
    }

    fun observe(owner: LifecycleOwner, observer: Observer<ListNetworkResource<T>>) =
            liveData.observe(owner, observer)

    operator fun setValue(ref: Any, p: KProperty<*>, value: ListNetworkResource<T>) {
        liveData.postValue(value)
    }

    operator fun getValue(ref: Any, p: KProperty<*>): ListNetworkResource<T> =
            liveData.value!!
}
