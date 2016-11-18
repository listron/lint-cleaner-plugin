Removes unused resources reported by Android lint including strings, colors and dimensions,styles,drawable .

## Usage

Apply the plugin in your `build.gradle`:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.android.tools.build:gradle:2.1.0'
    classpath 'com.droidtitan:lint-cleaner-plugin:0.3.0'
  }
}

apply plugin: 'android'
apply plugin: 'com.droidtitan.lintcleaner'
```


Finally, to remove unused resources use: 
     
    gradle lintClean

## Optional Configuration using DSL

```groovy
    lintCleaner {
        // Exclude specific files(layout,drawable,anim ...)
        excludeFile = ['activity_animated_demo_layout.xml','address_logo.png']

        // Exclude specifix lines (string.xml,color.xml,style.xml ...)
        excludeLine = ['color_f4f4f4','dispatch_driverarrive_desc','LoginCommitLayout']

        // Ability to ignore all the line res(string.xml,color.xml,style.xml ...). False by default.
        ignoreResFiles = true

        // Default path is build/outputs/lint-results.xml
        lintXmlFilePath = 'build/outputs/lint-results-debug.xml'
    }
```
