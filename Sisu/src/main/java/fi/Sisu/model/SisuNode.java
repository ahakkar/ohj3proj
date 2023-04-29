package fi.Sisu.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class for Sisu Nodes. Nodes are elements in a tree, which can contain parent-
 * and subnodes. Nodes represent any module from Sisu (DegreeProgramme,
 * StudyModule or
 * GroupingModule), all of which have slight differences in attributes and
 * semantics, but ultimately mostly the same structure and attributes.
 * 
 * Data can come from Sisu or from file. Json is parsed to this class, which
 * then can be used to create the tree.
 * 
 * Because of the slight differences, inheritance is not used. Instead, this
 * one node can be used for all three types of nodes. This reduces complexity
 * and makes the code more readable and maintainable.
 * 
 * @author Kilian Kugge
 * @author Antti Hakkarainen (rewrite to one class)
 */
public class SisuNode {
    /**
     * Name of the node.
     */
    @JsonProperty("name")
    private String name;

    /**
     * Abbreviation of the node.
     */
    @JsonProperty("abbreviation")
    private String abbreviation;

    /**
     * GroupId of the node. Used as the unique identifier.
     */
    @JsonProperty("groupId")
    private String groupId;

    /**
     * List of child nodes.
     */
    @JsonProperty("childModules")
    private ArrayList<SisuNode> childModules;

    /**
     * List of child node groupIds. This is used because the process of retrieving
     * the child nodes is done in two steps. First the groupIds are retrieved, then
     * the actual node.
     */
    @JsonProperty("childModuleIds")
    private ArrayList<String> childModuleIds;

    /**
     * Map of child courses. Key is the groupId of the course.
     */
    @JsonProperty("childCourses")
    private HashMap<String, Course> childCourses;

    /**
     * List of mandatory course's groupIds.
     */
    @JsonProperty("mandatoryCourses")
    private HashSet<String> mandatoryCourses;

    /**
     * List of chosen course's groupIds.
     */
    @JsonProperty("chosenCourses")
    private HashSet<String> chosenCourses;

    /**
     * Description of the node.
     */
    @JsonProperty("contentDescription")
    private String contentDescription;

    /**
     * Learning outcomes of the node.
     */
    @JsonProperty("learningOutcomes")
    private String learningOutcomes;

    /**
     * Prerequisites of the node.
     */
    @JsonProperty("prerequisites")
    private String prerequisites;

    /**
     * Max credits possible to attain from the node.
     */
    @JsonProperty("targetCredits")
    private int targetCredits;

    // Empty constructor for jackson
    public SisuNode() {
        childCourses = new HashMap<>();
        childModules = new ArrayList<>();
        childModuleIds = new ArrayList<>();
        mandatoryCourses = new HashSet<String>();
        chosenCourses = new HashSet<String>();
    }

    public SisuNode(String groupId) {
        this();
        this.groupId = groupId;
    }

    /**
     * Searches for a node with the given groupId from the entire SisuNode tree.
     * 
     * @param groupId GroupId of the node to be searched for.
     * @return Node with the given groupId, or null if not found.
     */
    public SisuNode findNodeInTree(String groupId) {

        if (this.groupId.equals(groupId)) {
            return this;
        }

        for (SisuNode module : childModules) {
            SisuNode node = module.findNodeInTree(groupId);
            if (node != null) {
                return node;
            }
        }

        return null;

    }

    /**
     * Searches for a course with the given groupId from the childCourses map, or
     * from the entire SisuNode tree starting from this node. Returns the groupId of
     * the parent node that contains the course, or null if the course is not found.
     *
     * @param courseId GroupId of the course to be searched for.
     * @return GroupId of the parent node that contains the course, or null if the
     *         course is not found.
     */
    public String findCourseParentGroupId(String courseId) {
        if (childCourses.containsKey(courseId)) {
            return groupId;
        }

        for (SisuNode module : childModules) {
            String parentNodeGroupId = module.findCourseParentGroupId(courseId);
            if (parentNodeGroupId != null) {
                return parentNodeGroupId;
            }
        }
        return null;
    }

    /**
     * Searches for a course with the given groupId from the childCourses map.
     * 
     * @param groupId GroupId of the course to be searched for.
     * @return Course with the given groupId, or null if not found.
     */
    public Course findCourse(String groupId) {
        if (childCourses.containsKey(groupId)) {
            return childCourses.get(groupId);
        }
        return null;
    }

