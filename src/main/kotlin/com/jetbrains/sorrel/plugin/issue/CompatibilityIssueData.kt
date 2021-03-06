package com.jetbrains.sorrel.plugin.issue

data class CompatibilityIssueData(
    val packageDependencyLicenseIssueGroups: List<PackageDependencyIssueGroup>,
    val submoduleLicenseIssueGroups: List<SubmoduleIssueGroup>
) {

    fun isEmpty(): Boolean {
        return packageDependencyLicenseIssueGroups.isEmpty() && submoduleLicenseIssueGroups.isEmpty()
    }

    fun convertCompatibilityIssuesDataToPlainText(): String {
        val stringBuilder = StringBuilder()
        if (packageDependencyLicenseIssueGroups.isNotEmpty()) {
            stringBuilder.append(
                convertPackageDependencyIssueGroupsToPlainText()
            )
        }
        if (submoduleLicenseIssueGroups.isNotEmpty()) {
            stringBuilder.append(
                convertSubmodulesIssueGroupsToPlainText()
            )
        }
        return stringBuilder.toString()
    }

    fun convertPackageDependencyIssueGroupsToPlainText(): String {
        val stringBuilder = StringBuilder()
        for (group in packageDependencyLicenseIssueGroups) {
            group.issues.forEach { packageDependencyIssue ->
                stringBuilder.append(
                    com.jetbrains.sorrel.plugin.SorrelBundle.message(
                        "sorrel.ui.compatibilityIssues.plainText.moduleAndDependency",
                        group.moduleName,
                        group.moduleLicenseName,
                        packageDependencyIssue.packageIdentifier,
                        packageDependencyIssue.licenseName
                    )
                )
                stringBuilder.appendLine()
            }
        }
        stringBuilder.removeSuffix("\n")
        return stringBuilder.toString()
    }

    fun convertSubmodulesIssueGroupsToPlainText(): String {
        val stringBuilder = StringBuilder()
        for (group in submoduleLicenseIssueGroups) {
            group.issues.forEach {
                stringBuilder.append(
                    com.jetbrains.sorrel.plugin.SorrelBundle.message(
                        "sorrel.ui.compatibilityIssues.plainText.moduleAndSubmodules",
                        group.moduleName,
                        group.moduleLicenseName,
                        it.moduleName,
                        it.licenseName
                    )
                )
                stringBuilder.appendLine()
            }
        }
        stringBuilder.removeSuffix("\n")
        return stringBuilder.toString()
    }

    fun convertCompatibilityIssuesDataToHtml(): String {
        val stringBuilder = StringBuilder("<html><body><ol>")
        if (packageDependencyLicenseIssueGroups.isNotEmpty()) {
            stringBuilder.append(
                convertPackageDependencyIssueGroupsToHtml()
            )
        }
        if (submoduleLicenseIssueGroups.isNotEmpty()) {
            stringBuilder.append(
                convertSubmodulesIssueGroupsToHtml()
            )
        }
        stringBuilder.append("</ol></body></html>")
        return stringBuilder.toString()
    }

    private fun convertPackageDependencyIssueGroupsToHtml(): String {
        val stringBuilder = StringBuilder()

        for (group in packageDependencyLicenseIssueGroups) {
            stringBuilder.append("<li>")
            stringBuilder.append(
                com.jetbrains.sorrel.plugin.SorrelBundle.message(
                    "sorrel.ui.compatibilityIssues.html.moduleAndDependency.head",
                    group.moduleName,
                    group.moduleLicenseName
                )
            )
            stringBuilder.append("<ul>")
            group.issues.forEach { packageDependencyIssue ->
                stringBuilder.append("<li>")
                stringBuilder.append(
                    com.jetbrains.sorrel.plugin.SorrelBundle.message(
                        "sorrel.ui.compatibilityIssues.html.moduleAndDependency",
                        packageDependencyIssue.packageIdentifier,
                        packageDependencyIssue.licenseName
                    )
                )
                stringBuilder.append("</li>")
            }
            stringBuilder.append("</ul>")
            stringBuilder.append("</li>")
        }

        return stringBuilder.toString()
    }

    private fun convertSubmodulesIssueGroupsToHtml(): String {
        val stringBuilder = StringBuilder()

        for (group in submoduleLicenseIssueGroups) {
            stringBuilder.append("<li>")
            stringBuilder.append(
                com.jetbrains.sorrel.plugin.SorrelBundle.message(
                    "sorrel.ui.compatibilityIssues.html.moduleAndSubmodules.head",
                    group.moduleName,
                    group.moduleLicenseName
                )
            )
            stringBuilder.append("<ul>")
            group.issues.forEach {
                stringBuilder.append("<li>")
                stringBuilder.append(
                    com.jetbrains.sorrel.plugin.SorrelBundle.message(
                        "sorrel.ui.compatibilityIssues.html.moduleAndSubmodules",
                        it.moduleName,
                        it.licenseName
                    )
                )
                stringBuilder.append("</li>")
            }
            stringBuilder.append("</ul>")
            stringBuilder.append("</li>")
        }

        return stringBuilder.toString()
    }
}
