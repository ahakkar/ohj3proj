package fi.Sisu.viewmodel;

import java.io.IOException;
import java.util.Optional;

import fi.Sisu.app.AppState;
import fi.Sisu.datasource.IApiDataSource;
import fi.Sisu.datasource.IFileDataSource;
import fi.Sisu.model.Student;
import fi.Sisu.navigation.ScreenType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * MainViewModel servers as API between MainView and Business Logic of the
 * application.
 * 
 * Screens are changed by changing the currentScreen property.
 * 
 * @author Antti Hakkarainen
 * @author Heikki Hohtari
 */
public class MainViewModel extends ViewModel {    

    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty studentID;
    private StringProperty degreeProgramme;
    private IntegerProperty totalCredits;
    private IntegerProperty targetCredits;

    private final BooleanProperty infoButtonVisible;
 

    /**
     * Constructor.
     * 
     * @param model The Backend
     * @throws IOException
     */
    public MainViewModel(
        AppState appState, 
        IApiDataSource apiDataSource, 
        IFileDataSource iFileDataSource) 
        {
        super(appState, apiDataSource, iFileDataSource);   
        
        new SimpleObjectProperty<ScreenType>();      
        this.infoButtonVisible = new SimpleBooleanProperty(false);     

        firstName = new SimpleStringProperty();
        lastName = new SimpleStringProperty();
        studentID = new SimpleStringProperty();
        degreeProgramme = new SimpleStringProperty();    
        totalCredits = new SimpleIntegerProperty(0);
        targetCredits = new SimpleIntegerProperty(0);

        // Listen to changes in the selected student ID.
        appState.selectedStudentIDProperty().addListener((obs, oldStudentID, newStudentID) -> {
            // set the info button visible if a student is selected
            if (newStudentID != null) {
                infoButtonVisible.set(true);
            } else {
                infoButtonVisible.set(false);
            }
            updateStudentInfo();
        });

        // Listen to changes in student data
        appState.studentDataChangedProperty().addListener((obs, oldStudentDataChanged, newStudentDataChanged) -> {  
            appState.setStudentDataChanged(false);      
            updateStudentInfo();
            
        });
    }


    /**
     * Called from the controller's initializer to update the viewmodel's
     * information of the selected student.
     */     
    public void updateStudentInfo() {
        if (appState.getSelectedStudentID() == null) {
            firstName.set("");
            lastName.set("");
            studentID.set("");
            degreeProgramme.set(null);
            return;
        }

        Optional<Student> selectedStudentOptional
            = fileDataSource.getStudent(appState.getSelectedStudentID());

        selectedStudentOptional.ifPresent(selectedStudent -> {
            firstName.set(selectedStudent.getFirstName());
            lastName.set(selectedStudent.getLastName());
            studentID.set(selectedStudent.getStudentID());
            degreeProgramme.set(selectedStudent.getStudyProgrammeName());

            // Order of these two is important for progress calculations! 
            // Progress update is triggered by change in totalCredits
            selectedStudent.getStudyProgramme().ifPresent(studyProgramme -> {
                targetCredits.set(studyProgramme.getTargetCredits());
                totalCredits.set(selectedStudent.calculateTotalCompletedCredits());
            });              
        });     
    }


    // Methods for binding student data between controller and viewmodel.
    public StringProperty firstNameProperty() { return firstName; }

    public StringProperty lastNameProperty() { return lastName; }
    
    public StringProperty studentIDProperty() { return studentID; }

    public StringProperty degreeProgrammeProperty() { return degreeProgramme; }

    public IntegerProperty totalCreditsProperty() { return totalCredits; }

    public IntegerProperty targetCreditsProperty() { return targetCredits; }

    public BooleanProperty infoButtonVisibleProperty() { return infoButtonVisible; }    

    /**
     * This method gets called from Controller, when the user clicks the
     * "Edit Student" button in the MainScreen. It sets the
     * requestedScreen property to the STUDENT_SCREEN.
     */
    public void onViewStudentInfoButtonPressed() {
        setRequestedScreen(ScreenType.STUDENT_SCREEN);
    }

    /**
     * This method gets called from Controller, when the user clicks the
     * "Change Student" button in the ChooseStudentScreen. It sets the
     * requestedScreen property to the CHOOSE_STUDENT_SCREEN.
     */
    public void onChangeStudentButtonPressed() {
        setRequestedScreen(ScreenType.CHOOSE_STUDENT_SCREEN);
    }

    
    public boolean isInfoButtonVisible() {
        return infoButtonVisible.get();
    }
    
    public void setInfoButtonVisible(boolean visible) {
        infoButtonVisible.set(visible);
    }

}
