# Setting up Code Style Check with Checkstyle

To set up the **Checkstyle plugin** for code checking in a Gradle-based project, follow these steps:

---

## Step 1: Add the Checkstyle Plugin

Add the following configuration to the `build.gradle` file under the `plugins` section:

```gradle
plugins {
    id 'checkstyle'
}
```

---

## Step 2: Configure the Checkstyle Plugin

Add the Checkstyle configuration within the `build.gradle` file:

```gradle
checkstyle {
    toolVersion = '10.23.0' // Specify the desired Checkstyle version
    configFile = file("${rootProject.projectDir}/checkstyle.xml") // Path to the Checkstyle configuration file
    showViolations = true // Show violations in the build output
    ignoreFailures = false // Fail the build if violations are detected
}
```

---

## Step 3: Configure Checkstyle Task

Append a task configuration for all `Checkstyle` tasks to properly customize report generation:

```gradle
tasks.withType(Checkstyle).configureEach {

    // Define base directory for configuration properties
    conventionMapping.map("configProperties") {
        ["basedir": rootDir]
    }

    reports {
        // Disable XML report, enable and configure HTML report
        xml.required = false
        html.required = true
        html.outputLocation = layout.buildDirectory.file("/reports/checkstyle/checkstyleReport.html")
    }
}
```

---

## Step 4: Add Configuration Files to the Project Root

To define rules and suppressions, ensure the following configuration files are added to the **root directory** of your
project:

1. `import-control.xml` – Specifies rules for organizing imports.
2. `checkstyle-suppressions.xml` – Defines exceptions and suppressions for specific checks.
3. `checkstyle.xml` – Main configuration file containing all Checkstyle rules.
4. `.editorconfig` – Configuration file for IDE/editor formatting rules.

**Note:** Each file should be customized based on **project-specific requirements** and its **coding standards**.

---

## Step 5: Run Checkstyle

The Checkstyle verification will automatically be executed during the project build process. However, it can also be
triggered manually using the following Gradle command:

```bash
gradle clean build 
```

This will generate a Checkstyle report in the following directory:

<project-root>/build/reports/checkstyle/checkstyleReport.html


## Step 6: Setup Editor Configuration

1.  Navigate to IDE settings: `File` -> `Settings` (or `IntelliJ IDEA` -> `Preferences` on macOS).
2.  Go to the `Editor` -> `Code Style` section.
3.  In the `Scheme` dropdown list, select **Project**. This ensures that the project-specific code style scheme (often defined in the `.editorconfig` file) is used.
4.  Click `Apply` or `OK` to save the changes.
5.  Then, **restart the IDE** to ensure all settings are applied correctly, especially if changes were made to the `.editorconfig` file.

---


## Important Notes

1. **Customization**: Each Checkstyle-related file (`checkstyle.xml`, `import-control.xml`, etc.) and `.editorconfig`
   must be tailored to meet the specific coding standards and requirements of the project.
2. **Reports**: Checkstyle will produce an HTML report (by the configuration above) that lists violations for easy
   access.
3. **Integration**: Integrating Checkstyle ensures code consistency and adherence to style guidelines across the team.

---

By following the above steps, you can effectively incorporate code style checks into your Gradle project and maintain
consistent coding standards.
