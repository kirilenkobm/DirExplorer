package views.iconviews

import Constants
import dataModels.*
import state.ColorTheme
import state.Settings
import utils.Utils
import views.directoryviews.GridDirectoryView
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants


abstract class AbstractIconEntityView(
    private val entity: FileSystemEntity,
    private val parentDirView: GridDirectoryView,
    private val colorTheme: ColorTheme)
{
    // The icon view must look like a vertical stack
    // Top element - frame of the same size, regardless on the icon size
    // icon is sentered in this frame
    // Text -> the same size too, but having lesses height
    // text aligned to top by y-axis, to center by x-axis
    val iconLabel = JLabel().apply {
        horizontalAlignment = SwingConstants.CENTER
        alignmentX = Component.CENTER_ALIGNMENT
    }

    private val textLabel = JLabel().apply {
        horizontalAlignment = SwingConstants.CENTER
        alignmentX = Component.CENTER_ALIGNMENT
    }

    private val textPanel = JPanel(BorderLayout()).apply {
        preferredSize = Dimension(90, 35)
        maximumSize = preferredSize
        isOpaque = false
        add(textLabel, BorderLayout.NORTH)
    }

    private val entityPanel = JPanel(GridBagLayout()).apply {
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
        iconLabel.preferredSize = Dimension(90, 85)
        iconLabel.maximumSize = iconLabel.preferredSize
        add(iconLabel, constraints)

        // Constraints for textPanel
        constraints.gridy = 1
        add(textPanel, constraints)

        preferredSize = Dimension(90, 120)
        maximumSize = preferredSize
        isOpaque = false
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                when (e.clickCount) {
                    1 -> parentDirView.setSelectedIcon(this@AbstractIconEntityView)
                    2 -> {
                        parentDirView.setSelectedIcon(this@AbstractIconEntityView)
                        parentDirView.performEntityAction(entity)
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
        preferredSize = Dimension(90, 120)
        maximumSize = preferredSize
    }

    protected abstract fun setIcon()

    fun createView(): JPanel {
        setIcon()
        textLabel.text = Utils.getFilenameForIcon(entity.name)
        textLabel.foreground = if (colorTheme == ColorTheme.LIGHT) Color.BLACK else Color.WHITE
        return wrapperPanel
    }


    fun resizeIcon(icon: ImageIcon): ImageIcon {
        val image = icon.image
        val imageWidth = image.getWidth(null)
        val imageHeight = image.getHeight(null)

        // If the image's width or height is already equal to Settings.iconSize,
        // return the original icon - do not waste resources on rescaling
        return if (imageWidth == Settings.iconSize || imageHeight == Settings.iconSize) {
            icon
        } else {
            ImageIcon(image.getScaledInstance(Settings.iconSize, Settings.iconSize, Image.SCALE_DEFAULT))
        }
    }

    fun setSelected(selected: Boolean) {
        entityPanel.apply {
            background = if (selected) Constants.SELECTION_COLOR else Constants.TRANSPARENT_COLOR
            isOpaque = selected
        }
    }
}
