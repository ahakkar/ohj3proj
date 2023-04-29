package fi.Sisu.view;

/**
 * Abstract class for all Controllers (= Views in MVVM pattern).
 * Controller acts as an intermediate between user and ViewModel.
 * 
 * Each .fxml view has a Controller. It's job is to:
 *  - Update the view based on the ViewModel's commands.
 *  - Rgister user actions, like button clicks, and pass the events to the ViewModel.
 * 
 * ViewModel then handles the event, updates the Model and the View accordingly.
 * 
 * @author Antti Hakkarainen
 */
public abstract class Controller {

    public abstract void initialize();

}
