import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("EECS 4080 Project");
    
        Label title = new Label("YorkU Course Maps");
        title.setStyle("-fx-padding: 0 0 120 0; -fx-font-size: 50;");

        Button start_button = new Button("Start");
        start_button.setPrefSize(100, 20);
        start_button.setStyle("-fx-font-size: 20;");
        start_button.setOnAction(e -> create_map(primaryStage));

        VBox center_box = new VBox();
        center_box.getChildren().add(title);
        center_box.getChildren().add(start_button);
        center_box.setAlignment(Pos.CENTER);

        Scene start_scene = new Scene(center_box, 700, 500);
        primaryStage.setScene(start_scene);
        primaryStage.show();
    }

    void create_map(Stage primaryStage){
        Label select_faculty = new Label("Select Faculty:");
        select_faculty.setStyle("-fx-font-size: 17;");

        Label select_program = new Label("Select Program:");
        select_program.setStyle("-fx-font-size: 17;");

        Label select_school_year = new Label("Select School Year:");
        select_school_year.setStyle("-fx-font-size: 17;");

        ComboBox<String> faculty = new ComboBox<>();
        faculty.setStyle("-fx-font-size: 15;");
        faculty.setPrefSize(200, 15);
        faculty.getItems().addAll("Lassonde - LE");

        ComboBox<String> program = new ComboBox<>();
        program.setStyle("-fx-font-size: 15;");
        program.setPrefSize(200, 15);
        program.getItems().addAll("Computer Science", "Software Engineering");

        ComboBox<String> school_year = new ComboBox<>();
        school_year.setStyle("-fx-font-size: 15;");
        school_year.setPrefSize(200, 15);
        school_year.getItems().addAll("2019-2020", "2020-2021", "2021-2022" );

        Button create_map_button = new Button("Create Map");
        create_map_button.setStyle("-fx-font-size: 20;");

        Hyperlink back = new Hyperlink("Back");
        back.setStyle("-fx-text-fill: black; -fx-font-size: 15;");

        GridPane info_grid = new GridPane();
        info_grid.add(select_faculty, 0, 0);
        info_grid.add(faculty, 1, 0);
        info_grid.add(select_program, 0, 1);
        info_grid.add(program, 1, 1);
        info_grid.add(select_school_year, 0, 2);
        info_grid.add(school_year, 1, 2);
        info_grid.setVgap(20);
        info_grid.setHgap(50);
        info_grid.setAlignment(Pos.CENTER);
        info_grid.setPadding(new Insets(0, 0, 100, 0)); 

        VBox center_box = new VBox();
        center_box.getChildren().add(info_grid);
        center_box.getChildren().add(create_map_button);
        center_box.setAlignment(Pos.CENTER);
        
        HBox back_box = new HBox();
        back_box.getChildren().add(back);
        back_box.setAlignment(Pos.BOTTOM_LEFT);
        back_box.setPadding(new Insets(5));

        BorderPane create_map_pane = new BorderPane();
        create_map_pane.setCenter(center_box);
        create_map_pane.setBottom(back_box);

        Scene create_map_scene = new Scene(create_map_pane, 700, 500);
        primaryStage.setScene(create_map_scene);    
    }

    public static void main(String[] args){
        launch(args);
    }
}

