package com.jetbrains.sorrel.plugin.toolwindow.panels.packages

import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.jetbrains.sorrel.plugin.ComponentActionWrapper
import com.jetbrains.sorrel.plugin.SorrelUtilUI
import com.jetbrains.sorrel.plugin.SorrelUtilUI.Companion.createActionToolbar
import com.jetbrains.sorrel.plugin.toolwindow.panels.PanelBase
import com.jetbrains.sorrel.plugin.toolwindow.panels.RefreshAction
import com.jetbrains.sorrel.plugin.updateAndRepaint
import com.jetbrains.sorrel.plugin.utils.licenseDetectorModel
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.DocumentEvent

class PackageLicensesPanel(
    val project: Project
) : PanelBase(com.jetbrains.sorrel.plugin.SorrelBundle.message("sorrel.ui.toolwindow.tab.packages.title")) {

    private val smartList = PackagesSmartList(project)

    val searchTextField = PackagesSmartSearchField()
        .apply {
            goToList = {
                if (smartList.hasPackageItems) {
                    smartList.selectedIndex = smartList.firstPackageIndex
                    IdeFocusManager.getInstance(project).requestFocus(smartList, false)
                    true
                } else {
                    false
                }
            }
        }

    private val packagesPanel = SorrelUtilUI.borderPanel {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    private val moduleContextComboBox = ModuleContextComboBox(project)

    private fun updateLaf() {
        @Suppress("MagicNumber") // Gotta love Swing APIs
        with(searchTextField) {
            textEditor.putClientProperty("JTextField.Search.Gap", JBUI.scale(6))
            textEditor.putClientProperty("JTextField.Search.GapEmptyText", JBUI.scale(-1))
            textEditor.border = JBUI.Borders.empty(0, 6, 0, 0)
            textEditor.isOpaque = true
            textEditor.background = SorrelUtilUI.HeaderBackgroundColor
        }
    }

    private fun createModuleSelectionActionGroup() = DefaultActionGroup().apply {
        add(ComponentActionWrapper { moduleContextComboBox })
    }

    private val moduleSelectionToolbar = ActionManager.getInstance().createActionToolbar(
        "",
        createModuleSelectionActionGroup(),
        true
    ).apply {
        component.background = SorrelUtilUI.HeaderBackgroundColor
        component.border = BorderFactory.createMatteBorder(
            0, JBUI.scale(1), 0, 0,
            JBUI.CurrentTheme.CustomFrameDecorations.paneBackground()
        )
    }

    private val headerPanel = SorrelUtilUI.headerPanel {
        SorrelUtilUI.setHeight(this, SorrelUtilUI.MediumHeaderHeight)

        border = BorderFactory.createEmptyBorder()

        addToCenter(object : JPanel() {
            init {
                //TODO: Fix right insets on last component
                layout = MigLayout("ins 0 0 0 0, fill", "[left, fill, grow][][right]", "center")
                add(searchTextField)
                add(moduleSelectionToolbar.component)
                add(createActionToolbar(RefreshAction()))
            }

            override fun getBackground() = SorrelUtilUI.UsualBackgroundColor
        })
    }

    private val scrollPane = JBScrollPane(
        packagesPanel.apply {
            add(createListPanel(smartList))
            add(Box.createVerticalGlue())
        },
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    ).apply {
        this.border = BorderFactory.createMatteBorder(
            JBUI.scale(1),
            0,
            0,
            0,
            JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground()
        )
        this.verticalScrollBar.unitIncrement = 16

        UIUtil.putClientProperty(verticalScrollBar, JBScrollPane.IGNORE_SCROLLBAR_IN_INSETS, true)
    }

    private fun createListPanel(list: PackagesSmartList) = SorrelUtilUI.borderPanel {
        minimumSize = Dimension(1, 1)
        maximumSize = Dimension(Int.MAX_VALUE, maximumSize.height)
        add(list, BorderLayout.NORTH)
        SorrelUtilUI.updateParentHeight(list)
    }

    init {
        val viewModel = project.licenseDetectorModel()

        viewModel.searchQuery.set("")

        viewModel.status.advise(viewModel.lifetime) {
            searchTextField.isEnabled = !it.isBusy
        }

        smartList.transferFocusUp = {
            IdeFocusManager.getInstance(viewModel.project).requestFocus(searchTextField, false)
        }

        searchTextField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                ApplicationManager.getApplication().invokeLater {
                    viewModel.searchQuery.set(searchTextField.text)
                }
            }
        })
        viewModel.searchQuery.advise(viewModel.lifetime) { searchTerm ->
            if (searchTextField.text != searchTerm) {
                searchTextField.text = searchTerm
            }
        }

        viewModel.searchResultsUpdated.advise(viewModel.lifetime) {
            smartList.updateAllPackages(it.values.toList())
            packagesPanel.updateAndRepaint()
        }

        smartList.addPackageSelectionListener {
            viewModel.selectedPackage.set(it.identifier)
        }

        viewModel.status.advise(viewModel.lifetime) {
            smartList.installedHeader.setProgressVisibility(it.isBusy)
            smartList.updateAndRepaint()
            packagesPanel.updateAndRepaint()
        }

        // LaF
        updateLaf()
        ApplicationManager.getApplication().messageBus.connect().subscribe(
            LafManagerListener.TOPIC, LafManagerListener { updateLaf() }
        )

        //Paint packages after build ui
        //viewModel.refreshFoundPackages()
    }

    override fun build() = SorrelUtilUI.boxPanel {
        add(headerPanel)
        add(scrollPane)

        @Suppress("MagicNumber") // Swing APIs are <3
        minimumSize = Dimension(JBUI.scale(200), minimumSize.height)
    }
}
