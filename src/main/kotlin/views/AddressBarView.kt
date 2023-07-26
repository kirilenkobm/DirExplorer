package views

import dataModels.ExplorerDirectory
import state.AppState
import java.awt.*
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import kotlin.math.max
import kotlin.math.min


class AddressBarView(private val mainView: MainView) {
    private val addressBar = JPanel()
    private val addressBarPanel = JPanel(BorderLayout())

    init {
        addressBar.layout = BoxLayout(addressBar, BoxLayout.X_AXIS)
        addressBarPanel.add(addressBar, BorderLayout.NORTH)
        updateView()
    }

    private fun createAddressBarButton(partName: String, newPath: Path): JButton {
        return JButton(partName).apply {
            isContentAreaFilled = false  // transparent button
            isBorderPainted = false // remove stroke
            isFocusPainted = false // remove focus highlight TODO: check whether needed
            horizontalTextPosition = SwingConstants.CENTER
            verticalTextPosition = SwingConstants.CENTER
            foreground = Color.BLACK
            font = Font("Arial", Font.PLAIN, 14)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)  // hover cursor
            addActionListener {
                AppState.updateDirectory(ExplorerDirectory(newPath.toString()))
                mainView.updateView()
            }
        }
    }

    fun updateView() {
        addressBar.removeAll()
        addressBar.layout = GridBagLayout()
        val constraints = GridBagConstraints()

        // hold the buttons and separators
        val components = ArrayList<Component>()

        val path = Paths.get(AppState.currentExplorerDirectory.path)
        val rootPath = path.root
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
            val partName = AppState.zipDirMapping[part.toString()] ?: part.toString()
            val button = createAddressBarButton(partName, newPath)
            // update currentPath to the new path
            currentPath = newPath

            val separatorLabel = JLabel(IconManager.chevronRightIcon)
            components.add(separatorLabel)
            components.add(button)
        }

        // Check if the total width of the components is too wide
        val totalWidth = components.sumOf { it.preferredSize.width }
        if (totalWidth > addressBar.width) {
            // if it's too wide, keep the first and last few components as buttons,
            // and replace the middle components with ...

            // TODO: compute properties
            val numStartComponents = 6
            val numEndComponents = 6

            // Safer number of items
            for (i in 0..<min(numStartComponents, components.size)) {
                addressBar.add(components[i], constraints)
            }


            // TODO: maybe? create dropdown menu
            // val middleComponents = components.subList(numStartComponents, components.size - numEndComponents)
            val ellipsisLabel = JLabel("  ...  ")
            addressBar.add(ellipsisLabel)

            // Safely add components to the end of the address bar
            for (i in max(0, components.size - numEndComponents)..<components.size) {
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
    }

    fun getPanel(): JPanel {
        return addressBarPanel
    }
}
