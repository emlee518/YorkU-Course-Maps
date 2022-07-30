# Visualization of Course Maps @ YorkU

A visualization tool that scrapes data from the official [York Academic Calendar](https://calendars.students.yorku.ca/) and [York Courses Website](https://w2prod.sis.yorku.ca/Apps/WebObjects/cdm) so that students can be better informed when making course decisions. 

## Resources

- [Visual Studio Code - Coding Pack for Java](https://code.visualstudio.com/docs/languages/java)
- [JavaFX SDK](https://gluonhq.com/products/javafx/)
- [Selenium with Java](https://www.selenium.dev/downloads/)
- [JSON Simple](https://code.google.com/archive/p/json-simple/downloads)
- [slf4j-simple](https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/)
- [ChromeDriver](https://chromedriver.chromium.org/downloads)
- [Chrome Browser](https://www.slimjet.com/chrome/google-chrome-old-version.php)


## Setup

1. Install Visual Studio Code - Coding Pack for Java
2. Download and extract JavaFX SDK
3. Download and extract Selenium with Java
4. Download JSON Simple and slf4j-simple
5. Download ZIP of code and open project
6. Import downloaded files
7. Add JavaFX runtime components:
    
        "vmArgs": "--module-path <path-to-JavaFX-SDK-lib> --add-modules javafx.controls,javafx.fxml"

8. Downgrade Chrome Browser if needed

## Running

1. Execute project/src/Main.java to start application
2. Select faculty, program, and school year to create a course map



