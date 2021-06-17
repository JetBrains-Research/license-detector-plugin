# License Management Plugin

[![JB Research](https://jb.gg/badges/research-flat-square.svg)](https://research.jetbrains.org/)
![Build](https://github.com/DmitryPogrebnoy/license-compatibility-plugin/workflows/Build/badge.svg)

<!-- Plugin description -->
A plugin for IntelliJ IDEA for license management and incompatibility detection.

## Features

- [x] Detect the licenses of the project's dependencies and visualize them by a module.
- [x] Show inlay hints with license names for dependency add expressions in project build scripts.
- [x] Suggest possible license for the project/module based on its dependencies.
- [x] Detect possible incompatibilities between the licenses in the project and warn the developer about them.
- [x] Provide short and understandable descriptions for existing licenses.

<!-- Plugin description end -->

## Installation

The plugin is currently in development, so the first version is coming on the Marketplace.

You can install the plugin in IDEA as follows:

1. Run the gradle command `build`. jar-archive of the plugin will be in the `build/libs` directory.
2. To install the plugin you need to go to `File` - `Settings` - `Plugins` - ⚙️ - `Install from disk` - Pick plugin jar.

If you don't want to install the plugin in IDEA, you can open this project in IDEA and run the gradle command `runIde`
to test the plugin. A plugin tool window will appear on the right called `Project licenses`.

## Supported Licenses

The plugin supports 16 licenses. A specially trained machine learning model (ML) and the Sørensen-Dies coefficient (DSC)
are using to detect 12 licenses. The other four licenses are detecting only using the Sørensen-Dies coefficient.
Supported licenses and detection methods are showing in the table below.

| License                                                  | Detecting method |
| :------------------------------------------------------- | :--------------: |
| GNU Affero General Public License v3.0 only              | ML + DSC         |
| Apache License 2.0                                       | ML + DSC         |
| BSD 2-Clause "Simplified" License                        | ML + DSC         |
| BSD 3-Clause "New" or "Revised" License                  | ML + DSC         |
| Common Development and Distribution License 1.0          | DSC              |
| Eclipse Public License 1.0                               | DSC              |
| GNU General Public License v2.0 only                     | ML + DSC         |
| GNU General Public License v2.0 with classpath exception | DSC              |
| GNU General Public License v3.0 only                     | ML + DSC         |
| ISC License                                              | ML + DSC         |
| GNU Lesser General Public License v2.1 only              | ML + DSC         |
| GNU Lesser General Public License v3.0 only              | ML + DSC         |
| MIT License                                              | ML + DSC         |
| Mozilla Public License 1.1                               | DSC              |
| Mozilla Public License 2.0                               | ML + DSC         |
| Do What The F*** You Want To Public License              | ML + DSC         |

## User Interface

The plugin provides the user with a graphical interface for the convenience of managing licenses. The main graphical
interface of the plugin is the *Tool Window*. The window contains two tabs and provides the information about all the
licenses in the project. The first tab is called *Project License* and is presented below. It contains the information
about the detected main license of the project (root module), its description (permissions, limitations, and conditions)
, as well as a list of detected potential license violations. From this tab, the user also can export a JSON report with
all the data.

![Project License Window](https://github.com/JetBrains-Research/license-detector-plugin/raw/main/docs/pictures/ProjectLicenseWindow.png)

The second tab is called *Package Licenses* and is presented below. This tab contains the information about all the
licenses of libraries used inside project, it supports a search among all the libraries and filtering by modules.

![Package Licenses Window](https://github.com/JetBrains-Research/license-detector-plugin/raw/main/docs/pictures/PackageLicensesWindow.png)

In addition to the Tool Window, the plugin provides the *License Editor Notification* panel, presented below. This panel
appears at the top of the editor when the module license is opened in it. The panel allows the user to change the
license in several simple clicks using the drop-down menu, while indicating which licenses are compatible with all the
licenses of the module's libraries. The panel also provides an opportunity to compare the current text of the license
file with the original text of the license to check for possible differences.

![License Editor Notification](https://github.com/JetBrains-Research/license-detector-plugin/raw/main/docs/pictures/LicenseEditorNotification.png)

Also, the plugin adds a new item into the IDE's *Create new file...* menu, called *Project License File*. If the project
has no license, the user can use this functionality, and the plugin will detect the licenses compatible with all the
project's libraries, and suggest the most permissive one to the user. This way, even the most inexperienced user can
manage their licenses and not make mistakes. In the future, the user can always change their license using the *License
Editor Notification* panel described above.

![Create Module License File](https://github.com/JetBrains-Research/license-detector-plugin/raw/main/docs/pictures/CreateModuleLicenseFile.png)

Finally, the developed plugin provides another convenient way of viewing the licenses of libraries by providing hints
right in the build system script. A hint with the name of the license of a given library appears next to each command
that connects the library, as shown below. These hints allow the user to keep the licenses in mind when adding new
libraries directly in the editor. The hints are implemented for Maven, Groovy Gradle, and Kotlin Gradle scripts.

![License Inlays Hints](https://github.com/JetBrains-Research/license-detector-plugin/raw/main/docs/gif/LicenseInlaysHints.gif)

## Contacts

If you have any questions or suggestions about the plugin, feel free to contact Yaroslav Golubev in Telegram (@areyde).
