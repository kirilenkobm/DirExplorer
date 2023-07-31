package state

import view.iconviews.AbstractIconEntityView

/**
 * Singleton object responsible for managing the selection state of icons in the grid display view.
 *
 * This object keeps track of the currently selected icon, ensuring that at any given time,
 * only one icon (or none) is selected.When a new icon is selected, the previously selected icon (if any)
 * is automatically deselected, ensuring that only one icon is selected at a time.
 **/
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