    /**
     * Searches for a course with the given groupId from the entire
     * SisuNode tree starting from this node.
     * 
     * @param groupId GroupId of the course to be searched for.
     * @return Course with the given groupId, or null if not found.
     */
    public Course findCourseInTree(String groupId) {
        Course course = findCourse(groupId);
        if (course != null) {
            return course;
        }

        for (SisuNode module : childModules) {
            course = module.findCourseInTree(groupId);
            if (course != null) {
                return course;
            }
        }
        return null;
    }

    /**
     * Deletes all instanses of course. Does not raise error if course is missing.
     * 
     * This does not delete mandatory courses if they are defined in given SisuNodes
     * mandatory courses atribute. Thus it is important that this method is called
     * only on top level
     * (degree program level) object since they have the mandatory courses. If sub
     * SisuNode
     * does not contain mandatory list (as they often dont) this will remove the
     * course regardles.
     * 
     * @param GroupId     non mandatory course
     * @param mandatories Set of mandatory course groupIds. Also empty HashSet can
     *                    be given and it will work.
     * @return true if removal succesfull or there was nothing to delete. Returns
     *         false if course is mandatory.
     */
    public void deleteCourse(String groupId, HashSet<String> mandatories) {
        mandatories.addAll(this.getAllMandatoryCourses());
        if (mandatories.contains(groupId)) {
            System.err.println("Trying to delete or move mandatory course. You can't do that! Aborting!");
            return;
        }
        this.getChildCourses().remove(groupId);
        for (SisuNode node : this.getChildModules()) {
            node.deleteCourse(groupId, mandatories);
        }
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

    public ArrayList<SisuNode> getChildModules() {
        return childModules;
    }

    public void setChildModules(ArrayList<SisuNode> childModules) {
        this.childModules = childModules;
    }

    public void addChildModule(SisuNode module) {
        childModules.add(module);
    }

    /**
     * Adds a list of child modules to the module.
     * 
     * @param modules List of modules to be added.
     */
    public void addChildModules(ArrayList<SisuNode> nodes) {
        childModules.addAll(nodes);
    }

    public ArrayList<String> getChildModuleIds() {
        return childModuleIds;
    }

    public void setChildModuleIds(ArrayList<String> childModuleIds) {
        this.childModuleIds = childModuleIds;
    }

    public void addChildModuleId(String moduleId) {
        childModuleIds.add(moduleId);
    }

    public HashMap<String, Course> getChildCourses() {
        return childCourses;
    }

    public void setChildCourses(HashMap<String, Course> childCourses) {
        this.childCourses = childCourses;
    }

    public void addChildCourse(Course course) {
        childCourses.put(course.getGroupId(), course);
    }

    /**
     * Updates a child course in the module. Course has to be already in the
     * module.
     * 
     * @param course Course to be updated.
     */
    public void updateChildCourse(Course course) {
        if (course == null || !childCourses.containsKey(course.getGroupId())) {
            return;
        }
        childCourses.put(course.getGroupId(), course);
    }

    /**
     * Updates a list of child courses in the module. Courses have to be already
     * in the module.
     * 
     * @param courses List of courses to be updated.
     */
    public void updateChildCourses(ArrayList<Course> courses) {
        for (Course course : courses) {
            updateChildCourse(course);
        }
    }

    public HashSet<String> getMandatoryCourses() {
        return mandatoryCourses;
    }

    public void setMandatoryCourses(HashSet<String> mandatoryCourses) {
        this.mandatoryCourses = mandatoryCourses;
    }

    public void addMandatoryCourse(String courseId) {
        if (this.childCourses.containsKey(courseId)) {
            mandatoryCourses.add(courseId);
        }
    }

    public boolean removeMandatoryCourse(String courseId) {
        return mandatoryCourses.remove(courseId);
    }

    public HashSet<String> getChosenCourses() {
        return chosenCourses;
    }

    public void setChosenCourses(HashSet<String> chosenCourses) {
        this.chosenCourses = chosenCourses;
    }

    public void addChosenCourse(String courseId) {
        if (this.childCourses.containsKey(courseId)) {
            this.chosenCourses.add(courseId);
        }
    }

    public boolean removeChosenCourse(String courseId) {
        return this.chosenCourses.remove(courseId);
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

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "SisuNode saveObject [programme=" + name + ", chosenCourses=" + chosenCourses
                + "]";
    }

    /**
     * Returns all mandatory courses of the node and its children.
     * @return set of mandatory course groupId's or empty set if none is found.
     */
    public HashSet<String> getAllMandatoryCourses() {
        HashSet<String> set = new HashSet<>();
        set.addAll(this.getMandatoryCourses());

        for (SisuNode node: this.getChildModules()) {
            set.addAll(node.getAllMandatoryCourses());
        }
        return set;
    }
}