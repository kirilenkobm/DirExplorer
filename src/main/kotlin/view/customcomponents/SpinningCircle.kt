package view.customcomponents

import state.Settings
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Timer
import javax.swing.JPanel
import kotlin.math.min

/**
 * The SpinningCircle class extends JPanel to create a loading spinner.
 * This spinner is used to visually indicate that a zip archive is currently unpacking.
 */
class SpinningCircle : JPanel() {
    private var angle = 360.0

    init {
        val timer = Timer(10) { repaint() }
        timer.start()
        isOpaque = false
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(Settings.iconSize, Settings.iconSize)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.stroke = BasicStroke(10f)
        g2.color = Color.GRAY

        val w = width
        val h = height
        val size = min(w, h)
        val insets = size / 10

        g2.drawArc(insets, insets, size - 2 * insets, size - 2 * insets, angle.toInt(), -120)
        angle -= 5.0
        if (angle <= 0) {
            angle = 360.0
        }
    }
}
