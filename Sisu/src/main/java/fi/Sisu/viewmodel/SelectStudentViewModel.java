package fi.Sisu.viewmodel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.Sisu.app.AppState;
import fi.Sisu.datasource.IApiDataSource;
import fi.Sisu.datasource.IFileDataSource;
import fi.Sisu.model.Student;
import fi.Sisu.navigation.ScreenType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * StudentViewModel serves as API between MainView and Business Logic of the
 * application
 * 
 * @author Antti Hakkarainen
 */
public class SelectStudentViewModel extends ViewModel {  

    private ObservableList<Student> studentItems;
    private ObjectProperty<Student> selectedStudent;    

    /**
     * Constructor.
     * 
     * @param model The Backend
     */
    public SelectStudentViewModel(
        AppState appState, 
        IApiDataSource apiDataSource, 
        IFileDataSource iFileDataSource) 
        {
        super(appState, apiDataSource, iFileDataSource);  

        this.selectedStudent = new SimpleObjectProperty<Student>();

        loadStudents();
    }

    /**
     * loads a list of students from the JSON file to studentItems variable so
     * the View can display them in the ListView
     */
    public void loadStudents() {
        List<Student> students = fileDataSource.getAllStudents();
        students = removeInvalidStudents(students);
        studentItems = FXCollections.observableArrayList(students);
    }


    /**
     * Deletes the student from the JSON file
     */
    public void onDeleteStudentButtonPressed() {
        if (selectedStudent.get().getStudentID() == null) {
            return;
        }

        fileDataSource.deleteStudent(selectedStudent.get().getStudentID());
        studentItems.remove(selectedStudent.get());

    }


    /**
     * Moves the user to a student screen, which has no student info.
     * So setting the selectedStudentID to null in appState
     */
    public void onNewStudentButtonPressed() {
        appState.setSelectedStudentID(null);
        appState.setStudentDataChanged(true);
        setRequestedScreen(ScreenType.STUDENT_SCREEN);
    }


    /**
     * Moves the user to a degreeprogram's screen, which has selected student's
     * degree structure and courses displayed
     */
    public void onChooseStudentButtonPressed(Optional<Student> chosenStudent) {
        chosenStudent.ifPresent(student -> {
            if (student.getStudentID() == null) {
                return;
            }
            appState.setSelectedStudentID(student.getStudentID());
            setRequestedScreen(ScreenType.CURRICULUM_VIEW_SCREEN);
        });
    }


    /**
     * Removes students that have no studentID from the list
     * 
     * @param students List<Student> to be filtered
     * @return List<Student> filtered list
     */
    public List<Student> removeInvalidStudents(List<Student> students) {
        return students.stream()
            .filter(student -> student.getStudentID() != null)
            .collect(Collectors.toList());
    }


    /**
     * Servers the list of items for the ListView in the Controller.
     * 
     * @return
     */
    public ObservableList<Student> studentItemsProperty() {
        return studentItems;
    }


    /**
     * Holds the Student selected in the View's ListView.
     * @return
     */
    public ObjectProperty<Student> selectedStudentProperty() {
        return selectedStudent;
    }

}
