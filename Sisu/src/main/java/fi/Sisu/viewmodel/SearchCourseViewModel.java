package fi.Sisu.viewmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import fi.Sisu.app.AppState;
import fi.Sisu.datasource.IApiDataSource;
import fi.Sisu.datasource.IFileDataSource;
import fi.Sisu.model.Course;
import fi.Sisu.model.SisuNode;
import fi.Sisu.model.Student;
import fi.Sisu.navigation.ScreenType;
import fi.Sisu.utils.MyJavaFXUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * SearchCourseViewModel serves as API between MainView and Business Logic of
 * the
 * application
 */
public class SearchCourseViewModel extends ViewModel {

    private final StringProperty requestedCourse;
    private final ObjectProperty<Course> selectedCourse;
    private StringProperty searchTextField;

    private ObservableList<Course> courseList;

    /**
     * Constructor.
     * 
     * @param modelFactory The Backend
     */
    public SearchCourseViewModel(AppState appState,
            IApiDataSource apiDataSource,
            IFileDataSource iFileDataSource) {
        super(appState, apiDataSource, iFileDataSource);

        this.requestedCourse = new SimpleStringProperty();
        this.selectedCourse = new SimpleObjectProperty<Course>();
        this.searchTextField = new SimpleStringProperty();
        this.courseList = FXCollections.observableArrayList();

        // Used to retrieve the course information from the backend when
        // the user changes the selected course in treeview
        requestedCourse.addListener((obs, oldCourseCode, newCourseCode) -> {
            if (newCourseCode != null && !newCourseCode.isEmpty()) {
                Optional<Course> course = this.apiDataSource.getCourse(newCourseCode);
                course.ifPresent(action -> setCourseProperty(action));
            }
        });
    }


    /**
     * Switches currently selected course. If course does not have grade (as it does
     * not when it is fetched from API) this method "refeshes" the coures and
     * retrieves the latest course from the SisuApi. Gives errors if searchfield
     * has less values that 3 or searchfield contains other characters than letters
     * or numbers.
     * 
     * @param course A valid course
     * @return If new course is retreieved from API returns that course. Else
     *         returns the original course.
     */
    public void handleSelectedCourse(Course course) {
        if (course == null) {
            return;
        }
        // If course is not graded fetch the most recent description from api.        
        Optional<Course> newCourse = apiDataSource.getCourse(course.getGroupId());
        newCourse.ifPresent(action -> {
            setCourseProperty(action);
        });        
        setRequestedCourse(course.getGroupId());
    }


    /**
     * On pressing cancel button, returns back to degreeprogramme view.
     */
    public void onCancelCourseSelectionButtonPressed() {
        setRequestedScreen(ScreenType.CURRICULUM_VIEW_SCREEN);
    }


    /**
     * Defines the behaviour of Select course button. If no course is selected it
     * gives warning and does nothing. Otherwise proceeds to set the course.
     */
    public void onSelectCourseButtonPressed() {
        String courseId = this.selectedCourse.getValue().getGroupId();

        Optional<Student> currentStudent = fileDataSource.getStudent(appState.getSelectedStudentID());
        Student student = currentStudent.get();
        if (student == null) {
            return;
        }

        Optional<SisuNode> currentDegreeProgramme = student.getStudyProgramme(); 
        SisuNode degreeProgramme = currentDegreeProgramme.get();
        if (degreeProgramme == null) { 
            return;
        }

        Course existingCourse = degreeProgramme.findCourseInTree(courseId);

        // Check that course is not already on degree programme.
        boolean courseExists = (existingCourse != null);

        // Check that course is not mandatory
        HashSet<String> set = degreeProgramme.getAllMandatoryCourses();
        boolean isMandatory = set.contains(courseId);

        // Check aprpriate action
        if (isMandatory) {
            MyJavaFXUtils.displayAlert(AlertType.WARNING, 
                "Mandatory course", 
                "Mandatory courses cannot be moved to another location", 
                "You can only select non mandatory courses.");
                return;
        }
        else if(courseExists) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Existing course");
            alert.setHeaderText("This course already exists in your degree program.");
            alert.setContentText("Do you want to set it to this module?");

            Optional<ButtonType> result = alert.showAndWait();
            if (!(result.get() == ButtonType.OK)) {
                // Do nothing if not willing to move.
                return;
            }
            else {
                // If moving first delete existing course
                HashSet<String> emptySet = new HashSet<>();
                degreeProgramme.deleteCourse(courseId, emptySet);
            }
        }
        
        // Adds and saves the course
        student.addCourse(appState.getSelectedModuleGroupId(), this.selectedCourse.getValue());
        fileDataSource.saveStudent(student);   
        setRequestedScreen(ScreenType.CURRICULUM_VIEW_SCREEN);
    }



    /**
     * Definies behavior of Search Button press and enter/return key press.
     */
    public void onSearchButtonPressed() {
        Optional<ArrayList<Course>> courses = 
            apiDataSource.searchForCourses(this.searchTextField.get());
        courses.ifPresent(action -> courseList.setAll(action));
    }

    public ObjectProperty<Course> selectedCourseProperty() {
        return selectedCourse;
    }

    private void setCourseProperty(Course newCourse) {
        this.selectedCourse.set(newCourse);
    }

    public StringProperty requestedCourseProperty() {
        return requestedCourse;
    }
 
    public void setRequestedCourse(String courseCode) {
        requestedCourse.set(courseCode);
    }

    public StringProperty searchTextFieldProperty() {
        return this.searchTextField;
    }

    public void setSearchTextFieldProperty(String searchTerm) {
        this.searchTextField.set(searchTerm);
    }

    public ObservableList<Course> courseListProperty() {
        return courseList;
    }

    public void setCourseListProperty(ObservableList<Course> courseList) {
        this.courseList = courseList;
    }
}