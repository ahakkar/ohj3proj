package fi.Sisu.view;

import fi.Sisu.model.Course;
import fi.Sisu.utils.MyJavaFXUtils;
import fi.Sisu.viewmodel.SearchCourseViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Controller for course screen.
 * 
 */
public class SearchCourseController extends Controller {

    // Links .fxml elements to the Controller
    @FXML
    WebView courseDetailsWebView;

    @FXML
    TextField searchTextField;

    @FXML
    Button searchButton;

    @FXML
    ListView<Course> courseListView;

    @FXML
    Button cancelCourseSelectionButton;

    @FXML
    Button selectCourseButton;

    // To pass events to and reciev new data from.
    private SearchCourseViewModel viewModel;

    /**
     * * Construcor to store private variables and set the binds and listening
     * actions
     * to this controller.
     * 
     * @param model
     */
    public SearchCourseController(SearchCourseViewModel viewModel) {
        this.viewModel = viewModel;

    }


    /**
     * Initializes the Controller. Sets up bindings and listeners.
     */
    @FXML
    public void initialize() {

        // Bind elements to viewmodel corresponding properties
        Bindings.bindBidirectional(searchTextField.textProperty(), viewModel.searchTextFieldProperty());

        // Bind functions to viewmodel functions
        searchTextField.setOnAction(event -> viewModel.onSearchButtonPressed());
        searchButton.setOnAction(event -> {
            if(validateSearchParameters()) { 
                viewModel.onSearchButtonPressed();
            }
        });   
        cancelCourseSelectionButton.setOnAction(event -> {
            viewModel.onCancelCourseSelectionButtonPressed();                        
        });     
        selectCourseButton.setOnAction(event -> {
            if(validateSelectedCourse(viewModel.selectedCourseProperty().get())) {
                viewModel.onSelectCourseButtonPressed();
            }            
        });

        // Bind the left side list to view Model
        courseListView.setItems(viewModel.courseListProperty());

        // Bind the courseDescription property to the WebView
        viewModel.selectedCourseProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                WebEngine webEngine = courseDetailsWebView.getEngine();             
                webEngine.loadContent(MyJavaFXUtils.formatCourseHTML(newValue)); 
            }
        });

        // Listen for item selection
        courseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.handleSelectedCourse(newValue);    
        });

    }


    private Boolean validateSearchParameters() {
        // Validate input
        if (this.searchTextField.getText() == null) {
            MyJavaFXUtils.displayAlert(
                AlertType.WARNING,
                "Search term is invalid",
                "Search term must not be empty."); 
            return false;
        }
        else if (this.searchTextField.getText().length() < 3) {
            MyJavaFXUtils.displayAlert(
                AlertType.WARNING,
                "Search term is invalid",
                "Search term must contain at least 3 characters."); 
            return false;
        }   
        else if (!this.searchTextField.getText().matches("[a-zA-Z0-9åäöÅÄÖ]+")) {
            MyJavaFXUtils.displayAlert(
                AlertType.WARNING,
                "Search term is invalid",
                "Search term must contain only letters and numbers.");

            return false;
        }

        return true;
    }

    private Boolean validateSelectedCourse(Course selectedCourse) {
        if (viewModel.selectedCourseProperty().get() == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No course selected");
            alert.setContentText("You have to select a course from the list before adding it to your study programme.");
            alert.showAndWait();
            return false;
        }

        return true;
    }
}
