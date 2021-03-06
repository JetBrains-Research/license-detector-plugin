package com.jetbrains.sorrel.plugin.editor

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffDialogHints
import com.intellij.diff.DiffManager
import com.intellij.diff.actions.impl.MutableDiffRequestChain
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.util.DiffUserDataKeys
import com.intellij.diff.util.DiffUserDataKeysEx
import com.intellij.diff.util.Side
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.BulkAwareDocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.jetbrains.sorrel.plugin.SorrelUtilUI
import com.jetbrains.sorrel.plugin.SorrelUtilUI.Companion.comboBox
import com.jetbrains.sorrel.plugin.detection.DetectorManager
import com.jetbrains.sorrel.plugin.diff.WordsFirstDiffComputer
import com.jetbrains.sorrel.plugin.licenses.ALL_SUPPORTED_LICENSE
import com.jetbrains.sorrel.plugin.licenses.SupportedLicense
import com.jetbrains.sorrel.plugin.model.ProjectModule
import com.jetbrains.sorrel.plugin.updateAndRepaint
import com.jetbrains.sorrel.plugin.utils.licenseDetectorModel
import com.jetbrains.sorrel.plugin.utils.logDebug

class LicenseFileEditorNotificationPanel(
    val project: Project,
    val licenseFile: VirtualFile
) : EditorNotificationPanel() {

    private val model = project.licenseDetectorModel()
    private val actionName = "Change project license file"

    init {
        text = com.jetbrains.sorrel.plugin.SorrelBundle.message("sorrel.ui.editor.notification.license.file.title")
        myBackgroundColor = SorrelUtilUI.HeaderBackgroundColor

        val module = ModuleUtilCore.findModuleForFile(licenseFile, project)!!
        val projectModule = model.projectModules.value.find { it.nativeModule == module }!!
        val comboBoxCompatibleLicenses = createComboBoxWithLicenses(projectModule)

        myLinksPanel.add(comboBoxCompatibleLicenses)

        val licenseDocument: Document = ReadAction.compute<Document, Throwable> {
            FileDocumentManager.getInstance().getDocument(licenseFile)!!
        }

        if (licenseDocument.isWritable) {
            createUpdateLicenseFileTextActionLabel(comboBoxCompatibleLicenses, licenseDocument)
        }
        addUpdateOnLicenseFileText(comboBoxCompatibleLicenses, licenseDocument)

        createShowDiffLicenseFileActionLabel(comboBoxCompatibleLicenses, licenseFile)

        model.licenseManager.modulesCompatibleLicenses.advise(model.lifetime) {
            updateAndRepaint()
            comboBoxCompatibleLicenses.updateAndRepaint()
        }
        model.licenseManager.modulesLicenses.advise(model.lifetime) {
            val moduleLicense: SupportedLicense = it[projectModule] ?: return@advise
            comboBoxCompatibleLicenses.selectedItem = moduleLicense
        }
    }

    private fun createComboBoxWithLicenses(projectModule: ProjectModule): ComboBox<SupportedLicense> {
        val moduleProjectLicense = model.licenseManager.modulesLicenses.value[projectModule]!!
        val comboBox = comboBox(ALL_SUPPORTED_LICENSE)
        comboBox.isSwingPopup = false
        comboBox.renderer = LicenseListCellRenderer(model, projectModule)
        comboBox.selectedItem = moduleProjectLicense
        addUpdateLicenseFileActions(comboBox)
        return comboBox
    }

    private fun createUpdateLicenseFileTextActionLabel(
        comboBox: ComboBox<SupportedLicense>,
        licenseDocument: Document
    ) {
        val application = ApplicationManager.getApplication()
        val commandProcessor = CommandProcessor.getInstance()

        createActionLabel(com.jetbrains.sorrel.plugin.SorrelBundle.message("sorrel.ui.editor.notification.license.file.action.updateLicenseFileText")) {
            val selectedLicense = (comboBox.selectedItem as SupportedLicense)

            if (licenseDocument.isWritable) {
                commandProcessor.executeCommand(project, {
                    application.runWriteAction {
                        licenseDocument.setText(selectedLicense.fullText)
                    }
                }, actionName, null)
            }
        }
    }

    private fun createShowDiffLicenseFileActionLabel(
        comboBox: ComboBox<SupportedLicense>,
        licenseFile: VirtualFile
    ) {
        createActionLabel(com.jetbrains.sorrel.plugin.SorrelBundle.message("sorrel.ui.editor.notification.license.file.action.showDiffLicenseFile.label")) {
            val diffContentFactory = DiffContentFactory.getInstance()

            val selectedLicense = comboBox.selectedItem as SupportedLicense
            val selectedLicenseDocument = EditorFactory.getInstance().createDocument(selectedLicense.fullText)
            selectedLicenseDocument.setReadOnly(true)
            val referenceLicenseContent = diffContentFactory.create(
                project,
                selectedLicenseDocument,
                PlainTextFileType.INSTANCE
            )
            val currentLicenseFileContent = diffContentFactory.create(project, licenseFile)

            val chain = MutableDiffRequestChain(currentLicenseFileContent, referenceLicenseContent)

            if (currentLicenseFileContent is DocumentContent) {
                val editors = EditorFactory.getInstance().getEditors(currentLicenseFileContent.document)
                if (editors.isNotEmpty()) {
                    val currentLine = editors[0].caretModel.logicalPosition.line
                    chain.putRequestUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.LEFT, currentLine))
                }
            }

            chain.putRequestUserData(DiffUserDataKeys.DO_NOT_IGNORE_WHITESPACES, true)
            chain.putRequestUserData(DiffUserDataKeysEx.CUSTOM_DIFF_COMPUTER, WordsFirstDiffComputer())


            chain.windowTitle = licenseFile.name +
                    com.jetbrains.sorrel.plugin.SorrelBundle.message("sorrel.ui.editor.notification.license.file.action.showDiffLicenseFile.vs") +
                    com.jetbrains.sorrel.plugin.SorrelBundle.message("sorrel.ui.editor.notification.license.file.action.showDiffLicenseFile.referenceLicenseFile")
            chain.title1 = licenseFile.name
            chain.title2 =
                com.jetbrains.sorrel.plugin.SorrelBundle.message("sorrel.ui.editor.notification.license.file.action.showDiffLicenseFile.referenceLicenseFile")

            DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.DEFAULT)
        }
    }

    private fun addUpdateLicenseFileActions(comboBox: ComboBox<SupportedLicense>) {
        comboBox.addActionListener {
            val module = ModuleUtilCore.findModuleForFile(licenseFile, project) ?: return@addActionListener
            val projectModule = model.projectModules.value.find {
                it.nativeModule == module
            } ?: return@addActionListener
            val selectedLicense = (comboBox.selectedItem as SupportedLicense)
            val newModulesLicenses = model.licenseManager.modulesLicenses.value.toMutableMap()
            newModulesLicenses[projectModule] = selectedLicense
            logDebug("Notification panel: Updating license of ${module.name}")
            model.licenseManager.modulesLicenses.set(newModulesLicenses)
        }
    }

    private fun addUpdateOnLicenseFileText(comboBox: ComboBox<SupportedLicense>, licenseDocument: Document) {
        licenseDocument.addDocumentListener(object : BulkAwareDocumentListener.Simple {
            override fun bulkUpdateFinished(document: Document) {
                val licenseDocumentText = document.text
                val license = DetectorManager.getLicenseByFullText(licenseDocumentText)
                comboBox.selectedItem = license
            }
        })
    }
}