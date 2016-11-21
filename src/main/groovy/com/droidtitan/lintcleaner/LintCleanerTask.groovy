package com.droidtitan.lintcleaner

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList

import javax.xml.parsers.DocumentBuilderFactory

class LintCleanerTask extends DefaultTask {
  static final String NAME = "lintClean"

  final String LINE_SEPARATOR = System.getProperty("line.separator")
  final String UNUSED_RESOURCES_ID = "UnusedResources"
  final String WRITER_ENCODING = "UTF-8"
  final String ARRAY_XML_TAG = "array"
  final String STYLE_XML_TAG = "style"
  final String LAYOUT_XML_TAG = "/layout"
  final String DRAWABLE_XML_TAG = "/drawable"
  final String COLOR_XML_TAG = "/color/"
  final String ANIM_XML_TAG = "anim"
  final String FILE_PATH_XML_TAG = "file"
  final String ID_XML_TAG = "id"
  final String ISSUE_XML_TAG = "issue"
  final String LINE_XML_TAG = "line"
  final String LOCATION_XML_TAG = "location"

  final Map<String, List<String>> filePathToLines = new HashMap<String, ArrayList<String>>()
  List<String> excludeFile
  List<String> excludeLine
  String lintXmlFilePath
  boolean ignoreResFiles

  LintCleanerTask() {
    group = LintCleanerPlugin.GROUP
    description = "Removes unused resources reported by the Android Plugin lint task"
  }

  @TaskAction void removeUnusedResources() {
    def lintFile = new File(getLintXmlFilePath())
    if (!lintFile.exists()) {
      println "$lintFile.absolutePath is not a lint xml file"
      return
    }

    def builderFactory = DocumentBuilderFactory.newInstance()
    Document lintDocument = builderFactory.newDocumentBuilder().parse(lintFile)
    excludeFile = getExcludeFile()
    org.w3c.dom.NodeList issues = lintDocument.getElementsByTagName(ISSUE_XML_TAG)
    processIssues(issues)

    if (!getIgnoreResFiles()) {
      removeUnusedLinesInResFiles()
    }
  }

  void processIssues(org.w3c.dom.NodeList issues) {
    issues.each {
      Element issue = it as Element

      if (issue.getAttribute(ID_XML_TAG).equals(UNUSED_RESOURCES_ID)) {
        org.w3c.dom.NodeList locations = issue.getElementsByTagName(LOCATION_XML_TAG)

        if (locations.length == 1) {
          processLocation(locations.item(0) as Element)
        } else {
          locations.each {
            processLocation(it as Element)
          }
        }
      }
    }
  }

  /** Removes unused files or adds filePath to map for processing by @removeUnusedLinesInResFiles. */
  void processLocation(Element location) {
    String line = location.getAttribute(LINE_XML_TAG)
    String filePath = location.getAttribute(FILE_PATH_XML_TAG)
    String fileName = filterFileName(filePath)

    boolean isLayout = filePath.contains(LAYOUT_XML_TAG)
    boolean isDrawable = filePath.contains(DRAWABLE_XML_TAG)
    boolean isAnim = filePath.contains(ANIM_XML_TAG)
    boolean isColor = filePath.contains(COLOR_XML_TAG)
    if (line.empty || isLayout || isDrawable || isAnim ||isColor) {
      if(excludeFile.contains(fileName)){
        return;
      }
	  File file = new File(filePath)
      file.delete()
      println "Removed $file.name"
    } else {
      List<String> lineNumbers = filePathToLines.get(filePath)
      lineNumbers = lineNumbers ? lineNumbers : new ArrayList<String>()
      lineNumbers.add(line)
      filePathToLines.put(filePath, lineNumbers)
    }
  }

  String filterFileName(String path){
    File sourceFile = new File(path)
    return sourceFile.name;
  }

  /** Removes unused resources from single files like strings.xml, color.xml etc. */
  void removeUnusedLinesInResFiles() {
    filePathToLines.each { filePath, unusedLines ->
      File sourceFile = new File(filePath)

      if (excludeFile.contains(sourceFile.name)) {
        return
      }

      def sourceDir = sourceFile.getParentFile().toString()
      File tempFile = new File("${sourceDir}/${sourceFile.name}bak")

      tempFile.withWriter(WRITER_ENCODING) { writer ->
        int index = 1
        boolean removingArray = false
        boolean removingStyle = false
	    sourceFile.eachLine { line ->

          String lineNumber = Integer.toString(index)

          if ((unusedLines.contains(lineNumber) && !isExcluded(line)) || removingArray ||removingStyle) {
            if (line.contains(ARRAY_XML_TAG)) {
              removingArray = !removingArray
            }
            if(line.contains(STYLE_XML_TAG)){
              removingStyle = !removingStyle
            }
          } else {
            writer << line + LINE_SEPARATOR
          }
          index++
        }
      }

      sourceFile.setWritable(true)
      sourceFile.delete()
      if (tempFile.renameTo(sourceFile)) {
        printEntryRemovalCount(sourceFile, unusedLines.size())
      } else {
        tempFile.delete()
        println "Failed to remove entries from $sourceFile.name"
      }
    }
  }

  boolean isExcluded(String line){
    for(String s:excludeLine){
      if(line.contains(s)){
        return true;
      }
    }
    return false;
  }

  static void printEntryRemovalCount(File file, int count) {
    println "Removed $count ${count == 1 ? "entry" : "entries"} from $file.name"
  }
}
