package fi.Sisu.viewmodel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

import fi.Sisu.app.AppState;
import fi.Sisu.datasource.IApiDataSource;
import fi.Sisu.datasource.IFileDataSource;
import fi.Sisu.model.SisuNode;
import fi.Sisu.model.Student;
import fi.Sisu.navigation.ScreenType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

/**
 * CurriculumViewModel serves as API between MainView and Business Logic of the
 * application
 * 
 * @author Antti Hakkarainen
 */
public class SelectDegreeProgrammeViewModel extends ViewModel {

    private final ObjectProperty<SisuNode> selectedDegreeProgramme;

    // Contains the study program items from the Sisu API.
    private ObservableList<SisuNode> studyProgramItems;
    private FilteredList<SisuNode> filteredStudyProgramItems;
    private SortedList<SisuNode> sortedStudyProgramItems;


    /**
     * Constructor.
     * 
     * @param model sisuApiModel interface which is used to fetch data from Sisu's
     *              KoriAPI
     */
    public SelectDegreeProgrammeViewModel(
            AppState appState,
            IApiDataSource apiDataSource,
            IFileDataSource iFileDataSource) {
        super(appState, apiDataSource, iFileDataSource);

        this.selectedDegreeProgramme = new SimpleObjectProperty<>();
    }


    /**
     * Cancel button returns to the student screen.
     */
    public void onCancelButtonPressed() {
        requestedScreen.set(ScreenType.STUDENT_SCREEN);
    }


    /**
     * Select button sets a new DegreeProgramme to the student, and then
     * returns to previous screen.
     */
    public void onSelectStudyProgramButtonPressed() {
        if (appState.getSelectedStudentID() == null) {
            System.err.println("StudentViewModel: StudentID not set when setting degreeProgramme!");
            return;
        }

        String studentId = appState.getSelectedStudentID();

        // Load degreeprogramme from sisu and save it to the Student
        Optional<Student> selectedStudentOptional
            = fileDataSource.getStudent(studentId);
        
        selectedStudentOptional.ifPresent(student -> {
            // If student was found, and student has a degree programme, reset it after confirmation
            student.getStudyProgramme().ifPresentOrElse(action -> {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Resetting degree programme");
                alert.setHeaderText("You are about to replace your previously\nselected degree programme.");
                alert.setContentText("Replacing degree programme will remove all\npreviously added modules and study attainments.\n\nAre you sure you want to proceed?");

                Optional<ButtonType> result = alert.showAndWait();  
                if (result.get() == ButtonType.OK) {
                    setNewStudyProgramme(student);
                }
            },
            () -> {
                // If student was found, but student has no degreeprogramme yet, add a new one to student
                setNewStudyProgramme(student);
            });             
        });        
    }


    /**
     * Sets a new study programme to the student.
     * 
     * @param student Student object to be updated
     */
    private void setNewStudyProgramme(Student student) {                    
        Optional<SisuNode> fullDegreeProgramme = 
        apiDataSource.getStudyProgramme(selectedDegreeProgramme.getValue().getGroupId());

        fullDegreeProgramme.ifPresent(programme -> {
            student.setStudyProgrammeGroupID(selectedDegreeProgramme.getValue().getGroupId());
            student.setStudyProgrammeName(selectedDegreeProgramme.getValue().getName());
            student.setStudyProgramme(programme);
            fileDataSource.saveStudent(student);
            setRequestedScreen(ScreenType.PREVIOUS_SCREEN);
        });     
    }


    /**
     * Loads the degree programmes from the Sisu API and initializes the
     * variables used for sorting and filtering the degreeprogramme list.
     */
    public void loadDegreeProgrammes() {
        // Get list of study programmes
        Optional<ArrayList<SisuNode>> list = apiDataSource.getAllStudyProgrammes();
        list.ifPresentOrElse(action -> {
            studyProgramItems = FXCollections.observableArrayList(action);
        
            // Wrap the ObservableList in a FilteredList (initially display all data).
            filteredStudyProgramItems = new FilteredList<>(studyProgramItems);
        
            // Wrap the FilteredList in a SortedList. (for A-Z/Z-A sorting)
            sortedStudyProgramItems = new SortedList<>(filteredStudyProgramItems);
        }, () -> {
            // If a list could not be provided, initialize an empty one.
            studyProgramItems = FXCollections.observableArrayList();
            filteredStudyProgramItems = new FilteredList<>(studyProgramItems);
            sortedStudyProgramItems = new SortedList<>(filteredStudyProgramItems);
        });
    }

    public void handleSelectedDegreeProgramme(SisuNode dp) {
        selectedDegreeProgramme.set(dp);
    }

    /**
     * Servers the list of items for the ListView in the Controller.
     * 
     * @return
     */
    public ObservableList<SisuNode> sortedItemsProperty() {
        return sortedStudyProgramItems;
    }

    /**
     * Filters the studyProgramItems list based on the given filterText
     * (from the user).
     * 
     * @param filterText contents from the filterTextField in the Controller
     */
    public void filterItems(String filterText) {
        filteredStudyProgramItems.setPredicate(degreeProgramme -> {
            if (filterText == null || filterText.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = filterText.toLowerCase();
            String degreeProgrammeName = degreeProgramme.getName().toLowerCase();

            return degreeProgrammeName.contains(lowerCaseFilter);
        });
    }

    /**
     * Sorts the studyProgramItems to asc or desc order based on DegreeProgramme's
     * name variable.
     * 
     * @param descending true if descending order, false if ascending order
     */
    public void setSortingOrder(boolean descending) {
        Comparator<SisuNode> degreeProgrammeComparator = Comparator.comparing(SisuNode::getName);

        if (descending) {
            sortedStudyProgramItems.setComparator(degreeProgrammeComparator.reversed());
        } else {
            sortedStudyProgramItems.setComparator(degreeProgrammeComparator);
        }
    }
}