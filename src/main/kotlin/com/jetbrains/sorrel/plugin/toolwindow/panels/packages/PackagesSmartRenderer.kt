package com.jetbrains.sorrel.plugin.toolwindow.panels.packages

import com.intellij.util.IconUtil
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import com.jetbrains.sorrel.plugin.RiderColor
import com.jetbrains.sorrel.plugin.SorrelUtilUI
import com.jetbrains.sorrel.plugin.model.PackageDependency
import com.jetbrains.sorrel.plugin.toHtml
import org.apache.commons.lang3.StringUtils
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

private val packageIconSize by lazy { JBUI.scale(16) }
private val packageIcon by lazy {
    IconUtil.toSize(
        com.jetbrains.sorrel.plugin.SorrelPluginIcons.Package,
        packageIconSize,
        packageIconSize
    )
}

class PackagesSmartRenderer : ListCellRenderer<PackagesSmartItem> {

    override fun getListCellRendererComponent(
            list: JList<out PackagesSmartItem>,
            item: PackagesSmartItem?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
    ): Component? =
            when (item) {
                is PackagesSmartItem.Package -> {
                    val iconLabel = JLabel(packageIcon).apply {
                        minimumSize = Dimension(packageIconSize, packageIconSize)
                        preferredSize = Dimension(packageIconSize, packageIconSize)
                        maximumSize = Dimension(packageIconSize, packageIconSize)
                    }

                    val packagePanel = createPackagePanel(item.meta, isSelected, list, iconLabel)
                    packagePanel
                }
                is PackagesSmartItem.Header -> item.panel
                is PackagesSmartItem.Fake -> PackagesSmartItem.Fake.panel
                null -> null
            }

    private fun createPackagePanel(
        packageSearchDependency: PackageDependency,
        isSelected: Boolean,
        list: JList<out PackagesSmartItem>,
        iconLabel: JLabel
    ): JPanel {
        val textColor = SorrelUtilUI.getTextColor(isSelected)
        val textColor2 = SorrelUtilUI.getTextColor2(isSelected)

        return buildPanel(
            packageSearchDependency = packageSearchDependency,
            applyColors = applyColors(isSelected, list),
            iconLabel = iconLabel,
            idMessage = buildIdMessage(packageSearchDependency, textColor, textColor2),
            mainLicenseName = packageSearchDependency.getMainLicense()?.name,
            otherLicensesNames = buildOtherLicensesNames(packageSearchDependency, textColor2)
        )
    }

    private fun buildIdMessage(
        packageSearchDependency: PackageDependency,
        textColor: RiderColor,
        textColor2: RiderColor
    ): String = buildString {
        if (packageSearchDependency.remoteInfo?.name != null && packageSearchDependency.remoteInfo?.name != packageSearchDependency.identifier) {
            append(colored(StringUtils.normalizeSpace(packageSearchDependency.remoteInfo?.name), textColor))
            append(" ")
            append(colored(packageSearchDependency.identifier, textColor2))
        } else {
            append(colored(packageSearchDependency.identifier, textColor))
        }
    }

    private fun buildOtherLicensesNames(
        packageSearchDependency: PackageDependency,
        textColor: RiderColor
    ): List<String> {
        return packageSearchDependency.getOtherLicenses().map {
            colored(it.name, textColor)
        }
    }

    private fun applyColors(isSelected: Boolean, list: JList<out PackagesSmartItem>): (JComponent) -> Unit {
        val itemBackground = if (isSelected) list.selectionBackground else list.background
        val itemForeground = if (isSelected) list.selectionForeground else list.foreground

        return {
            it.background = itemBackground
            it.foreground = itemForeground
        }
    }

    @Suppress("LongParameterList")
    private fun buildPanel(
        packageSearchDependency: PackageDependency,
        applyColors: (JComponent) -> Unit,
        iconLabel: JLabel,
        idMessage: String,
        mainLicenseName: String?,
        otherLicensesNames: List<String>
    ): JPanel = JPanel(BorderLayout()).apply {
        @Suppress("MagicNumber") // Gotta love Swing APIs
        if (packageSearchDependency.identifier.isNotBlank()) {
            applyColors(this)
            border = JBEmptyBorder(0, 0, 0, 18)

            add(JPanel().apply {
                applyColors(this)
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = JBEmptyBorder(2, 8, 2, 4)

                add(iconLabel)
            }, BorderLayout.WEST)

            add(JPanel().apply {
                applyColors(this)
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                add(JLabel("<html>$idMessage</html>"))
            }, BorderLayout.CENTER)

            add(JPanel().apply {
                applyColors(this)
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                if (mainLicenseName != null) {
                    add(JLabel("<html>$mainLicenseName</html>"))
                }

                otherLicensesNames.forEach {
                    add(JLabel("<html>$it</html>"))
                }

            }, BorderLayout.EAST)
        }
    }

    private fun colored(text: String?, color: RiderColor) = "<font color=${color.toHtml()}>${text ?: ""}</font>"
}
