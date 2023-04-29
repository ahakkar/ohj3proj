package fi.Sisu.view;

import java.io.IOException;

import fi.Sisu.utils.MyJavaFXUtils;
import fi.Sisu.viewmodel.StudentViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Is the Student view the user sees.
 * 
 * StudentController is binded to StudentViewModel to send notify of events
 * whenever
 * communication between the two is needeed.
 * 
 * @author Antti Hakkarainen
 */
public class StudentController extends Controller {

    // Links .fxml elements to the Controller
    @FXML
    private VBox mainContainer;

    @FXML
    private Button selectStudyProgramButton;

    @FXML
    private Button editInfoButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveChangesButton;

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private TextField studentIdTextField;

    @FXML
    private Label degreeProgrammeLabel;

    // To pass events to and reciev new data from.
    private StudentViewModel viewModel;

    /**
     * Construcor to store private variables and set the binds and listening
     * actions to this controller.
     * 
     * @param model
     */
    public StudentController(StudentViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Initialize the buttons in Controller to call the correct methods in the
     * ViewModel. So when user presses a button, the ViewModel is notified and
     * handles the event.
     */
    @FXML
    public void initialize() {
        initBindings();
        setOnAction();
        // TODO fix the textfield formatter

        // Disable the fields, if we initially are not in editing mode
        if (!viewModel.isInEditingModeProperty().get()) {
            firstNameTextField.getStyleClass().add("non-editable-textfield");
            lastNameTextField.getStyleClass().add("non-editable-textfield");
            studentIdTextField.getStyleClass().add("non-editable-textfield");
        }

        // change element styles based on editing mode
        viewModel.isInEditingModeProperty().addListener((obs, oldvalue, newvalue) -> {
            if (newvalue) {
                firstNameTextField.getStyleClass().remove("non-editable-textfield");
                lastNameTextField.getStyleClass().remove("non-editable-textfield");
                if (viewModel.studentIdProperty().get().isEmpty()) {
                    studentIdTextField.getStyleClass().remove("non-editable-textfield");
                }
            } else {
                firstNameTextField.getStyleClass().add("non-editable-textfield");
                lastNameTextField.getStyleClass().add("non-editable-textfield");
                studentIdTextField.getStyleClass().add("non-editable-textfield");
            }
        });

        // allows only letters and Finnish letters
        MyJavaFXUtils.addCharacterConstraint(firstNameTextField, "[a-zA-ZåäöÅÄÖ]*"); 
        MyJavaFXUtils.addCharacterConstraint(lastNameTextField, "[a-zA-ZåäöÅÄÖ]*"); 
        // Accepts only limited characters
        MyJavaFXUtils.addCharacterConstraint(studentIdTextField, "^[a-zA-Z0-9]{1,16}$");


    }

    /**
     * Bind the textFields to the viewModel.
     */
    public void initBindings() {
        // bind the textFields to the viewModel
        firstNameTextField.textProperty().bindBidirectional(viewModel.firstNameProperty());
        lastNameTextField.textProperty().bindBidirectional(viewModel.lastNameProperty());
        studentIdTextField.textProperty().bindBidirectional(viewModel.studentIdProperty());
        degreeProgrammeLabel.textProperty().bind(viewModel.degreeProgrammeNameProperty());

        // make the fields editable only when in editing mode
        firstNameTextField.editableProperty().bind(viewModel.isInEditingModeProperty());
        lastNameTextField.editableProperty().bind(viewModel.isInEditingModeProperty());
        studentIdTextField.editableProperty().bind(viewModel.isInEditingModeProperty());
    }

    /**
     * Set the event actions of the buttons.
     */
    private void setOnAction() {
        // handle events when user presses a certain button
        selectStudyProgramButton.setOnAction(event -> viewModel.onSelectStudyProgramButtonPressed());
        editInfoButton.setOnAction(event -> viewModel.onEditInfoButtonPressed());
        cancelButton.setOnAction(event -> {
            try {
                viewModel.onCancelButtonPressed();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        saveChangesButton.setOnAction(event -> {
            try {
                viewModel.onSaveChangesButtonPressed();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
