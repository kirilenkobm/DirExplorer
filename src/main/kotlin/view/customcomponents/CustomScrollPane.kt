package view.customcomponents

import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import javax.swing.JScrollPane
import kotlin.math.ceil
import kotlin.math.max

/**
 * Default scroll pane was incompatible with any adaptive grid,
 * because it provided the child view the maximal width that
 * was ever reach, instead of the viewport width.
 * As the result, when user decreased the DirExplorer window,
 * the columns did not adapt. The grid adapted only to
 * window increasing instead.
 * This custom scroll pane provides the child view the
 * actual viewport size.
 */
class CustomScrollPane(component: JPanel) : JScrollPane(component) {
    init {
        viewport.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
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
        })
    }
}
