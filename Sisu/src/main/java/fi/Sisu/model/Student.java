package fi.Sisu.model;

import java.util.HashSet;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Student class is used to store student data.
 * 
 * @author Heikki Hohtari
 */
public class Student {
    @JsonProperty("studentID")
    private String studentID;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("studyProgrammeGroupID")
    private String studyProgrammeGroupID;

    @JsonProperty("studyProgrammeName")
    private String studyProgrammeName;

    @JsonProperty("studyProgramme")
    private SisuNode studyProgramme;

    /**
     * Basic constructor for jackson to work with. Jackson Requires empty public
     * construcor.
     */
    public Student() {
        // Jackson Requires empty construcor
    }

    /**
     * Student represents a single student, with name, id, attainments etc.
     * 
     * @param studentID String student id
     * @param firstName String student first name
     * @param lastName  String student last name
     * @param attainments List of attainments
     * @param studyProgramme List of study programmes
     */
    public Student(String studentID, String firstName, String lastName,
            SisuNode studyProgramme) {
        this.studentID = studentID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studyProgramme = studyProgramme;
    }

    /**
     * Set a grade for a course in the study programme. First the module is found
     * from the study programme, then the course is found from the module. 
     * @param moduleGroupId GroupId of the module the course is supposed to be in.
     * @param courseGroupId GroupId of the course.
     * @param grade Grade to be set, should be "pass", "fail" or a number between 0-5.
     * @return True if the grade was set, false if the course was not found.
     */
    public boolean setGrade(String moduleGroupId, String courseGroupId, String grade) {
        
        // Find the module from the study programme, this way is faster than
        // searching every course from every module until the correct one is found.
        SisuNode node = studyProgramme.findNodeInTree(moduleGroupId);
        if (node == null) { return false; }

        // Then find the course from the module.
        Course course = node.findCourse(courseGroupId);
        if (course == null) { return false; }

        course.setGrade(grade);
        return true;
    }


    /**
     * Sets the chosen courses for a module in the degreeprogramme.
     * 
     * @param moduleGroupId String of the studymodule's groupId
     * @param chosenCourses HashSet<String> of course groupIds
     * @return True if the chosencourses were set, false if the module was not found.
     */
    public boolean setModuleChosenCourses(String moduleGroupId, HashSet<String> chosenCourses) {
        SisuNode studyModule = studyProgramme.findNodeInTree(moduleGroupId);
        if (studyModule == null) { return false; }

        studyModule.setChosenCourses(chosenCourses);
        return true;
    }


    /**
     * Save a course to the selected studymodule in the Student's degree programme.
     * 
     * @param moduleGroupId String groupId of the module the course is supposed to be in.
     * @param courseGroupId String groupId of the course.
     * @return True if the course was added, false if the course was not found.
     */
    // TODO: Eikö selectCourse olisi kuvaavampi nimi?
    public boolean addCourse(String moduleGroupId, Course newCourse) {
        SisuNode studyModule = studyProgramme.findNodeInTree(moduleGroupId);
        if (studyModule == null) { return false; }   
        
        // Add the added course to chosen courses too, so it is displayed immediately in UI.
        // Execution order here is important! Don't change it.
        studyModule.addChildCourse(newCourse);
        studyModule.addChosenCourse(newCourse.getGroupId());
       
        return true;
    }


    /**
     * Remove a course from the selected courses in the study programme.
     * 
     * @param moduleGroupId
     * @param courseGroupId
     * @return True if the course was removed, false if the course was not found.
     */
    // TODO: Eikö unSelectCourse olisi kuvaavampi nimi?
    public boolean removeCourse(String moduleGroupId, String courseGroupId) {

        // Find the module from the study programme.
        SisuNode node = studyProgramme.findNodeInTree(moduleGroupId);
        if (node == null) { return false; }

        // Then find the course from the module.
        Course course = node.findCourse(courseGroupId);
        if (course == null) { return false; }

        // Remove the course from the module.
        return node.removeChosenCourse(courseGroupId);
    }

    /**
     * Calculates the student's total completed course credits.
     * 
     * @return Integer of the total count of credits.     
     */
    public Integer calculateTotalCompletedCredits() {  
        return countCourseCredits(studyProgramme);

    }

    /**
     * Calculates the module's completed course credits recursively.
     * 
     * @param module SisuNode
     * @param totalCourseCredits Integer of the current count of credits.
     * @return Integer of the current count of credits.
     */
    private Integer countCourseCredits(SisuNode module) {
        Integer totalCredits = 0;

        // Accumulate credits for courses in the current module.
        for (Course course : module.getChildCourses().values()) {
            if (course.getGrade() != null) {
                if (course.getGrade().equals("fail")) {
                    continue;
                }
                totalCredits += course.getTargetCredits();
            }
        }
    
        // Recursively count credits in child modules.
        for (SisuNode childModule : module.getChildModules()) {
            totalCredits += countCourseCredits(childModule);
        }
    
        return totalCredits;

    }
    
    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStudyProgrammeGroupID() {
        return studyProgrammeGroupID;
    }

    public void setStudyProgrammeGroupID(String studyProgrammeGroupID) {
        this.studyProgrammeGroupID = studyProgrammeGroupID;
    }

    public String getStudyProgrammeName() {
        return studyProgrammeName;
    }

    public void setStudyProgrammeName(String studyProgrammeName) {
        this.studyProgrammeName = studyProgrammeName;
    }

    public Optional<SisuNode> getStudyProgramme() {
        return Optional.ofNullable(studyProgramme);
    }

    public void setStudyProgramme(SisuNode studyProgramme) {
        this.studyProgramme = studyProgramme;
    }

    @Override
    public String toString() {
        return "Student saveObject [studentID=" + studentID + ", firstName=" + firstName + ", lastName=" + lastName
                + ", studyProgramme=" + studyProgramme + "]";
    }    
}