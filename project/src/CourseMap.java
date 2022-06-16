import java.util.ArrayList;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class CourseMap {
    String faculty;
    String program;
    String year;

    List<Object> full_list = new ArrayList<Object>();

    public CourseMap(String faculty, String program, String year){
        this.faculty = faculty;
        this.program = program;
        this.year = year;
    }

    public List<Object> get_full_list(){
        System.setProperty("webdriver.chrome.driver","lib/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        WebDriver driver = new ChromeDriver(options);    

        get_program_core(driver);
        driver.quit(); 
        
        return full_list;
    }

    public void get_program_core(WebDriver driver){
        String cal_url = String.format("https://%s.calendars.students.yorku.ca/%s/programs/%s/%s", this.year, this.year, this.faculty, this.program.replaceAll(" ", "-"));
        String core_xpath = "//h3[contains(text(),'Program Core')]/following-sibling::ul[1]";
        
        driver.get(cal_url);
        String program_core = driver.findElement(By.xpath(core_xpath)).getAttribute("textContent");

        //insert missing ; after LE/EECS 2030 3.00 and remove . at end of SC/MATH 1310 3.00.
        StringBuilder core = new StringBuilder(program_core);
        core.insert(164, ";");
        core.deleteCharAt(core.length()-1);

        String bullet = "- ";
        add_full_list(core.toString(), bullet, driver);
        driver.quit(); 
    }

    public void add_full_list(String string, String bullet, WebDriver driver){
        String[] split_line = string.split(";");
        
        for(String course_name: split_line){
            String[] split_courses = course_name.split("[or,]+\s");
            
            if(split_courses.length > 1){
                List<Course> tmp_list = new ArrayList<Course>();
                StringBuilder tmp_line = new StringBuilder();
                for(String course : split_courses){
                    Course tmp_course = new Course(bullet, course);
                    tmp_list.add(tmp_course);
                    tmp_line.append(tmp_course.name + "or ");
                }                
                tmp_line.setLength(tmp_line.length() - 3);
                full_list.add(tmp_list);
            }
            else{
                Course tmp_course = new Course(bullet, split_courses[0]);
                full_list.add(tmp_course);
                tmp_course.get_prerequistes(driver);
            }
        }
        driver.quit(); 
    }

}

