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
        (Comparator.comparing((String s) -> s.charAt(s.indexOf(" ") + 1)).thenComparing(s -> s.substring(3, 7).trim()).thenComparing(Comparator.comparing(s -> s.substring(7, 12))));

    public CourseMap(String faculty, String program, String year){
        this.faculty = faculty;
        this.program = program;
        this.year = year;
    }

    public TreeMap<String, Object> get_full_map(){
        System.setProperty("webdriver.chrome.driver","lib/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("headless");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.61 Safari/537.36");
        options.addArguments("--incognito", "--disable-blink-features");
        options.addArguments("--incognito", "--disable-blink-features=AutomationControlled");
        options.addArguments("start-maximized");
        options.addArguments("--disable-gpu");
        options.addArguments("--allow-running-insecure-content");
        WebDriver driver = new ChromeDriver(options);    

        get_upper(driver);
        get_acad_cal(driver);
        update_list();
        driver.quit(); 

        return full_map;
    }

    public void get_acad_cal(WebDriver driver){
        String cal_url;

        if(year == "2019-2020"){
            cal_url = String.format("https://%s.calendars.students.yorku.ca/%s/programs/%s", this.year, this.year, this.program.replaceAll(" ", "-"));
        }
        else if(year == "2022-2023"){
            cal_url = String.format("https://calendars.students.yorku.ca/%s/programs/%s/%s", this.year, this.faculty, this.program.replaceAll(" ", "-"));
        }
        else{
            cal_url = String.format("https://%s.calendars.students.yorku.ca/%s/programs/%s/%s", this.year, this.year, this.faculty, this.program.replaceAll(" ", "-"));
        } 

        driver.get(cal_url);
        String[] split_line;

        if(program == "Computer Science"){
            String core_xpath = "//h3[contains(text(),'Program Core')]/following-sibling::ul[1]";
            String major_reqs_xpath = "//h5[contains(text(),'Honours Major Program')]/following-sibling::ul[1]";
            String program_core = driver.findElement(By.xpath(core_xpath)).getAttribute("textContent");
            StringBuilder calendar_reqs = new StringBuilder(program_core.replace("\t", "").replace("\n", ""));

            if(year == "2019-2020"){
                //insert missing ; after LE/EECS 1012 3.00 and LE/EECS 2030 3.00, and replace . with ; at end of SC/MATH 1310 3.00.
                calendar_reqs.insert(35, ";");
                calendar_reqs.insert(143, ";");
                calendar_reqs.setCharAt(calendar_reqs.length()-1, ';');
            }
            else{
               //insert missing ; after LE/EECS 2030 3.00 and replace . with ; at end of SC/MATH 1310 3.00.
               calendar_reqs.insert(164, ";");
               calendar_reqs.setCharAt(calendar_reqs.length()-1, ';');
            }

            String major_reqs = driver.findElement(By.xpath(major_reqs_xpath)).getAttribute("textContent");
            Matcher matcher = Pattern.compile("([A-Z]{2}\\/[A-Z]{4} \\d{4} \\d.00)(.*?);").matcher(major_reqs);

            while(matcher.find()){  
                String req = matcher.group();
                calendar_reqs.append(req);
            }

            split_line = calendar_reqs.toString().split("; |;");
        }
        else{
            String core_xpath = "//h5[contains(text(),'Program Core')]/following-sibling::ul[1]";
            String major_reqs_xpath = "//h3[contains(text(),'Program Requirements')]/following-sibling::ul[1]";
            String general_xpath = "//strong[contains(text(),'General Stream')]/following-sibling::ol[1]/li[1]";
            String program_core = driver.findElement(By.xpath(core_xpath)).getAttribute("textContent");
            StringBuilder calendar_reqs = new StringBuilder(program_core.replace("\t", "").replace("\n", ""));
            
            if(year == "2022-2023"){
                //remove extra ** after LE/ENG 4000 6.00 and text at end of list, and extra space at start
                calendar_reqs.deleteCharAt(0);
                calendar_reqs.delete(163, 165);
                calendar_reqs.delete(337, calendar_reqs.length());
            }
            else{
                //remove extra ** after LE/ENG 4000 6.00 and text at end of list
                calendar_reqs.delete(161, 163);
                calendar_reqs.delete(332, calendar_reqs.length());
            }
            
            String major_reqs = driver.findElement(By.xpath(major_reqs_xpath)).getAttribute("textContent").replace("\t", "").replace("\n", "");
            Matcher matcher = Pattern.compile("([A-Z]{2}\\/[A-Z]{4} \\d{4} \\d.00)(.*?)[a-z]").matcher(major_reqs);
                        
            while(matcher.find()){  
                String req = matcher.group();
                calendar_reqs.append(req);
            }          

            //remove text at end
            if(year == "2022-2023"){
                calendar_reqs.delete(713, calendar_reqs.length());
            }
            else{
                calendar_reqs.delete(708, calendar_reqs.length());
            }

            String general_stream = driver.findElement(By.xpath(general_xpath)).getAttribute("textContent");
            calendar_reqs.append(general_stream);
            split_line = calendar_reqs.toString().split("; |;|, ");
        }
        
        String bullet = "- ";
        add_full_map(split_line, bullet, driver);        
    }

    public void add_full_map(String[] split_line, String bullet, WebDriver driver){        
        for(String course_name: split_line){
            String[] split_courses = course_name.split(", | or ");
            
            if(split_courses.length > 1){
                List<Course> tmp_list = new ArrayList<Course>();
                StringBuilder tmp_line = new StringBuilder();
                for(String course : split_courses){
                    Course tmp_course = new Course(bullet, course);
                    tmp_course.get_prerequistes(driver, "          -> ");
                    tmp_list.add(tmp_course);
                    tmp_line.append(tmp_course.name + " or ");
                }                
                tmp_line.setLength(tmp_line.length() - 4);
                full_map.put(tmp_line.toString(), tmp_list);
            }
            else{
                Course tmp_course = new Course(bullet, split_courses[0]);
                tmp_course.get_prerequistes(driver, "     -> ");
                full_map.put(split_courses[0], tmp_course);
            }
        }
    }
    
    public void get_upper(WebDriver driver){
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

        List<WebElement> upper_table;
        if (program == "Computer Science"){
            upper_table = driver.findElements(By.xpath("//table[2]/tbody/tr[152]/following-sibling::tr"));
        }
        else{
            upper_table = driver.findElements(By.xpath("//table[2]/tbody/tr[75]/following-sibling::tr"));
        }

        for(WebElement element: upper_table){
            String text = element.getText();

            if(text.contains("Present")){
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
    }

    public void update_list(){        
        try {
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(new FileReader("src/update_lists/" + program.replaceAll(" ", "_") + "_Update_List.json"));
            JSONArray update_arr = (JSONArray) object.get("children");
            Iterator<?> update_it = update_arr.iterator();

            while (update_it.hasNext()) {
                JSONObject update_obj = (JSONObject) update_it.next();
                String course_str = update_obj.get("name").toString();

                if (update_obj.containsKey("list")){
                    List<Course> tmp_list = (List<Course>) full_map.get(course_str);
                    JSONArray tmp_arr = (JSONArray) update_obj.get("list");
                    Iterator<?> tmp_it = tmp_arr.iterator();

                    while(tmp_it.hasNext()){
                        JSONObject tmp_obj = (JSONObject) tmp_it.next();
                        int index = Integer.parseInt(tmp_obj.get("index").toString());
                        Course tmp_course = tmp_list.get(index);
                        String space = "     ";
                        update_child(tmp_course, tmp_obj, space);
                    }
                }
                else{
                    Course course = (Course) full_map.get(course_str);
                    update_child(course, update_obj, "");
                }              
            }

            if(object.containsKey("add_course")){
                JSONArray add_arr = (JSONArray) object.get("add_course");
                Iterator<?> add_it = add_arr.iterator();

                while (add_it.hasNext()) {
                    JSONObject add_obj = (JSONObject) add_it.next();
                    String bullet = add_obj.get("bullet").toString();
                    String name = add_obj.get("name").toString();
                    Course tmp_course = new Course(bullet, name);
                    update_child(tmp_course, add_obj, "");
                    full_map.put(name, tmp_course);
                }
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update_child(Course course, JSONObject jobj, String space){
        if(jobj.containsKey("alt")){
            course.add_children("     = ", jobj.get("alt").toString());
            if(jobj.containsKey("prereq")){
                course.add_children("\n     -> ", jobj.get("prereq").toString());
            }
        }
        else if(jobj.containsKey("prereq")){
            String[] prereq_split = jobj.get("prereq").toString().split("; ");
            String prereq_join = String.join("\n" + space + "     -> ", prereq_split);
            course.add_children("     -> ", prereq_join);
        }
        else if(jobj.containsKey("replace")){
            String[] replace_split = jobj.get("replace").toString().split("; ");
            String replace_join = String.join("\n" + space + "     -> ", replace_split);
            course.replace_prereq(space + "     -> ", replace_join);
        }  
    }    
}

