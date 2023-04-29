package fi.Sisu.navigation;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import fi.Sisu.app.AppState;
import fi.Sisu.view.Controller;
import fi.Sisu.view.MainController;
import fi.Sisu.viewmodel.ViewModel;


/**
 * NavigationService is responsible for asking ScreenManager to load the
 * screens, and then switching between them when ViewModels request a screen
 * change. It also holds the current Student ID for witching between views.
 * 
 * @author Antti Hakkarainen
 * @author Heikki Hohtari
 */
public class NavigationService {

    private AppState appState;
    private ScreenFactory screenFactory;

    private ScreenType currentScreen;
    private MainController mainController;

    private static final Integer MAX_HISTORY_SIZE = 10;

    // Contains the history of screens that have been shown.
    private Deque<ScreenType> screenHistory = new ArrayDeque<>();

    /**
     * Constructor for NavigationService
     * 
     * @param screenFactory ScreenFactory object
     * @throws IOException
     */
    public NavigationService(AppState appState, ScreenFactory screenFactory) throws IOException {
        this.appState = appState;
        this.screenFactory = screenFactory;
        this.mainController = null; 
    }


    /**
     * Registers a screen change listener between a ViewModel and NavigationService.
     * 
     * @param viewModel ViewModel that is listened to.
     * @return void
     */
    private void addScreenChangeListener(ViewModel viewModel) {
        viewModel.requestedScreenProperty().addListener((obs, oldScreen, newScreen) -> {
            if (newScreen != null) {
                try {
                    changeSubScene(newScreen);
                } catch (IOException e) {
                    System.err.println("Error changing subscene to " + newScreen + ".");
                    e.printStackTrace();
                }
                // Reset the requested screen to null after handling the change
                viewModel.setRequestedScreen(null);
            }
        });
    }


    /**
     * Changes the main scene of the application based on the given screenType enum.
     * 
     * @param primaryStage
     * @param screenType
     * @throws IOException
     */
    public void changeScene(Stage primaryStage, ScreenType screenType) throws IOException {
        ScreenObject newScreen = screenFactory.createScreen(appState, ScreenType.MAIN_SCREEN);
        Scene scene = new Scene(newScreen.getView());
        scene.getStylesheets().add(getClass().getResource("/fi/Sisu/view/styles.css").toExternalForm());

        // Set the main scene to the appState, register listeners to the main scene
        Controller controller = newScreen.getController();
        mainController = (MainController) controller;

        ViewModel viewModel = newScreen.getViewModel();
        addScreenChangeListener(viewModel);  
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("SisuApp v.0.3");
        primaryStage.show();
    }


    /**
     * Creates a new screen based on the screenType parameter.
     * The screen is set by MainController to a subscene of the main screen.
     * 
     * @param screenType
     * @throws IOException
     */
    public void changeSubScene(ScreenType screenType) throws IOException {
        // If the screenType is PREVIOUS_SCREEN, get the previous screen from the
        // history
        if (screenType == ScreenType.PREVIOUS_SCREEN) {
            screenType = screenHistory.pop();
        }

        ScreenObject newScreen = screenFactory.createScreen(appState, screenType);
        ViewModel newViewModel = newScreen.getViewModel();       

        // Add the current screen to the history
        if (getCurrentScreen() != null) {
            addScreenToHistory(getCurrentScreen());
        }
        currentScreen = screenType;
        mainController.setSubScene(newScreen.getView());

        addScreenChangeListener(newViewModel);
    }


    /**
     * Adds the given screenType to the screen history.
     * @param screenType
     */
    private void addScreenToHistory(ScreenType screenType) {
        if (screenHistory.size() >= MAX_HISTORY_SIZE) {
            screenHistory.removeLast();
        }
        screenHistory.push(screenType);
    }


    /**
     * Returns the current screen.
     * @return ScreenType enum of the current screen.
     */
    public ScreenType getCurrentScreen() {
        return currentScreen;
    }


    /**
     * Goes back to the previous screen.
     * @throws IOException
     */
    public void goBack() throws IOException {
        if (!screenHistory.isEmpty()) {
            ScreenType previousScreen = screenHistory.pop();
            changeSubScene(previousScreen);
        }
    }
}
