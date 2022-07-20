import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.util.List;
import java.util.TreeMap;

public class Main extends Application{
 
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("EECS 4080 Project");
    
        Label title = new Label("YorkU Course Maps");
        title.setId("title");

        Button start_button = new Button("Start");
        start_button.setId("buttons");

        VBox center_box = new VBox(title, start_button);
        center_box.setId("center_box");

        Scene start_scene = new Scene(center_box, 800, 550);
        start_scene.getStylesheets().add("style.css");
        
        start_button.setOnAction(e -> create_map(primaryStage, start_scene));
        
        primaryStage.setScene(start_scene);
        primaryStage.show();
    }

    public void create_map(Stage primaryStage, Scene previous){
        Label select_faculty = new Label("Select Faculty:");
        select_faculty.setId("labels");

        Label select_program = new Label("Select Program:");
        select_program.setId("labels");

        Label select_school_year = new Label("Select School Year:");
        select_school_year.setId("labels");

        ComboBox<String> faculty = new ComboBox<>();
        faculty.getItems().addAll("Lassonde - LE");
        faculty.setId("combobox");

        ComboBox<String> program = new ComboBox<>();
        program.setId("combobox");

        ObservableList<String> le_programs = FXCollections.observableArrayList();
        le_programs.addAll("Computer Science", "Software Engineering");
        
        faculty.setOnAction(e -> {
            if(faculty.getValue() == "Lassonde - LE"){
                program.setItems(le_programs);
            }
        });

        ComboBox<String> school_year = new ComboBox<>();
        school_year.getItems().addAll("2019-2020", "2020-2021", "2021-2022");
        school_year.setId("combobox");

        Button create_map_button = new Button("Create Map");
        create_map_button.setId("buttons");

        Hyperlink back = new Hyperlink("Back");
        back.setOnAction(e -> back(primaryStage, previous));
        back.setId("hyperlink");

        GridPane info_grid = new GridPane();
        info_grid.add(select_faculty, 0, 0);
        info_grid.add(faculty, 1, 0);
        info_grid.add(select_program, 0, 1);
        info_grid.add(program, 1, 1);
        info_grid.add(select_school_year, 0, 2);
        info_grid.add(school_year, 1, 2);
        info_grid.setVgap(20);
        info_grid.setHgap(50);
        info_grid.setId("info_grid");

        VBox center_box = new VBox(info_grid, create_map_button);
        center_box.setId("center_box");
        
        HBox back_box = new HBox(back);
        back_box.setId("back_box");

        BorderPane create_map_pane = new BorderPane();
        create_map_pane.setCenter(center_box);
        create_map_pane.setBottom(back_box);

        Scene create_map_scene = new Scene(create_map_pane, 800, 550);
        create_map_scene.getStylesheets().add("style.css");

        create_map_button.setOnAction(e -> {
            if(faculty != null && program != null && school_year.getValue() != null){
                String fa = faculty.getValue().substring(faculty.getValue().length() - 2);
                course_map(primaryStage, create_map_scene, fa, program.getValue(), school_year.getValue());
            }
        });

        primaryStage.setScene(create_map_scene);    
    }

    public void course_map(Stage primaryStage, Scene previous, String faculty, String program, String year){
        Label course_map_title = new Label(program + " Course Map");
        course_map_title.setId("program_title");

        TextArea cmap = new TextArea();
        cmap.setEditable(false);
        cmap.setId("cmap");

        Label course_types = new Label("- Required          = Alternative          -> Prerequisites          + Upper year");
        course_types.setId("course_types");    

        Hyperlink back = new Hyperlink("Back");
        back.setOnAction(e -> back(primaryStage, previous));
        back.setId("hyperlink");

        Button save = new Button("Save");
        save.setOnAction(e -> {
            if(!cmap.getText().isEmpty()){
                save(primaryStage, cmap.getText(), program);
            }
        });
        save.setId("save");


        HBox top_box = new HBox(course_map_title, save);
        top_box.setId("top_box");

        HBox bottom_box = new HBox(back, course_types);
        bottom_box.setId("bottom_box");

        BorderPane course_map_pane = new BorderPane();
        course_map_pane.setTop(top_box);
        course_map_pane.setCenter(cmap);
        course_map_pane.setBottom(bottom_box);

        Scene course_map_scene = new Scene(course_map_pane, 800, 550);
        course_map_scene.getStylesheets().add("style.css");
        primaryStage.setScene(course_map_scene);
        
        CourseMap course_map = new CourseMap(faculty, program, year);
        Platform.runLater(() -> {
            TreeMap<String, Object> full_map = course_map.get_full_map();
            print_course_map(cmap, full_map);
        });
    }

    public void print_course_map(TextArea cmap, TreeMap<String, Object> full_map){       
        full_map.forEach((course_name, obj) -> {
            if (obj.getClass().getName() == "Course"){
                Course course = (Course) obj;
                cmap.appendText(course.bullet + course.name + "\n");

                if(!course.children.isEmpty()){
                    cmap.appendText(course.children + "\n");
                }
            }
            else{
                cmap.appendText("- " + course_name + "\n");
                
                if(!course_name.contains("EECS 1")){
                    List<Course> tmp_list = (List<Course>) obj;
                    for(Course course : tmp_list){
                        cmap.appendText("     " + course.bullet + course.name + "\n");
                        cmap.appendText(course.children + "\n");
                    }  
                }
            }
        });
        cmap.setScrollTop(Double.MAX_VALUE);
    }

    public void save(Stage primaryStage, String cmap_text, String program){
        try {
            PrintWriter writer = new PrintWriter(program.replaceAll(" ", "-") + "-Course-Map.txt", "UTF-8");
            writer.println(program + " Course Map");
            writer.println("- Required \n= Alternative \n-> Prerequisites \n+ Upper year\n");
            writer.println(cmap_text);
            writer.close();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void back(Stage primaryStage, Scene previous){
        primaryStage.setScene(previous);    
    }

    public static void main(String[] args){
        launch(args);
    }
}
