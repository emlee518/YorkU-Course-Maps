import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.*;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import org.json.simple.*;
import org.json.simple.parser.*;

public class CourseMap {
    String faculty;
    String program;
    String year;

    TreeMap<String, Object> full_map = new TreeMap<String, Object>
        (Comparator.comparing((String s) -> s.substring(8, 12)).thenComparing(Comparator.comparing(s -> s.substring(3, 7))));

    public CourseMap(String faculty, String program, String year){
        this.faculty = faculty;
        this.program = program;
        this.year = year;
    }

    public TreeMap<String, Object> get_full_map(){
        System.setProperty("webdriver.chrome.driver","lib/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.61 Safari/537.36");
        options.addArguments("--incognito", "--disable-blink-features");
        options.addArguments("--incognito", "--disable-blink-features=AutomationControlled");
        options.addArguments("start-maximized");
        options.addArguments("--disable-gpu");
        options.addArguments("--allow-running-insecure-content");
        WebDriver driver = new ChromeDriver(options);    

        get_academic_calendar(driver);
        get_upper(driver);
        update_list();

        driver.quit(); 

        return full_map;
    }

    public void get_academic_calendar(WebDriver driver){
        System.out.println("Getting academic calendar courses...");

        String cal_url = String.format("https://%s.calendars.students.yorku.ca/%s/programs/%s/%s", this.year, this.year, this.faculty, this.program.replaceAll(" ", "-"));
        String core_xpath = "//h3[contains(text(),'Program Core')]/following-sibling::ul[1]";
        String major_reqs_xpath = "//h5[contains(text(),'Honours Major Program')]/following-sibling::ul[1]";
        driver.get(cal_url);
        
        //insert missing ; after LE/EECS 2030 3.00 and replace . with ; at end of SC/MATH 1310 3.00.
        String program_core = driver.findElement(By.xpath(core_xpath)).getAttribute("textContent");
        StringBuilder calendar_reqs = new StringBuilder(program_core);
        calendar_reqs.insert(164, ";");
        calendar_reqs.setCharAt(calendar_reqs.length()-1, ';');

        String major_reqs = driver.findElement(By.xpath(major_reqs_xpath)).getAttribute("textContent");
        Matcher matcher = Pattern.compile("([A-Z]{2}\\/[A-Z]{4} \\d{4} \\d.00)(.*?);").matcher(major_reqs);
        
        while(matcher.find()){  
            String req = matcher.group();
            calendar_reqs.append(req);
        }

        String bullet = "- ";
        add_full_map(calendar_reqs.toString(), bullet, driver);
        System.out.println("Success!");
    }

    public void get_upper(WebDriver driver){
        System.out.println("Getting upper courses...");

        String upper_url = "https://w2prod.sis.yorku.ca/Apps/WebObjects/cdm";
        String upper_xpath = "//a[contains(text(),'Faculty, Subject, Number')]";
        driver.get(upper_url);

        WebElement hist_course = driver.findElement(By.xpath(upper_xpath));
        hist_course.click();

        WebElement faculty_select = driver.findElement(By.id("facultySelect"));
        Select faculty = new Select(faculty_select);
        faculty.selectByIndex(10);

        WebElement subject_select = driver.findElement(By.id("subjectSelect"));
        Select subject = new Select(subject_select);
        subject.selectByIndex(4);

        WebElement search = driver.findElement(By.xpath("//input[@type='submit']"));
        search.click();

        List<WebElement> upper_table = driver.findElements(By.xpath("//table[2]/tbody/tr"));

        for(WebElement element: upper_table){
            String text = element.getText();

            if(text.contains("Present") && text.contains("LE/EECS 4")){
                element.findElement(By.tagName("a")).sendKeys(Keys.chord(Keys.CONTROL, Keys.RETURN));

                ArrayList<String> tab = new ArrayList<>(driver.getWindowHandles()); 
                driver.switchTo().window(tab.get(1));

                String name = text.substring(0,17);
                Course upper_course = new Course("+ ", name);
                upper_course.get_prereqs(driver, "     -> ");
                full_map.put(name, upper_course);

                driver.close();
                driver.switchTo().window(tab.get(0));
            }
        }
        System.out.println("Success!");
    }

    public void add_full_map(String string, String bullet, WebDriver driver){
        String[] split_line = string.split(";");
        
        for(String course_name: split_line){
            String[] split_courses = course_name.split("[or,]+\s");
            
            if(split_courses.length > 1){
                List<Course> tmp_list = new ArrayList<Course>();
                StringBuilder tmp_line = new StringBuilder();
                for(String course : split_courses){
                    Course tmp_course = new Course(bullet, course);
                    tmp_course.get_prerequistes(driver, "          -> ");
                    tmp_list.add(tmp_course);
                    tmp_line.append(tmp_course.name + " or ");
                }                
                tmp_line.setLength(tmp_line.length() - 3);
                full_map.put(tmp_line.toString(), tmp_list);
            }
            else{
                Course tmp_course = new Course(bullet, split_courses[0]);
                tmp_course.get_prerequistes(driver, "     -> ");
                full_map.put(split_courses[0], tmp_course);
            }
        }
    }

    public void update_list(){  
        System.out.println("Comparing to update list...");
      
        try {
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(new FileReader("src/update_list.json"));
            JSONArray alt_arr = (JSONArray) object.get("children");
            Iterator<?> alt_it = alt_arr.iterator();

            while (alt_it.hasNext()) {
                JSONObject alt_obj = (JSONObject) alt_it.next();
                Course course = (Course) full_map.get(alt_obj.get("name"));

                if(alt_obj.containsKey("alt")){
                    course.add_children("     = ", alt_obj.get("alt").toString());
                }
                if(alt_obj.containsKey("prereq") && !course.children.isEmpty()){
                    course.add_children("\n     -> ", alt_obj.get("prereq").toString());
                }
                else if(alt_obj.containsKey("prereq")){
                    course.add_children("     -> ", alt_obj.get("prereq").toString());
                }
            }
            System.out.println("Success!");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

