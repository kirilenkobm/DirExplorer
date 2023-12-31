package view.iconviews

import Constants
import model.*
import service.EntityActionsHandler
import state.ColorTheme
import state.SelectedIconManager
import state.Settings
import util.Utils
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * Abstract base class for creating icon views for file system entities.
 *
 * This class provides a template for creating icon views that represent
 * file system entities in a vertical stack layout.
 *
 * The icon view consists of an icon image centered within a frame, and a text label below the icon.
 *
 * The icon view supports selection behavior, where the background color changes when the view is selected.
 */
abstract class AbstractIconEntityView(
    private val entity: FileSystemEntity,
) {
    // The icon view must look like a vertical stack
    // Top element - frame of the same size, regardless on the icon size
    // icon is centered in this frame
    // Text -> the same size too, but having lesser height
    // text aligned to top by y-axis, to center by x-axis
    val iconLabel = JLabel().apply {
        horizontalAlignment = SwingConstants.CENTER
        alignmentX = Component.CENTER_ALIGNMENT
    }

    val textLabel = JLabel().apply {
        horizontalAlignment = SwingConstants.CENTER
        alignmentX = Component.CENTER_ALIGNMENT
    }

    private val textPanel = JPanel(BorderLayout()).apply {
        preferredSize = Dimension(
            Constants.GRID_COLUMN_WIDTH,
            Constants.GRID_TEXT_FRAME_HEIGHT
        )
        maximumSize = preferredSize
        isOpaque = false
        add(textLabel, BorderLayout.NORTH)
    }

    val entityPanel = JPanel(GridBagLayout()).apply {
        val constraints = GridBagConstraints()

        // Constraints for iconLabel
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridwidth = 1
        constraints.gridheight = 1
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        constraints.fill = GridBagConstraints.BOTH
        constraints.insets = Insets(0, 0, 0, 0)
        iconLabel.preferredSize = Dimension(
            Constants.GRID_COLUMN_WIDTH,
            Constants.GRID_IMAGE_FRAME_HEIGHT
        )
        iconLabel.maximumSize = iconLabel.preferredSize
        add(iconLabel, constraints)

        // Constraints for textPanel
        constraints.gridy = 1
        add(textPanel, constraints)

        preferredSize = Dimension(
            Constants.GRID_COLUMN_WIDTH,
            Constants.GRID_ROW_HEIGHT
        )
        maximumSize = preferredSize
        isOpaque = false
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                when (e.clickCount) {
                    1 -> SelectedIconManager.setSelectedIcon(this@AbstractIconEntityView)
                    2 -> {
                        SelectedIconManager.setSelectedIcon(this@AbstractIconEntityView)
                        EntityActionsHandler.performEntityAction(entity)
                    }
                }
            }
        })
    }

    private val wrapperPanel = JPanel(GridBagLayout()).apply {
        val gbc = GridBagConstraints().apply {
            gridwidth = GridBagConstraints.REMAINDER
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            weighty = 1.0
        }
        add(entityPanel, gbc)
        preferredSize = Dimension(Constants.GRID_COLUMN_WIDTH, Constants.GRID_ROW_HEIGHT)
        maximumSize = Dimension(Constants.GRID_COLUMN_WIDTH, Constants.GRID_ROW_HEIGHT)
    }

    protected abstract fun setIcon()

    internal open fun createView(): JPanel {
        textLabel.text = Utils.getFilenameForIcon(entity.name)
        textLabel.foreground = if (Settings.colorTheme == ColorTheme.LIGHT) Color.BLACK else Color.WHITE
        setIcon()
        return wrapperPanel
    }

    fun setSelected(selected: Boolean) {
        entityPanel.apply {
            background = if (selected) Constants.SELECTION_COLOR else Constants.TRANSPARENT_COLOR
            isOpaque = selected
        }
    }
}
