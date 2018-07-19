package org.wordpress.android.viewmodel.pages

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.wordpress.android.R.string
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.ui.pages.PageItem
import org.wordpress.android.ui.pages.PageItem.Action
import org.wordpress.android.ui.pages.PageItem.Empty
import javax.inject.Inject

class PageParentViewModel
@Inject constructor(val dispatcher: Dispatcher) : ViewModel() {
    private val mutableData: MutableLiveData<List<PageItem>> = MutableLiveData()
    val data: LiveData<List<PageItem>> = mutableData

    private var isStarted: Boolean = false
    private var site: SiteModel? = null

    fun start(site: SiteModel, key: String) {
        this.site = site
        if (!isStarted) {
            mutableData.postValue(listOf(Empty(string.empty_list_default)))
            isStarted = true
        }
        val listOf = mockResult(key)
        mutableData.postValue(listOf)
    }

    fun stop() {
        this.site = null
    }

    fun onAction(action: Action, pageItem: PageItem): Boolean {
        TODO("not implemented")
    }
}