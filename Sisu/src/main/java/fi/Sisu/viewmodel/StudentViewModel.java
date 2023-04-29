package fi.Sisu.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.util.Optional;

import fi.Sisu.app.AppState;
import fi.Sisu.app.Constants;
import fi.Sisu.datasource.IApiDataSource;
import fi.Sisu.datasource.IFileDataSource;
import fi.Sisu.model.Student;
import fi.Sisu.navigation.ScreenType;
import fi.Sisu.utils.MyJavaFXUtils;

/**
 * StudentViewModel serves as API between MainView and Business Logic of the
 * application
 * 
 * @author Antti Hakkarainen
 */
public class StudentViewModel extends ViewModel {

    // Create binds for textfields
    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty studentId;
    private StringProperty degreeProgrammeName;

    private final BooleanProperty isInEditingMode;

    /**
     * Constructor.
     * 
     * @param model The Backend
     */
    public StudentViewModel(
            AppState appState,
            IApiDataSource apiDataSource,
            IFileDataSource iFileDataSource) {
        super(appState, apiDataSource, iFileDataSource);

        firstName = new SimpleStringProperty("");
        lastName = new SimpleStringProperty("");
        studentId = new SimpleStringProperty("");
        degreeProgrammeName = new SimpleStringProperty("");
        isInEditingMode = new SimpleBooleanProperty(false);

        // If no student is selected, we are creating a new student -> enable editing
        // mode
        if (appState.getSelectedStudentID() == null) {
            setIsInEditingMode(true);
        }

        // listens to changes in studentID
        this.appState.selectedStudentIDProperty().addListener((obs, oldStudentId, newStudentId) -> {
            updateStudentStringProperties();
        });

        updateStudentStringProperties();
    }


    /**
     * Update the viewmodel's information of the selected student.
     */
    public void updateStudentStringProperties() {
        if (appState.getSelectedStudentID() == null) {
            return;
        }

        // Check text fields and don't save data if text fields contain bad information 
        Optional<Student> selectedStudentOptional
            = fileDataSource.getStudent(appState.getSelectedStudentID());

        selectedStudentOptional.ifPresent(selectedStudent -> {
            firstName.set(selectedStudent.getFirstName().strip());
            lastName.set(selectedStudent.getLastName().strip());
            studentId.set(selectedStudent.getStudentID().strip());
            degreeProgrammeName.set(selectedStudent.getStudyProgrammeName());
        });
    }


    /**
     * Validates the text fields. If any of the text fields contain bad information,
     * an error message is shown and the method returns false.
     * 
     * @return true if all text fields contain valid information, false otherwise
     */
    public Boolean validateStudentTextFields() {
        if   (firstName.get().strip().length() < Constants.STUDENT_NAME_MIN_LENGTH 
            || lastName.get().strip().length() < Constants.STUDENT_NAME_MIN_LENGTH
        ) {
            MyJavaFXUtils.displayAlert(
                AlertType.WARNING,
                "Warning",
                "Incorrect data was entered.",
                "First and last name must be at least " + Constants.STUDENT_NAME_MIN_LENGTH + " characters.");
            return false;
        }
        if (studentId.get().strip().length() < Constants.STUDENT_ID_MIN_LENGTH
            || studentId.get().strip().length() > Constants.STUDENT_ID_MAX_LENGTH
        ) {
            MyJavaFXUtils.displayAlert(
                AlertType.WARNING,
                "Warning",
                "Incorrect data was entered.",
                "Student id must be between " 
                + Constants.STUDENT_ID_MIN_LENGTH
                + " and " + Constants.STUDENT_ID_MAX_LENGTH + " characters.");
            return false;
        }
        if (!studentId.get().strip().matches(Constants.STUDENT_ID_ALLOWED_REGEXP)) {
            MyJavaFXUtils.displayAlert(
                AlertType.WARNING,
                "Warning",
                "Incorrect data was entered.",
                "Student ID can only contain alphanumeric characters (a-z, A-Z, 0-9).");
            return false;
        }

        return true;
    }

    /**
     * Update or create a new student, depending on if studentid exists in JSON
     */
    private void saveStudentInfo() {
        // update existing student info
        if (fileDataSource.studentExists(studentId.get())) {
            fileDataSource.editStudent(
                    studentId.get(),
                    firstName.get(),
                    lastName.get());

            appState.setStudentDataChanged(true);
        }
        // create a new student
        else {
            fileDataSource
                    .addStudent(
                            studentId.get(),
                            firstName.get(),
                            lastName.get());
        }

        appState.setSelectedStudentID(studentId.get());
    }

    /**
     * 
     */
    public void onSelectStudyProgramButtonPressed() {
        if (!validateStudentTextFields()) {
            return;
        }
        saveStudentInfo();
        setRequestedScreen(ScreenType.SELECT_STUDY_PROGRAM_SCREEN);
    }

    /**
     * Toggles the isInEditingMode property.
     */
    public void onEditInfoButtonPressed() {
        setIsInEditingMode(!isInEditingMode());
    }

    /**
     * Returns the user to the previous screen without saving changes.
     * 
     * @throws IOException
     * 
     */
    public void onCancelButtonPressed() throws IOException {
        setRequestedScreen(ScreenType.PREVIOUS_SCREEN);
    }

    /**
     * This method gets called from Controller, when the user clicks the
     * "Edit Student" button in the StudentScreen. It sets the
     * requestedScreen property to the CHANGE_STUDY_PROGRAM.
     * 
     * @throws IOException
     */
    public void onSaveChangesButtonPressed() throws IOException {
        if (!validateStudentTextFields()) {
            return;
        }
        saveStudentInfo();
        setRequestedScreen(ScreenType.CURRICULUM_VIEW_SCREEN);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public StringProperty studentIdProperty() {
        return studentId;
    }

    public StringProperty degreeProgrammeNameProperty() {
        return degreeProgrammeName;
    }

    // Editing mode allows or disallows editing of the student info textfields
    public BooleanProperty isInEditingModeProperty() {
        return isInEditingMode;
    }

    public boolean isInEditingMode() {
        return isInEditingMode.get();
    }

    public void setIsInEditingMode(boolean isInEditingMode) {
        this.isInEditingMode.set(isInEditingMode);
    }
}