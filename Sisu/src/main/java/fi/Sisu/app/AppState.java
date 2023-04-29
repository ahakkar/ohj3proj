package fi.Sisu.app;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The only place which holds the studentID. ViewModel classes can ask the
 * studentID from this class.
 * 
 * @author Antti Hakkarainen
 */
public class AppState {
    private StringProperty selectedStudentID = new SimpleStringProperty();
    private StringProperty selectedModuleGroupId = new SimpleStringProperty();
    private BooleanProperty studentDataChanged = new SimpleBooleanProperty(false);
    private IntegerProperty totalCourseCredits = new SimpleIntegerProperty(0);

    /**
     * Used to communicate the selected student between different screens.
     */
    public StringProperty selectedStudentIDProperty() {
        return selectedStudentID;
    }

    public String getSelectedStudentID() {
        return selectedStudentID.get();
    }

    public void setSelectedStudentID(String selectedStudentID) {
        this.selectedStudentID.set(selectedStudentID);
    }

    /**
     * Used to communicate the module's groupId between studyprogramme screen
     * and search courses screen. This is needed because the search courses
     * screen needs to know which module the user wants to add a course to.
         */
    public StringProperty selectedModuleGroupIdProperty() {
        return selectedModuleGroupId;
    }

    public String getSelectedModuleGroupId() {
        return selectedModuleGroupId.get();
    }

    public void setSelectedModuleGroupId(String selectedModuleGroupId) {
        this.selectedModuleGroupId.set(selectedModuleGroupId);
    }


    /**
     * Used to communicate if student data has changed. This is needed because
     * you can't see the changes in Student object's variables.
     */
    public BooleanProperty studentDataChangedProperty() {
        return studentDataChanged;
    }

    public boolean isStudentDataChanged() {
        return studentDataChanged.get();
    }

    public void setStudentDataChanged(Boolean studentDataChanged) {
        this.studentDataChanged.set(studentDataChanged);
    }


    /**
     * Used to communicate the total credits of the student.
     * 
     * @return IntegerProperty totalCourseCredits
     */
    public IntegerProperty totalCourseCreditsProperty() {
        return totalCourseCredits;
    }

    public void setTotalCourseCredits(Integer totalCredits) {
        this.totalCourseCredits.set(totalCredits);
    }
}