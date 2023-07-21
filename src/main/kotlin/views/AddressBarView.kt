package views

import dataModels.ExplorerDirectory
import state.AppState
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.nio.file.Paths
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel


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

        // create a button for the root directory
        val rootButton = JButton(rootPath.toString())
        rootButton.addActionListener {
            AppState.updateDirectory(ExplorerDirectory(rootPath.toString()))
            mainView.updateView()
        }
        addressBar.add(rootButton, constraints)

        // create a button for each part of the path
        // TODO: is to be optimised
        // TODO: better design
        for (part in path) {
            // create a new currentPath that includes this part
            val newPath = currentPath.resolve(part)
            val button = JButton(part.toString())
            button.addActionListener {
                AppState.updateDirectory(ExplorerDirectory(newPath.toString()))
                mainView.updateView()
            }
            addressBar.add(button, constraints)
            // update currentPath to the new path
            currentPath = newPath
        }

        // add a filler component that takes up the remaining space
        constraints.weightx = 1.0 // set weightx to 1 for filler
        constraints.fill = GridBagConstraints.HORIZONTAL // resize filler horizontally
        addressBar.add(Box.createHorizontalGlue(), constraints)

        addressBar.revalidate()
        addressBar.repaint()
    }

    fun getPanel(): JPanel {
        return addressBarPanel
    }
}
