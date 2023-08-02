package view.iconviews

import model.UnknownEntity
import view.customcomponents.SpinningCircle
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.Insets


class UnpackingZipIconView(
    entity: UnknownEntity,
) : AbstractIconEntityView(entity) {

    private val spinningCircle = SpinningCircle()

    init {
        // Replace the iconLabel with the spinningCircle in the entityPanel
        entityPanel.apply {
            remove(iconLabel)

            val constraints = GridBagConstraints()

            // Constraints for spinningCircle
            constraints.gridx = 0
            constraints.gridy = 0
            constraints.gridwidth = 1
            constraints.gridheight = 1
            constraints.weightx = 1.0
            constraints.weighty = 1.0
            constraints.fill = GridBagConstraints.BOTH
            constraints.insets = Insets(0, 0, 0, 0)
            spinningCircle.preferredSize = Dimension(
                Constants.GRID_COLUMN_WIDTH,
                Constants.GRID_IMAGE_FRAME_HEIGHT
            )
            spinningCircle.maximumSize = spinningCircle.preferredSize
            isOpaque = false
            add(spinningCircle, constraints)
        }
    }

    // do nothing because the spinningCircle is already added to the entityPanel
    override fun setIcon() { }
}
