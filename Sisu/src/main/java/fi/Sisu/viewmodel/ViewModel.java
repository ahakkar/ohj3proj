package fi.Sisu.viewmodel;

import fi.Sisu.app.AppState;
import fi.Sisu.datasource.IApiDataSource;
import fi.Sisu.datasource.IFileDataSource;
import fi.Sisu.navigation.ScreenType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;


/**
 * Abstract class for all ViewModels.
 * 
 * ViewModels serve as API between GUI Views (named as Controller because of
 * JavaFX) and Business logic (Model).
 * 
 * ViewModel does not handle any Business logic, but can parse data so that it
 * can be entered to GUI. For instance ViewModel might recieve Student object
 * from Model and it would further parse that to strings to fit for the GUI.
 * 
 * ViewModel also handles userinput it gets from GUI (Controller). It does not listen
 * for events (that is the job of Controller), but when event occours in View it
 * is passed to ViewModel for handling.
 * 
 * @author Antti Hakkarainen
 */
public abstract class ViewModel {  
    
    protected final AppState appState;
    protected final IApiDataSource apiDataSource;
    protected final IFileDataSource fileDataSource;
    protected final ObjectProperty<ScreenType> requestedScreen;

    /**
     * Constructor.
     * 
     * @param appState AppState object holds the studentId
     * @param modelFactory ModelFactory provides access to the different models
     */
    public ViewModel(
        AppState appState, 
        IApiDataSource apiDataSource, 
        IFileDataSource iFileDataSource) 
        {
        this.appState = appState;
        this.apiDataSource = apiDataSource;
        this.fileDataSource = iFileDataSource;
        this.requestedScreen = new SimpleObjectProperty<>();
    }


    /**
     * Communicates the screen change from ViewModel to ViewHandler, which
     * actually changes the subscreen.
     * 
     * @return ObjectProperty<ScreenType>
     */
    public ObjectProperty<ScreenType> requestedScreenProperty() {
        return requestedScreen;
    }


    /**
     * Sets the requestedScreen property.
     * 
     * @param screenType
     * @return void
     */
    public void setRequestedScreen(ScreenType screenType) {
        requestedScreen.set(screenType);
    }  
    
}
