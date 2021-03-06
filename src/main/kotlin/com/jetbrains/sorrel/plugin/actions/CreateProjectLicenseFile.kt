package com.jetbrains.sorrel.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.WriteActionAware
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.module.ModuleUtilCore.findModuleForFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.IncorrectOperationException
import com.jetbrains.sorrel.plugin.detection.DetectorManager.licenseFileNamePattern
import com.jetbrains.sorrel.plugin.licenses.NoLicense
import com.jetbrains.sorrel.plugin.utils.licenseDetectorModel
import com.jetbrains.sorrel.plugin.utils.logDebug
import java.io.File

class CreateProjectLicenseFile : AnAction(), WriteActionAware {

    companion object {
        const val LICENSE_FILE_NAME: String = "LICENSE.txt"
    }

    private val actionName = "CreateProjectLicenseFile"

    override fun actionPerformed(e: AnActionEvent) {
        val application = ApplicationManager.getApplication()
        val commandProcessor = CommandProcessor.getInstance()

        val project: Project = e.project!!

        val model = project.licenseDetectorModel()

        val module = findModuleForFile(e.getRequiredData(PlatformDataKeys.VIRTUAL_FILE), project)!!
        val moduleDir = PsiManager.getInstance(project).findDirectory(module.guessModuleDir()!!)!!

        val createProjectLicenseFile = {
            try {
                val licenseFile: PsiFile = WriteAction.compute(
                    ThrowableComputable<PsiFile, IncorrectOperationException> {
                        moduleDir.createFile(LICENSE_FILE_NAME)
                    }
                )

                val licenseDocument = PsiDocumentManager.getInstance(project).getDocument(licenseFile)!!
                val curProjectModule = model.projectModules.value.find { it.nativeModule == module }!!
                val compatibleLicenses = model.licenseManager.modulesCompatibleLicenses
                    .value[curProjectModule]!!

                //TODO: Mb must be done in full order in licenses. Now the order is partial
                if (compatibleLicenses.any()) {
                    val recommendedLicense = compatibleLicenses[0]
                    application.runWriteAction {
                        licenseDocument.setText(recommendedLicense.fullText)
                    }
                    val newModulesLicenseMap = model.licenseManager.modulesLicenses.value.toMutableMap()
                    newModulesLicenseMap[curProjectModule] = recommendedLicense
                    model.licenseManager.modulesLicenses.set(newModulesLicenseMap)
                } else {
                    application.runWriteAction {
                        licenseDocument.setText(NoLicense.fullText)
                    }
                    val newModulesLicenseMap = model.licenseManager.modulesLicenses.value.toMutableMap()
                    newModulesLicenseMap[curProjectModule] = NoLicense
                    model.licenseManager.modulesLicenses.set(newModulesLicenseMap)
                }

                val openFileDescriptor = OpenFileDescriptor(project, licenseFile.virtualFile)
                openFileDescriptor.navigate(true)

            } catch (e: IncorrectOperationException) {
                logDebug("Failed to create license file.", e)
            }
        }

        commandProcessor.executeCommand(
            project,
            createProjectLicenseFile, actionName, null
        )
    }

    override fun update(e: AnActionEvent) {
        val project = e.project

        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        if (virtualFile == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val module = findModuleForFile(virtualFile, project)
        if (module == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val moduleDirPath = module.guessModuleDir()
        if (moduleDirPath == null) {
            e.presentation.isEnabled = false
            return
        }

        val moduleDir = File(moduleDirPath.path)
        if (!moduleDir.exists() || !moduleDir.isDirectory || !moduleDir.canRead()) {
            e.presentation.isEnabled = false
            return
        }

        if (moduleDir.listFiles()!!.any { licenseFileNamePattern.matches(it.name) }) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
    }
}