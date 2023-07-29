package state

import views.iconviews.AbstractIconEntityView

object SelectedIconManager {
    private var selectedIcon: AbstractIconEntityView? = null

    fun setSelectedIcon(icon: AbstractIconEntityView) {
        selectedIcon?.setSelected(false)
        icon.setSelected(true)
        selectedIcon = icon
    }

    fun deselect() {
        selectedIcon?.setSelected(false)
        selectedIcon = null
    }
}
