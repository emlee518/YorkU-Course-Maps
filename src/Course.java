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

    public void get_prerequistes(WebDriver driver){
        String ss = "FW";
        String ay = "2021";
        String prereq_url = String.format("https://w2prod.sis.yorku.ca/Apps/WebObjects/cdm.woa/wa/crsq?fa=%s&sj=%s&cn=%s&cr=%s&ay=%s&ss=%s", this.faculty, this.subject, this.code, this.credit, ay, ss);
        driver.get(prereq_url);

        String body = driver.findElement(By.tagName("body")).getText();

        if(!body.contains("4U")){
            Matcher matcher = Pattern.compile("Prerequisite[s]?: (.*?)[.]\s").matcher(body);
            List<String> prereq_list = new ArrayList<String>();
            
            while(matcher.find()){  
                String prereqs = matcher.group(1);
                String[] prereq_arr = prereqs.split("[;,]\s");
                prereq_list.addAll(Arrays.asList(prereq_arr));

                if(prereq_list.get(0).contains("GPA")){
                    prereq_list.remove(0);
                }
            }
            if(!prereq_list.isEmpty()){
                this.children = "     -> " + String.join("\n     -> ", prereq_list);
            }
        }
    }
}