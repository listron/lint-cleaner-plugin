package com.droidtitan.lintcleaner

import org.gradle.api.Project

class LintCleanerPluginExtension {

  String lintXmlFilePath
  boolean ignoreResFiles = false
  List<String> excludeFile = []
  List<String> excludeLine = []

  LintCleanerPluginExtension(Project project) {
    /** Default lint file path when user does not set it explicitly using DSL */
    this.lintXmlFilePath = "$project.buildDir/outputs/lint-results.xml"
  }
}
