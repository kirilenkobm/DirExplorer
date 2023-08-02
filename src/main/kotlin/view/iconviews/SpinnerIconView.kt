package view.iconviews

import model.UnknownEntity
import util.IconManager
import util.Utils

class SpinnerIconView(
    entity: UnknownEntity,
): AbstractIconEntityView(entity) {
    // private val linkEntity = entity

    override fun setIcon() {
        textLabel.text = "Loading zip..."
        iconLabel.icon = Utils.resizeIcon(IconManager.loadingSpinner)
    }
}
