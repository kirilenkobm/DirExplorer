package views

import dataModels.ExplorerDirectory
import state.AppState
import java.awt.*
import java.nio.file.Paths
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants


class AddressBarView(private val mainView: MainView) {
    val addressBar = JPanel()
    private val addressBarPanel = JPanel(BorderLayout())

    init {
        addressBar.layout = BoxLayout(addressBar, BoxLayout.X_AXIS)
        addressBarPanel.add(addressBar, BorderLayout.NORTH)
        updateView()
    }

    fun updateView() {
        addressBar.removeAll()
        addressBar.layout = GridBagLayout()
        val constraints = GridBagConstraints()

        val path = Paths.get(AppState.currentExplorerDirectory.path)
        val rootPath = path.root
        var currentPath = rootPath // start with the root of the path

        constraints.weightx = 0.0 // set weightx to 0 for buttons
        constraints.fill = GridBagConstraints.NONE // do not resize buttons

        // create a button for each part of the path
        // TODO: is to be optimised
        // TODO: better design
        for (part in path) {
            // create a new currentPath that includes this part
            // TODO: some hashmap from tempZip dir to original zip archive name
            // so that path contains /dir1/dir2/file.zip/dir3, not like
            // /dir1/dir2/.file.zip_xxxxxx/dir3
            val newPath = currentPath.resolve(part)
            val button = JButton(part.toString()).apply {
                isContentAreaFilled = false // make the button transparent
                isBorderPainted = false // remove the border
                isFocusPainted = false // remove the focus highlight
                horizontalTextPosition = SwingConstants.CENTER // center the text
                verticalTextPosition = SwingConstants.CENTER // center the text
                foreground = Color.BLACK // set the text color
                font = Font("Arial", Font.PLAIN, 14) // set the font
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) // change the cursor when hovering over the button
                addActionListener {
                    AppState.updateDirectory(ExplorerDirectory(newPath.toString()))
                    mainView.updateView()
                }
            }
            addressBar.add(button, constraints)
            val separatorLabel = JLabel(">")
            addressBar.add(separatorLabel, constraints)
            // update currentPath to the new path
            currentPath = newPath
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
