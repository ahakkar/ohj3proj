package fi.Sisu.view;

import fi.Sisu.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Is the Main (Primary, Head, etc) view the user sees.
 * 
 * MainController is binded to MainViewModel to send notify of events whenever
 * communication between the two is needeed.
 * 
 * @author Antti Hakkarainen
 * @author Heikki Hohtari
 */
public class MainController extends Controller {

    // Links .fxml elements to the Controller
    @FXML
    private VBox mainContainer;

    @FXML
    private Pane viewContainerPane;

    @FXML
    private Label studentFirstNameLabel;

    @FXML
    private Label studentLastNameLabel;

    @FXML
    private Label studentIdLabel;

    @FXML
    private Label degreeProgrammeNameLabel;

    @FXML
    private Label ectsProgressLabel;

    @FXML
    private Label ectsPercentageProgressLabel;    

    @FXML
    private Button viewStudentInfoButton;

    @FXML
    private Button changeStudentButton;

    private MainViewModel viewModel;

    /**
     * Construcor to store private variables and set the binds and listening actions
     * to this controller.
     * 
     * @param mainViewModel
     * @param viewHandler
     */
    public MainController(MainViewModel mainViewModel) {
        this.viewModel = mainViewModel;
    }

    /**
     * Initialize the buttons in Controller to call the correct methods in the
     * ViewModel. So when user presses a button, the ViewModel is notified and
     * handles the event.
     */
    @FXML
    public void initialize() {
        // handle button press events
        viewStudentInfoButton.setOnAction(event -> viewModel.onViewStudentInfoButtonPressed());
        changeStudentButton.setOnAction(event -> viewModel.onChangeStudentButtonPressed());
        viewStudentInfoButton.visibleProperty().bind(viewModel.infoButtonVisibleProperty());

        // bind the textFields to the viewModel
        studentFirstNameLabel.textProperty().bind(viewModel.firstNameProperty());
        studentLastNameLabel.textProperty().bind(viewModel.lastNameProperty());
        studentIdLabel.textProperty().bind(viewModel.studentIDProperty());
        degreeProgrammeNameLabel.textProperty().bind(viewModel.degreeProgrammeProperty());

        // Updates the study progress in ECTS when totalCredits changes
        viewModel.totalCreditsProperty().addListener((observable, oldValue, newValue) -> {
            Integer targetCredits = viewModel.targetCreditsProperty().getValue();
            String progress = String.format("%.0f%%", (newValue.floatValue() / (float) targetCredits) * 100.0);
            ectsProgressLabel.setText("Completed: " +newValue + " / " + targetCredits + " ECTS");
            ectsPercentageProgressLabel.setText("Progress: " + progress);
        });
    }

    /**
     * Changes the displayed subscene at viewContainerPane
     * 
     * @param subScene
     */
    public void setSubScene(Parent subScene) {
        viewContainerPane.getChildren().clear();
        viewContainerPane.getChildren().add(subScene);
    }

}
