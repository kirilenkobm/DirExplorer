package view.customcomponents

import java.awt.*
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

/**
 * FlowLayout subclass that fully supports wrapping of components.
 *
 * Taken from:
 * http://www.camick.com/java/source/WrapLayout.java
 * Translated to Kotlin using IDEA.
 * Was used to make the view in the grid mode adaptive to the window size.

 * Creates a new flow layout manager with the indicated alignment
 * and the indicated horizontal and vertical gaps.
 *
 * @param align the alignment value
 * @param hgap the horizontal gap between components
 * @param vgap the vertical gap between components
 */
class WrapLayout(align: Int, hgap: Int, vgap: Int) : FlowLayout(align, hgap, vgap) {


    /**
     * Returns the preferred dimensions for this layout given the
     * *visible* components in the specified target container.
     * @param target the component which needs to be laid out
     * @return the preferred dimensions to lay out the
     * subcomponents of the specified container
     */
    override fun preferredLayoutSize(target: Container): Dimension {
        return layoutSize(target, true)
    }

    /**
     * Returns the minimum dimensions needed to lay out the *visible*
     * components contained in the specified target container.
     * @param target the component which needs to be laid out
     * @return the minimum dimensions to lay out the
     * subcomponents of the specified container
     */
    override fun minimumLayoutSize(target: Container): Dimension {
        val minimum = layoutSize(target, false)
        minimum.width -= hgap + 1
        return minimum
    }

    /**
     * Returns the minimum or preferred dimension needed to lay out the target
     * container.
     *
     * @param target target to get layout size for
     * @param preferred should preferred size be calculated
     * @return the dimension to lay out the target container
     */
    private fun layoutSize(target: Container, preferred: Boolean): Dimension {
        synchronized(target.treeLock) {

            //  Each row must fit with the width allocated to the container.
            //  When the container width = 0, the preferred width of the container
            //  has not yet been calculated so lets ask for the maximum.
            var targetWidth: Int
            var container: Container = target
            while (container.size.width == 0 && container.parent != null) {
                container = container.parent
            }
            targetWidth = container.size.width
            if (targetWidth == 0) targetWidth = Int.MAX_VALUE
            val hgap = hgap
            val vgap = vgap
            val insets: Insets = target.insets
            val horizontalInsetsAndGap = insets.left + insets.right + hgap * 2
            val maxWidth = targetWidth - horizontalInsetsAndGap

            //  Fit components into the allowed width
            val dim = Dimension(0, 0)
            var rowWidth = 0
            var rowHeight = 0
            val nmembers: Int = target.componentCount
            for (i in 0..<nmembers) {
                val m: Component = target.getComponent(i)
                if (m.isVisible) {
                    val d = if (preferred) m.preferredSize else m.minimumSize

                    //  Can't add the component to current row. Start a new row.
                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight)
                        rowWidth = 0
                        rowHeight = 0
                    }

                    //  Add a horizontal gap for all components after the first
                    if (rowWidth != 0) {
                        rowWidth += hgap
                    }
                    rowWidth += d.width
                    rowHeight = rowHeight.coerceAtLeast(d.height)
                }
            }
            addRow(dim, rowWidth, rowHeight)
            dim.width += horizontalInsetsAndGap
            dim.height += insets.top + insets.bottom + vgap * 2

            //	When using a scroll pane or the DecoratedLookAndFeel we need to
            //  make sure the preferred size is less than the size of the
            //  target container so shrinking the container size works
            //  correctly. Removing the horizontal gap is an easy way to do this.
            val scrollPane: Container? = SwingUtilities.getAncestorOfClass(JScrollPane::class.java, target)
            if (scrollPane != null && target.isValid) {
                dim.width -= hgap + 1
            }
            return dim
        }
    }

    /**
	 *  A new row has been completed. Use the dimensions of this row
	 *  to update the preferred size for the container.
	 *
	 *  @param dim update the width and height when appropriate
	 *  @param rowWidth the width of the row to add
	 *  @param rowHeight the height of the row to add
	 */
    private fun addRow(dim: Dimension, rowWidth: Int, rowHeight: Int) {
        dim.width = dim.width.coerceAtLeast(rowWidth)
        if (dim.height > 0) {
            dim.height += vgap
        }
        dim.height += rowHeight
    }
}
