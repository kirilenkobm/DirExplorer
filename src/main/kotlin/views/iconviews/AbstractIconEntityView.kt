package views.iconviews

import dataModels.*
import kotlinx.coroutines.CoroutineScope
import state.Settings
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants


abstract class AbstractIconEntityView(private val entity: FileSystemEntity) {
    protected val iconLabel = JLabel()
    protected val textLabel = JLabel()
    protected val entityPanel = JPanel(GridBagLayout())
    private val maxNameLen = 24
    private val maxExtensionLen = 7

    init {
        val gbc = GridBagConstraints()
        gbc.gridwidth = GridBagConstraints.REMAINDER
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        gbc.weighty = 1.0

        // Set alignment to center
        iconLabel.horizontalAlignment = SwingConstants.CENTER
        textLabel.horizontalAlignment = SwingConstants.CENTER

        entityPanel.add(iconLabel, gbc)
        entityPanel.add(textLabel, gbc)
    }

    protected abstract fun setIcon()

    fun createView(): JPanel {
        setIcon()
        setText(entity.name)
        return entityPanel
    }

    fun resizeIcon(icon: ImageIcon): ImageIcon {
        val image = icon.image
        val imageWidth = image.getWidth(null)
        val imageHeight = image.getHeight(null)

        // If the image's width or height is already equal to Settings.iconSize,
        // return the original icon - do not waste resources on rescaling
        if (imageWidth == Settings.iconSize || imageHeight == Settings.iconSize) {
            return icon
        }

        // Otherwise, scale the image
        val newImage = image.getScaledInstance(
            Settings.iconSize,
            Settings.iconSize,
            java.awt.Image.SCALE_DEFAULT
        )
        return ImageIcon(newImage)
    }


    // In case filename is too long, I'd like to shorten
    // it in the icon view, replacing part of the name
    // with ellipsis
    // TODO: improve this function
    private fun setText(filename: String) {
        val extension = filename.substringAfterLast(".", "")
        val nameWithoutExtension = filename.substringBeforeLast(".")

        val finalName = if (filename.length > maxNameLen) {
            val trimmedName = nameWithoutExtension.take(maxNameLen - extension.length - 3)
            "$trimmedName...$extension"
        } else {
            filename
        }

        // split into two lines if it's too long
        val splitName = if (finalName.length > maxNameLen / 2 && finalName.length > 1) {
            val firstHalf = finalName.take(finalName.length / 2)
            val secondHalf = finalName.substring(finalName.length / 2)
            "$firstHalf<br>$secondHalf"
        } else {
            finalName
        }


        textLabel.text = "<html>$splitName</html>"
    }
}
