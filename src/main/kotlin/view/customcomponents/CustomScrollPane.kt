package view.customcomponents

import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import javax.swing.JScrollPane
import kotlin.math.ceil
import kotlin.math.max

/**
 * The default scroll pane was incompatible with any adaptive grid because it provided the child view
 * with the maximal width that was ever reached, instead of the viewport width. As a result, when the
 * user decreased the DirExplorer window, the columns did not adapt.
 * The grid only adapted to window increasing.
 *
 * This custom scroll pane addresses the issue by providing the child view with the actual viewport size.
 * It adjusts the preferred size of the component within the scroll pane based on the viewport
 * width and the number of columns needed for the adaptive grid layout.
 */
class CustomScrollPane(component: JPanel) : JScrollPane(component) {
    init {
        viewport.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                updatePreferredSize()
            }
        })
    }

    fun updatePreferredSize() {
        val componentInScrollPane = viewport.view as? JPanel
        componentInScrollPane?.let { comp ->
            setComponentPreferredSize(comp)
        }
    }

    private fun setComponentPreferredSize(component: JPanel) {
        if (component.componentCount > 0) {
            val width = viewport.width
            val layout = component.layout as WrapLayout
            val hgap = layout.hgap
            val vgap = layout.vgap
            val componentWidth = component.getComponent(0).preferredSize.width
            val columns = max(1, (width - hgap) / (componentWidth + hgap))
            val rows = ceil(component.componentCount.toDouble() / columns).toInt()
            val height = rows * (component.getComponent(0).preferredSize.height + vgap) + hgap
            component.preferredSize = Dimension(width, height)
        }
    }
}