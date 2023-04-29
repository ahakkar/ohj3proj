package fi.Sisu.view;

import java.util.Optional;

import fi.Sisu.model.Student;
import fi.Sisu.viewmodel.SelectStudentViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;

/**
 * Is the Student view the user sees.
 * 
 * @author Antti Hakkarainen
 */
public class SelectStudentController extends Controller {

    // Links .fxml elements to the Controller
    @FXML
    VBox mainContainer;

    @FXML
    Button deleteStudentButton;

    @FXML
    Button newStudentButton;

    @FXML
    Button chooseStudentButton;

    @FXML
    ListView<Student> studentListView;

    // To pass events to and reciev new data from.
    private SelectStudentViewModel viewModel;


    /**
     * * Construcor to store private variables and set the binds and listening
     * actions
     * to this controller.
     * 
     * @param model
     */
    public SelectStudentController(SelectStudentViewModel viewModel) {
        this.viewModel = viewModel;
    }


    /**
     * Initialize the buttons in Controller to call the correct methods in the
     * ViewModel. So when user presses a button, the ViewModel is notified and
     * handles the event.
     */
    @FXML
    public void initialize() {

        // Set the cell factory for the listview to display only the name of the DegreeProgramme
        studentListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    if (item.getStudentID() == null) {
                        setText("");                        
                    } else {
                        setText(item.getFirstName() + " " +  item.getLastName() + " [" + item.getStudentID() + "]");
                    }
                }
            }
        });

        // set the items in the listview to the items in the model
        studentListView.setItems(viewModel.studentItemsProperty());

        // Disable the buttons if no student is selected
        chooseStudentButton.disableProperty().bind(studentListView.getSelectionModel().selectedItemProperty().isNull());

        deleteStudentButton.setOnAction(event -> {
            
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Delete Student");
            confirmationAlert.setHeaderText("Are you sure you want to delete this student?");
            confirmationAlert.setContentText("This action cannot be undone.");
        
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                viewModel.onDeleteStudentButtonPressed();
            }
        });
        
        newStudentButton.setOnAction(event -> viewModel.onNewStudentButtonPressed());
        chooseStudentButton.setOnAction(event -> {
            Optional<Student> selectedStudent = 
                Optional.ofNullable(studentListView.getSelectionModel().getSelectedItem());
            viewModel.onChooseStudentButtonPressed(selectedStudent);
        });

        // Bind the selected student to viewModel, so we can use the info to delete students
        viewModel.selectedStudentProperty().bind(studentListView.getSelectionModel().selectedItemProperty());
    }

}
