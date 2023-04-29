package fi.Sisu.navigation;

import fi.Sisu.view.Controller;
import fi.Sisu.viewmodel.ViewModel;
import javafx.scene.Parent;

/**
 * Stores the model and controller of each screen.
 * 
 * @author Antti Hakkarainen  
 */
public class ScreenObject {
    private Parent view;
    private ViewModel viewModel;
    private Controller controller;

    /**
     * Constructor for Screen class.
     * @param viewModel the viewmodel object instance of a screen
     * @param controller the controller object instance of a screen
     */
    public ScreenObject(Parent view, ViewModel viewModel, Controller controller) {
        this.view = view;
        this.viewModel = viewModel;
        this.controller = controller;
    }


    /**
     * Getter for the stored view object.
     * @return
     */
    public Parent getView() {
        return view;
    }
    

    /**
     * Getter for the stored viewModel object.
     * @return
     */
    public ViewModel getViewModel() {
        return viewModel;
    }
    

    /**
     * Getter for the stored controller object.
     * @return
     */
    public Controller getController() {
        return controller;
    }

}