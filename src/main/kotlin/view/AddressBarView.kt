package view

import Constants
import model.DirectoryObserver
import model.ExplorerDirectory
import state.AppState
import state.AppStateUpdater
import state.ColorTheme
import state.Settings
import util.IconManager
import util.SystemRelatedValues
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.SwingConstants
import kotlin.math.max
import kotlin.math.min

/**
 * Class representing the address bar view in the application.
 *
 * This class is responsible for creating and updating the address bar, which displays the current directory path.
 * The path is represented as a series of buttons, each corresponding to a part of the path.
 * Clicking on a button updates the application state to navigate to that part of the path.
 *
 * The class implements the DirectoryObserver interface, allowing it to respond to changes
 * in the current directory by updating the address bar view.
 *
 * The address bar view also handles cases where the path is too long to fit in the available space,
 * in which case it displays the start and end of the path with an ellipsis in the middle.
 */
class AddressBarView: DirectoryObserver {
    private val addressBar = JPanel()
    private val addressBarPanel = JPanel(BorderLayout())

    init {
        AppState.addDirectoryObserver(this)
        addressBarPanel.border = null
        addressBarPanel.add(addressBar, BorderLayout.NORTH)
        updateView()
    }

    // Address bar buttons indicate parts of the current path separated by > character
    // Click on it triggers the AppState change.
    private fun createButton(text: String? = null, icon: Icon? = null, action: ActionListener? = null): JButton {
        return JButton(text, icon).apply {
            isContentAreaFilled = false  // transparent button
            isBorderPainted = false // remove stroke
            isFocusPainted = false // remove focus highlight
            horizontalTextPosition = SwingConstants.CENTER
            verticalTextPosition = SwingConstants.CENTER
            foreground = if (Settings.colorTheme == ColorTheme.LIGHT) {
                Color.BLACK
            } else {
                Color.WHITE
            }
            font = Font("Arial", Font.PLAIN, 14)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)  // hover cursor
            action?.let { addActionListener(it) }
        }
    }

    private fun createAddressBarButton(partName: String, newPath: Path): JButton {
        val action = ActionListener {
            AppStateUpdater.updateDirectory(ExplorerDirectory(newPath.toString()))
        }
        return createButton(partName, action = action)
    }

    private fun createDriveMenuForWindows(button: JButton) {
        val driveMenu = JPopupMenu()
        File.listRoots().forEach { drive ->
            val driveItem = JMenuItem(drive.absolutePath)
            driveItem.addActionListener {
                AppStateUpdater.updateDirectory(ExplorerDirectory(drive.absolutePath))
            }
            driveMenu.add(driveItem)
        }
        button.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                driveMenu.show(e.component, e.x, e.y)
            }
        })
    }

    private fun createWindowsThisPCButton(): JButton {
        return createButton(icon = IconManager.windowsThisPCIcon)
    }

    fun updateView() {
        addressBar.removeAll()
        addressBar.layout = GridBagLayout()
        addressBar.border = null
        val constraints = GridBagConstraints()

        // hold the buttons and separators
        val components = ArrayList<Component>()

        // On Windows, add a computer icon button that shows a dropdown menu of drives
        if (SystemRelatedValues.isWindows) {
            val computerButton = createWindowsThisPCButton()
            val separatorLabel = JLabel(IconManager.chevronRightIcon)
            createDriveMenuForWindows(computerButton)
            components.add(computerButton)
            components.add(separatorLabel)
        }

        val path = Paths.get(AppState.currentExplorerDirectory.path)
        val rootPath = SystemRelatedValues.rootDir()
        var currentPath = rootPath // start with the root of the path
        val rootButton = createAddressBarButton(rootPath.toString(), rootPath)

        components.add(rootButton)
        constraints.weightx = 0.0 // set weightx to 0 for buttons
        constraints.fill = GridBagConstraints.NONE // do not resize buttons
        // adjust the right inset to reduce the space after the button
        constraints.insets = Insets(0, 0, 0, -5)

        // create a button for each part of the path
        for (part in path) {
            // create a new currentPath that includes this part
            val newPath = currentPath.resolve(part)
            val partName = AppState.tempZipDirToNameMapping[part.toString()] ?: part.toString()
            val button = createAddressBarButton(partName, newPath)
            // update currentPath to the new path
            currentPath = newPath

            val separatorLabel = JLabel(IconManager.chevronRightIcon)
            components.add(separatorLabel)
            components.add(button)
        }

        // Check if the total width of the components is too wide
        val totalWidth = components.sumOf { it.preferredSize.width }
        val addressBarWidth = addressBar.width
        // Had to add addressBarWidth > 0 bc at start it's 0
        if (addressBarWidth in 1..<totalWidth) {

            var prefferedStartComponentsNum = Constants.ADDRESS_BAR_PREFERRED_NUM_START_ELEMS
            if (SystemRelatedValues.isWindows) {
                prefferedStartComponentsNum += 2
            }

            // Safer number of items
            for (i in 0..<min(prefferedStartComponentsNum, components.size)) {
                addressBar.add(components[i], constraints)
            }
            // val middleComponents = components.subList(numStartComponents, components.size - numEndComponents)
            val ellipsisLabel = JLabel(Constants.ELLIPSIS_LABEL)
            addressBar.add(ellipsisLabel)

            // Safely add components to the end of the address bar
            for (i in max(0, components.size - Constants.ADDRESS_BAR_PREFERRED_NUM_END_ELEMS)..<components.size)
            {
                addressBar.add(components[i], constraints)
            }
        } else {
            // If it's not too wide, add all the components to the address bar
            for (component in components) {
                addressBar.add(component, constraints)
            }
        }

        // add a filler component that takes up the remaining space
        constraints.weightx = 1.0 // set weightx to 1 for filler
        constraints.fill = GridBagConstraints.HORIZONTAL // resize filler horizontally
        addressBar.add(Box.createHorizontalGlue(), constraints)
        addressBar.border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK)

        addressBar.revalidate()
        addressBar.repaint()
        addressBar.background = if (Settings.colorTheme == ColorTheme.LIGHT) {
            Color(255, 255, 255, 255)
        } else {
            Color.DARK_GRAY
        }
    }

    fun getPanel(): JPanel {
        return addressBarPanel
    }

    override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
        updateView()
    }
}
