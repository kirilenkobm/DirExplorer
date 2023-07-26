package views.iconviews

import dataModels.*
import state.Settings
import views.directoryviews.IconsDirectoryView
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants


abstract class AbstractIconEntityView(private val entity: FileSystemEntity, private val parentDirView: IconsDirectoryView) {
    protected val iconLabel = JLabel()
    private val textLabel = JLabel()
    protected val entityPanel = JPanel()
    private val wrapperPanel = JPanel(GridBagLayout())
    private val maxNameLen = 24
    // private val maxExtensionLen = 7
    private val maxIconHeight = 120

    init {
        val gbc = GridBagConstraints()
        gbc.gridwidth = GridBagConstraints.REMAINDER
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        gbc.weighty = 1.0

        // Align elements to center -> Q: why do I have to do it twice?
        iconLabel.horizontalAlignment = SwingConstants.CENTER
        textLabel.horizontalAlignment = SwingConstants.CENTER
        iconLabel.alignmentX = Component.CENTER_ALIGNMENT
        textLabel.alignmentX = Component.CENTER_ALIGNMENT

        // BoxLayout allows to limit height
        entityPanel.layout = BoxLayout(entityPanel, BoxLayout.Y_AXIS)
        entityPanel.add(iconLabel)
        entityPanel.add(textLabel)

        entityPanel.maximumSize = Dimension(entityPanel.maximumSize.width, maxIconHeight)
//        if (Settings.colorTheme == ColorTheme.DARK) {
//            entityPanel.background = Color.DARK_GRAY
//        }
        wrapperPanel.add(entityPanel, gbc)

        entityPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 1) {
                    parentDirView.setSelectedIcon(this@AbstractIconEntityView)
                } else if (e.clickCount == 2) {
                    parentDirView.setSelectedIcon(this@AbstractIconEntityView)
                    parentDirView.performEntityAction(entity)
                }
            }
        })
    }

    protected abstract fun setIcon()

    fun createView(): JPanel {
        setIcon()
        setText(entity.name)
        return wrapperPanel
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
            Image.SCALE_DEFAULT
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
            val trimLength = maxOf(0, maxNameLen - extension.length - 3)
            val trimmedName = nameWithoutExtension.take(trimLength)
            "$trimmedName...$extension"
        } else {
            filename
        }

        // split into two lines if it's too long
        val splitName = if (finalName.length > maxNameLen / 2) {
            val firstHalf = finalName.take(finalName.length / 2)
            val secondHalf = finalName.substring(finalName.length / 2)
            "$firstHalf<br>$secondHalf"
        } else {
            finalName
        }

        textLabel.text = "<html>$splitName</html>"
    }

    fun setSelected(selected: Boolean) {
        if (selected) {
            entityPanel.background = Color(66, 135, 245, 255)
        } else {
            entityPanel.background = Color(0, 0, 0, 0)
            entityPanel.isOpaque = false
        }
    }
}
