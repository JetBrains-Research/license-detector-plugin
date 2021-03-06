package com.jetbrains.sorrel.plugin.toolwindow.panels.project

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.sorrel.plugin.SorrelUtilUI
import com.jetbrains.sorrel.plugin.model.LicenseManager
import com.jetbrains.sorrel.plugin.toolwindow.panels.PanelBase
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JScrollPane

class ProjectLicensePanel(
    project: Project,
    private val licenseManager: LicenseManager,
    private val lifetime: Lifetime
) : PanelBase(com.jetbrains.sorrel.plugin.SorrelBundle.message("sorrel.ui.toolwindow.tab.project.title")) {

    private val titleView = ProjectTitleView(project, licenseManager, lifetime)
    private val compatibleIssueView = CompatibleIssueView()

    override fun build(): JComponent = JBScrollPane(
        SorrelUtilUI.borderPanel {
            border = JBEmptyBorder(12, 12, 20, 12)

            addToTop(titleView.projectTitleViewPanel)
            addToCenter(compatibleIssueView.createPanel(licenseManager.compatibilityIssues, lifetime))
        },
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    ).apply {
        this.border = BorderFactory.createMatteBorder(JBUI.scale(1), 0, 0, 0, JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground())
        this.verticalScrollBar.unitIncrement = 16

        UIUtil.putClientProperty(verticalScrollBar, JBScrollPane.IGNORE_SCROLLBAR_IN_INSETS, true)
    }
}