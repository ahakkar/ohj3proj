package fi.Sisu.app;

import java.io.IOException;

import fi.Sisu.datasource.ApiDataSource;
import fi.Sisu.datasource.FileDataSource;
import fi.Sisu.datasource.IApiDataSource;
import fi.Sisu.datasource.IFileDataSource;
import fi.Sisu.navigation.NavigationService;
import fi.Sisu.navigation.ScreenFactory;
import fi.Sisu.navigation.ScreenType;

import javafx.application.Application;

import javafx.stage.Stage;

/**
 * Main class for the application, defined at pom.xml
 * 
 * @author Antti Hakkarainen
 * @author Heikki Hohtari
 */
public class MainApplication extends Application {

    private NavigationService navigationService;
   
    /**
     * Launches the JavaFX application.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Application.launch(MainApplication.class);
    }

    /**
     * Initializes a MVVM project.
     * 
     * ModelFactory creates and contains instances of business logic classes.
     * 
     * ScreenManager creates and contains ScreenObjects which contain a
     * View, ViewModel and Controller. For every screen (I.E Student,
     * Main, Settings, etc) one ScreenObject is created.
     * 
     * - View contains the loaded .fxml file, representing a single screen.
     * - ViewModel works as API between Controller and Business Logic.
     * - Controller handles events from user and passes them on to ViewModel.
     * 
     * AppState is responsible for keeping track of the current state of the
     * application, like which student is currently selected.
     */
    @Override
    public void init() throws IOException {
        // Initialize components
        IApiDataSource apiDataSource = new ApiDataSource();
        IFileDataSource fileDataSource = new FileDataSource();
        AppState appState = new AppState();
        ScreenFactory screenFactory = new ScreenFactory(apiDataSource, fileDataSource);        
        navigationService = new NavigationService(appState, screenFactory);        
    }


    /**
     * Sets up the primaryStage with a new scene, and tasks the NavigationService
     * to load the initial user view consisting of:
     * - MainView (static container which is always visible)
     * - a subview, initially consisting of ChooseStudentView
     * 
     * NavigationService then manages the switches between subviews, based on user
     * input.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set up the scene with the first screen's view
        navigationService.changeScene(primaryStage, ScreenType.MAIN_SCREEN);
        
        // loads an initial subscene in addition to the static main screen
        navigationService.changeSubScene(ScreenType.CHOOSE_STUDENT_SCREEN);



    }
}
