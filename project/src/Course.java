import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Course {
    String bullet; 
    String name;

    String faculty;
    String subject;
    String code;
    String credit;
    String children = "";    

    public Course(String bullet, String name){
        this.bullet = bullet;
        this.name = name;

        String[] parts = name.split("[\s/]+");
        this.faculty = parts[0];
        this.subject = parts[1];
        this.code = parts[2];
        this.credit = parts[3];
    }
    
    public void get_prerequistes(WebDriver driver, String space){
        String ss = "FW";
        String ay = "2022";
        String prereq_url = String.format("https://w2prod.sis.yorku.ca/Apps/WebObjects/cdm.woa/wa/crsq?fa=%s&sj=%s&cn=%s&cr=%s&ay=%s&ss=%s", this.faculty, this.subject, this.code, this.credit, ay, ss);
        driver.get(prereq_url);

        get_prereqs(driver, space);
    }

    public void get_prereqs(WebDriver driver, String space){
        String body = driver.findElement(By.tagName("body")).getText();
        Matcher prereq_matcher = Pattern.compile("Prerequisite[s]?: (.*?)[.](?!\\d)").matcher(body);

        if(!body.contains("4U") && prereq_matcher.find()){ 
            String prereqs = prereq_matcher.group(1);
            String[] prereq_arr = prereqs.split("; |, |and ");
            List<String> prereq_list = new ArrayList<String>(Arrays.asList(prereq_arr));

            prereq_list.removeIf(pl -> !pl.matches("^.*([A-Z]{2}\\/[A-Z]{3,4} \\d{4} \\d.00).*$"));
            if(!prereq_list.isEmpty()){
                this.children = space + String.join("\n" + space, prereq_list);
            }
        }
    }

    public void add_children(String bullet, String child){
        this.children += bullet + child;
    }

    public void replace_prereq(String bullet, String child){
        this.children = bullet + child;
    }
}