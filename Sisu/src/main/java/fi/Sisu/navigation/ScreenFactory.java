package fi.Sisu.navigation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import fi.Sisu.app.AppState;
import fi.Sisu.datasource.IApiDataSource;
import fi.Sisu.datasource.IFileDataSource;
import fi.Sisu.view.Controller;
import fi.Sisu.viewmodel.ViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Creates and contains ScreenObjects. One ScreenObject for every ViewModel,
 * Controller and .fxml trio.
 * 
 * Provides ScreenObjects for NavigationService, which switches the screens
 * based on what ViewModels tells it to do.
 * 
 * @author Antti Hakkarainen
 */
public class ScreenFactory {

    private final IApiDataSource apiDataSource;
    private final IFileDataSource fileDataSource;

    /**
     * Constructor for ScreenManager.
     * @throws IOException
     */

    public ScreenFactory(
        IApiDataSource apiDataSource, 
        IFileDataSource iFileDataSource
        ) throws IOException  {
        this.apiDataSource = apiDataSource;
        this.fileDataSource = iFileDataSource;
    }


    /**
     * Load and return screen based on the screen type parameter.
     * 
     * @param screenType ScreenType enum value
     * @return Parent object loaded screen
     */
    public ScreenObject createScreen(
        AppState appState, 
        ScreenType screenType
        ) throws IOException
        {
        try {
            ViewModel viewModel = screenType
                .getViewModelClass()
                .getConstructor(AppState.class, IApiDataSource.class, IFileDataSource.class)
                .newInstance(appState, apiDataSource, fileDataSource);


            Controller controller = screenType
                .getControllerClass()
                .getConstructor(screenType.getViewModelClass())
                .newInstance(viewModel);

            Parent newScreen = loadScreen(screenType.getViewPath(), viewModel, controller);
            ScreenObject obj = new ScreenObject(newScreen, viewModel, controller);
            return obj;   

        } 
        catch (InstantiationException e) {
            throw new IOException("Error instantiating ViewModel or Controller for screen type: " + screenType, e);
        } 
        catch (IllegalAccessException e) {
            throw new IOException("Error accessing constructor for ViewModel or Controller of screen type: " + screenType, e);
        } 
        catch (NoSuchMethodException e) {
            throw new IOException("Constructor not found for ViewModel or Controller of screen type: " + screenType, e);
        }
        catch (InvocationTargetException e) {
            throw new IOException("Error invoking constructor for ViewModel or Controller of screen type: " + screenType, e);
        }
    }


    /**
     * Helper method for loading .fxml files and setting the controller and
     * ViewModel.
     * 
     * @param viewPath   Path to the .fxml file
     * @param viewModel  ViewModel to be set to the controller
     * @param controller Controller to be set to the .fxml file
     * @return Parent object loaded screen
     * @throws IOException
     */
    private static Parent loadScreen(String viewPath, ViewModel viewModel, Controller controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(ScreenFactory.class.getResource(viewPath));
        loader.setController(controller);

        // If the controller class has a method named 'setViewModel', this block sets
        // the ViewModel
/*         try {
            Method setViewModelMethod = controller.getClass().getMethod("setViewModel", viewModel.getClass());
            setViewModelMethod.invoke(controller, viewModel);
        }
        catch (NoSuchMethodException e) {
            System.err.println("Method 'setViewModel' not found");
        }
        catch (IllegalAccessException e) {   
            System.err.println("Method 'setViewModel' cannot be accessed");
        } 
        catch (InvocationTargetException e) { 
            System.err.println("Method 'setViewModel' invocation failed");
        } */

        return loader.load();
    }

}
