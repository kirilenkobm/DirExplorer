package views.iconviews

import dataModels.*
import state.Settings
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel


abstract class AbstractIconEntityView(entity: FileSystemEntity) {
    protected val iconLabel = JLabel()
    protected val textLabel = JLabel(entity.name)
    protected val entityPanel = JPanel()

    init {
        entityPanel.layout = BoxLayout(entityPanel, BoxLayout.Y_AXIS)
        entityPanel.add(iconLabel)
        entityPanel.add(textLabel)
    }

    protected abstract fun setIcon()

    fun createView(): JPanel {
        setIcon()
        return entityPanel
    }

    fun resizeIcon(icon: ImageIcon): ImageIcon {
        val image = icon.image
        val newImage = image.getScaledInstance(
            Settings.iconSize,
            Settings.iconSize,
            java.awt.Image.SCALE_DEFAULT
        )
        return ImageIcon(newImage)
    }
}
