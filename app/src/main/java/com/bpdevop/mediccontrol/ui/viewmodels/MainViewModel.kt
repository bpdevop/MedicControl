package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.ui.navigation.NavigationItem
import com.bpdevop.mediccontrol.ui.navigation.mainMenuItems
import com.bpdevop.mediccontrol.ui.navigation.secondaryMenuItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _selectedItem = MutableStateFlow(mainMenuItems.firstOrNull())
    val selectedItem: StateFlow<NavigationItem?> = _selectedItem

    fun selectItem(menuItem: NavigationItem) {
        viewModelScope.launch {
            _selectedItem.value = menuItem
        }
    }

    fun getMenuItemByRoute(route: String?): NavigationItem? {
        return mainMenuItems.find { it.route == route }
            ?: secondaryMenuItems.find { it.route == route }
    }
}