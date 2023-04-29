package fi.Sisu.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class for Sisu Courses. Courses are elements contained in SisuNodes
 * that represent a course. These are the leaf nodes of the tree. 
 */
public class Course {
    /**
     * Name of the course.
     */
    @JsonProperty("name")
    private String name;

    /**
     * Abbreviation of the course.
     */
    @JsonProperty("abbreviation")
    private String abbreviation;

    /**
     * GroupId of the course. Used as the unique identifier.
     */
    @JsonProperty("groupId")
    private String groupId;

    /**
     * Description of the course.
     */
    @JsonProperty("contentDescription")
    private String contentDescription;

    /**
     * Learning outcomes of the course.
     */
    @JsonProperty("learningOutcomes")
    private String learningOutcomes;

    /**
     * Prerequisites of the course.
     */
    @JsonProperty("prerequisites")
    private String prerequisites;

    /**
     * Max credits possible to attain from the course.
     */
    @JsonProperty("targetCredits")
    private int targetCredits;  

    /**
     * Boolean value for whether the course is graded or not. If it is graded,
     * it means a grade of "fail" or between 1 and 5 is possible, otherwise it is
     * only "pass"/"fail".
     */
    @JsonProperty("graded")
    private boolean graded;

    /**
     * Grade of the course. If the course is graded, this is "pass" or a number 
     * between 1 and 5. Otherwise it is either "pass" or "fail".
     */
    @JsonProperty("grade")
    private String grade;

    public Course() {
        // Empty constructor for jackson
    }

    /**
     * Constructor for Course, requires only the groupId.
     * @param groupId
     */
    public Course(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public String getLearningOutcomes() {
        return learningOutcomes;
    }

    public void setLearningOutcomes(String learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public int getTargetCredits() {
        return targetCredits;
    }

    public void setTargetCredits(int targetCredits) {
        this.targetCredits = targetCredits;
    }

    public boolean isGraded() {
        return graded;
    }

    public boolean getGraded() {
        return graded;
    }

    public void setGraded(boolean graded) {
        this.graded = graded;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return abbreviation + ", " + name +  ", "  + targetCredits + "op" ;
    }
}
