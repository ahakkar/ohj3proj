package fi.Sisu.viewmodel;

import java.util.HashSet;
import java.util.Optional;

import fi.Sisu.app.AppState;
import fi.Sisu.datasource.IApiDataSource;
import fi.Sisu.datasource.IFileDataSource;
import fi.Sisu.model.Course;
import fi.Sisu.model.SisuNode;
import fi.Sisu.model.Student;
import fi.Sisu.navigation.ScreenType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;


/**
 * CurriculumViewModel serves as API between MainView and Business Logic of the
 * application
 */
public class DegreeProgrammeViewModel extends ViewModel {

    private final StringProperty requestedCourse;
    private final ObjectProperty<Course> course;
    private final ObjectProperty<SisuNode> studyProgramme;
    private final BooleanProperty studentDataChanged;
    

    /**
     * Constructor.
     * 
     * @param modelFactory The Backend
     */
    public DegreeProgrammeViewModel(
        AppState appState, 
        IApiDataSource apiDataSource, 
        IFileDataSource iFileDataSource) 
        {
        super(appState, apiDataSource, iFileDataSource);   

        this.requestedCourse = new SimpleStringProperty();
        this.course = new SimpleObjectProperty<Course>();
        this.studyProgramme = new SimpleObjectProperty<SisuNode>();
        this.studentDataChanged = new SimpleBooleanProperty(false);

        // Used to retrieve the course information from the backend when
        // the user changes the selected course in treeview
        requestedCourse.addListener((obs, oldCourseCode, newCourseCode) -> {
            if (newCourseCode == null || newCourseCode.isEmpty() || newCourseCode.equals("0")) {
                setCourseProperty(null);     
                return;          
            }

            // Load Student's degreeprogramme and...
            Optional<SisuNode> dp = 
            fileDataSource.getStudent(
                appState.getSelectedStudentID())
                .get()
                .getStudyProgramme();
                
            dp.ifPresent(programme -> {
                // look if the course data is already in student's data
                Course fileCourse = programme.findCourseInTree(newCourseCode);
                if (fileCourse != null) {
                    setCourseProperty(fileCourse);
                } 
                // Otherwise load the course data from the API
                else { 
                    Optional<Course> apiCourse = apiDataSource.getCourse(newCourseCode);
                    apiCourse.ifPresent(course -> setCourseProperty(course));
                }
            });  
        });

        loadStudyProgramme();
    }

    public void onAddCourseButtonPressed(String moduleGroupId, String courseGroupId) { 
        appState.setSelectedModuleGroupId(moduleGroupId);         
        setRequestedScreen(ScreenType.SEARCH_COURSE_SCREEN);
    }

    public void onSaveModuleChangesButtonPressed(String moduleGroupId, HashSet<String> chosenCourses) {
        Optional<Student> currentStudentOptional
            = fileDataSource.getStudent(appState.getSelectedStudentID());

        currentStudentOptional.ifPresent(currentStudent -> {
            currentStudent.setModuleChosenCourses(moduleGroupId, chosenCourses);
            fileDataSource.saveStudent(currentStudent);
            isStudentDataChanged(true); // TODO maybe use appstate's student data changed for this..
            appState.setStudentDataChanged(true);  
        });        
    }


    public void onRemoveCourseButtonPressed(String moduleGroupId, String courseGroupId) {
        Optional<Student> currentStudentOptional
            = fileDataSource.getStudent(appState.getSelectedStudentID());

        currentStudentOptional.ifPresent(currentStudent -> {
            currentStudent.removeCourse(moduleGroupId, courseGroupId);
            fileDataSource.saveStudent(currentStudent);
            isStudentDataChanged(true); // TODO maybe use appstate's student data changed for this..
            appState.setStudentDataChanged(true);  
        });
    }


    public void onRemoveCourseGradeButtonPressed(String moduleGroupId, String courseGroupId) {
        Optional<Student> currentStudentOptional
            = fileDataSource.getStudent(appState.getSelectedStudentID());

        currentStudentOptional.ifPresent(currentStudent -> {
            currentStudent.setGrade(moduleGroupId, courseGroupId, null);
            fileDataSource.saveStudent(currentStudent);
            isStudentDataChanged(true); // TODO maybe use appstate's student data changed for this..
            appState.setStudentDataChanged(true);  
        });
    }


    public void onSaveCourseGradeButtonPressed(String moduleGroupId, String courseGroupId, String grade) {
        Optional<Student> currentStudentOptional
            = fileDataSource.getStudent(appState.getSelectedStudentID());

        currentStudentOptional.ifPresent(currentStudent -> {
            currentStudent.setGrade(moduleGroupId, courseGroupId, grade);
            fileDataSource.saveStudent(currentStudent);
            isStudentDataChanged(true); // TODO maybe use appstate's student data changed for this..
            appState.setStudentDataChanged(true);  
        });
    }


    /**
     * Loads the study programme of the selected student.
     */
    private void loadStudyProgramme() {          
        fileDataSource.getStudent(appState.getSelectedStudentID()).ifPresent(selectedStudent -> {
            selectedStudent.getStudyProgramme().ifPresent(studyProgramme -> {
                setStudyProgramme(studyProgramme);
            });
        });
        
    }

    public void handleSelectedCourseGroupId(String courseGroupId) {
        requestedCourse.set(courseGroupId);
    }

    public void handleTotalCourseCredits(Integer totalCredits) {
        appState.setTotalCourseCredits(totalCredits);
    }

    public ObjectProperty<SisuNode> studyProgrammeProperty() {
        return studyProgramme;
    }

    public void setStudyProgramme(SisuNode studyProgramme) {
        this.studyProgramme.set(studyProgramme);
    }

    public ObjectProperty<Course> courseProperty() {
        return course;
    }

    private void setCourseProperty(Course course) {
        this.course.set(course);
    }

    public StringProperty requestedCourseProperty() {
        return requestedCourse;
    }
    
    public void setRequestedCourse(String courseCode) {
        requestedCourse.set(courseCode);
    }

    public BooleanProperty studentDataChangedProperty() {
        return studentDataChanged;
    }
    
    /**
     * Reloads the student's studyprogramme if student data is changed, so
     * the view can be updated with fresh data.
     * 
     * @param isChanged
     */
    public void isStudentDataChanged(Boolean isChanged) {
        if (isChanged == true) {
            loadStudyProgramme();
        }
        studentDataChanged.set(isChanged);
    }
}