<!--
@guidance:
Content Structure:
# Project Title
- Extract the project title from pom.xml and display it as the main heading.
- Immediately after the title, add a Maven Central badge as a new paragraph:  
  `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`  
  Replace `[groupId]` and `[artifactId]` with values from pom.xml.

# Overview
- Summarize the content of all `package-info.java` files found in the source folders.
- Describe the application's core functionality in a professional, non-technical style. Do not mention package names.
- Provide a concise summary outlining the project's purpose and main features.

# Installation Instructions
- List all prerequisites, including the required Java version and build tools.
- Describe multiple ways to use this plugin:
  - **Clone and Build the Project:**  
    - Provide clear, step-by-step instructions for checking out the repository and building the project with Maven.
  - **Download Assembled Jar File:**  
    - Include a direct download link for the application jar:  
      `[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/anteater/files/plugins/ai-ae-plugin.jar/download)`
    - Instruct the user to open the folder where Anteater is installed, locate the `plugins` folder, and move or copy the downloaded `.jar` file into it.
  - **Launch Anteater Desktop or CLI:**  
    - Explain that the application will automatically detect and load the plugin, making its features available.
  - **Use Maven to Manage Your Project:**  
    - Show how to add the plugin as a dependency in the project’s `pom.xml` file.
    - Explain that Maven will download and include the plugin automatically during the build process.

# Usage
- Provide usage instructions for:
  - **Code Helpers:**  
    - Describe how to use code helpers defined in `/src/main/java/com/ganteater/ae/desktop/editor`.
  - **Processors:**  
    - Describe how to use processors in `/src/main/java/com/ganteater/ae/processor`.
    - Outline a typical workflow for using the project artifacts.
    - Reference example recipes found in `src/ae/recipes`.

**Formatting Requirements:**
- Use Markdown syntax for all headings, lists, code blocks, and links.
- Ensure each section is clear, concise, and logically organized.
-->
