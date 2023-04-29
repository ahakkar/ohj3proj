package fi.Sisu.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.Sisu.model.Course;
import fi.Sisu.model.SisuNode;

/**
 * Class that takes in Kori API JSON data as strings and parses it into the
 * correct classes.
 * 
 * @author Kilian Kugge
 */
public class KoriJSONParser {

    public KoriJSONParser () {
        
    }


    /**
     * Parses a JSON string containing Degree Programme info into a list of
     * DegreeProgramme objects.
     * 
     * @param json JSON string to parse.
     * @return ArrayList of DegreeProgramme objects.
     */
    public ArrayList<SisuNode> parseAllStudyProgrammes(String json) {
        
        JsonNode root = getJsonRoot(json);
        if (root == null){ 
            return null;
        }

        ArrayList<SisuNode> studyProgrammes = new ArrayList<SisuNode>();

        for (JsonNode dp : root.get("searchResults")) {
            SisuNode sisuNode = new SisuNode(); 
            sisuNode.setName(dp.get("name").asText());
            sisuNode.setAbbreviation(dp.get("code").asText());
            sisuNode.setGroupId(dp.get("groupId").asText());
            sisuNode.setTargetCredits(dp.path("credits").get("min").asInt());

            studyProgrammes.add(sisuNode);
        }
        return studyProgrammes;
    }


    /**
     * Parses a JSON string into a Course object.
     * 
     * @param json JSON string to parse.
     * @return Course object.
     */
    public Course parseCourse(String json) {
        
        JsonNode root = getJsonRoot(json);
        if (root == null)
            return null;

        boolean graded = false;
        if (root.get("gradeScaleId").asText().equals("sis-0-5"))
            graded = true;

        Course course = new Course();
        course.setName(checkLanguageOrNull(root.path("name")));
        course.setAbbreviation(root.get("code").asText());
        course.setGroupId(root.get("groupId").asText());
        course.setTargetCredits(root.path("credits").get("max").asInt());
        course.setGraded(graded);
        course.setContentDescription(checkLanguageOrNull(root.path("content")));
        course.setLearningOutcomes(checkLanguageOrNull(root.path("outcomes")));
        course.setPrerequisites(checkLanguageOrNull(root.path("prerequisites")));      

        return course;
    }


    /**
     * Parses a JSON string of course search results into a Course object. This is 
     * a separate function because the JSON structure for search results is 
     * different from normal course JSON, mainly it has less information. 
     * @param json Json string to parse.
     * @return ArrayList of Course objects.
     */
    public ArrayList<Course> parseCourseSearchResults(String json) {
        
        JsonNode root = getJsonRoot(json);
        if (root == null)
            return null;
        
        // The search results are stored in a "searchResults" node.
        JsonNode nodes = root.get("searchResults");
        
        // Turn the nodes into a stream and map them to Course objects.
        // JsonNode implements iterator which means we can turn it into a stream
        // with it's spliterator.
        return (ArrayList<Course>)
                    StreamSupport.stream(nodes.spliterator(), true)
                    .map(node -> parseSingleSearchResultCourse(node))
                    .collect(Collectors.toList());
    }


    /**
     * Parses a JSON string into an incomplete (no children yet) DegreeProgramme.
     * 
     * @param json JSON string to parse.
     * @return DegreeProgramme object.
     */
    private SisuNode parseDegreeProgramme(JsonNode root) {
        SisuNode sisuNode = new SisuNode();
        sisuNode.setName(checkLanguageOrNull(root.path("name")));
        sisuNode.setAbbreviation(root.get("code").asText());
        sisuNode.setGroupId(root.get("groupId").asText());
        sisuNode.setContentDescription(checkLanguageOrNull(root.path("contentDescription")));
        sisuNode.setLearningOutcomes(checkLanguageOrNull(root.path("learningOutcomes")));
        sisuNode.setTargetCredits(root.path("targetCredits").get("min").asInt());
 
        return sisuNode;
    }


    /**
     * Gets the content description and learning outcomes of a degree programme
     * from a JSON string. 
     * 
     * @param json JSON string to parse.
     * @return String array containing 1. the content description and 2. the learning outcomes.
     */
    public String[] parseDegreeProgrammeDescriptions (String json) {
        JsonNode root = getJsonRoot(json);
        if (root == null)
            return null;

        String[] descriptions = new String[2];
        descriptions[0] = checkLanguageOrNull(root.path("contentDescription"));
        descriptions[1] = checkLanguageOrNull(root.path("learningOutcomes"));
        return descriptions;
    }


    /**
     * Parses a JSON string into an incomplete (no children yet) GroupingModule.
     * 
     * @param json JSON string to parse.
     * @return GroupingModule object.
     */
    private SisuNode parseGroupingModule(JsonNode root) {
        SisuNode sisuNode = new SisuNode();
        sisuNode.setName(checkLanguageOrNull(root.path("name")));
        sisuNode.setAbbreviation(root.get("code").asText());
        sisuNode.setGroupId(root.get("groupId").asText());
 
        return sisuNode;
    }


    /**
     * Parses a JSON string into a Module object.
     * @param json JSON string to parse.
     * @return Module object. Null if parsing failed for some reason.
     */
    public SisuNode parseModule(String json) {

        // Get the root node of the JSON.
        JsonNode root = getJsonRoot(json);
        if (root == null) 
            return null;

        // Parse the module based on its type.
        String type = root.get("type").asText();
        SisuNode module = null;
     
        if (type.equals("StudyModule")) {
            module = parseStudyModule(root);
        } 
        else if (type.equals("GroupingModule")) {
            module = parseGroupingModule(root);
        }
        else if (type.equals("DegreeProgramme")) {
            module = parseDegreeProgramme(root);
        }
        else {
            return null;
        }

        // Get the top composite rule where the child modules and courses will be.
        JsonNode compositeRule = getTopCompositeRule(root);
        if (compositeRule == null) {
            return module;
        }

        // Add the child module and course IDs to module and return.
        recurseRules(module, compositeRule.get("rules"), 
                    compositeRule.get("allMandatory").asBoolean());

        return module;
    }


    /**
     * Parses a single course search result into a Course object.
     * @param courseInfo
     * @return
     */
    private Course parseSingleSearchResultCourse (JsonNode courseInfo) {
        Course course = new Course(courseInfo.get("groupId").asText());
        course.setName(courseInfo.get("name").asText());
        course.setAbbreviation(courseInfo.get("code").asText());

        Integer min = courseInfo.path("credits").get("min").asInt();
        Integer max = courseInfo.path("credits").get("max").asInt();

        // Sometimes the max value can be null or 0, and in that case the min value
        // should be used, but otherwise use max. 
        if ((max != 0 && max != null)) {
            course.setTargetCredits(max);
        }
        else {
            course.setTargetCredits(min);
        }

        return course;
    }


    /**
     * Parses a JSON string into an incomplete (no children yet) StudyModule.
     * 
     * @param json JSON string to parse.
     * @return StudyModule object.
     */
    private SisuNode parseStudyModule(JsonNode root) {
        SisuNode sisuNode = new SisuNode();
        sisuNode.setName(checkLanguageOrNull(root.path("name")));
        sisuNode.setAbbreviation(root.get("code").asText());
        sisuNode.setGroupId(root.get("groupId").asText());
        sisuNode.setContentDescription(checkLanguageOrNull(root.path("contentDescription")));
        sisuNode.setLearningOutcomes(checkLanguageOrNull(root.path("learningOutcomes")));
        sisuNode.setPrerequisites(checkLanguageOrNull(root.path("prerequisites")));
        sisuNode.setTargetCredits(root.path("targetCredits").get("min").asInt());
 
        return sisuNode;
    }


    
    /**
     * Helper function for checking fields that may be in either Finnish or 
     * English and are not realiably included in the Kori API JSON files.
     * 
     * @param field JSON node to check.
     * @return String containing the field's value or null if the field is null.
     */
    private String checkLanguageOrNull(JsonNode field) {

        if (field.isNull()) {
            return null;
        }

        if (field.has("fi")) {
            return field.get("fi").asText();
        }
        
        if (field.has("en")) {
            return field.get("en").asText();
        }

        // If for some reason the field is not null but doesn't have either
        // language, return null anyway.
        return null;
    }


    /**
     * Reads a JSON string into a jackson node tree structure and returns the 
     * root node. 
     * 
     * @param json JSON string to parse.
     * @return Root node of the JSON tree. Null if parsing failed.
     */
    private JsonNode getJsonRoot(String json) {
        JsonNode root = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            root = mapper.readTree(json);
        }
        catch (IOException e) {
            System.err.println(e);
            return null;
        }

        // If the JSON's outermost node is a container, get the first child.
        // This is done because most if not all the Kori API JSON's have a
        // useless outer container node.
        if (root.isArray()) {
            root = root.get(0);
        }

        return root;
    }


    /**
     * Returns the top "CompositeRule" node of a Kori JSON file which contains
     * the child modules and courses and possibly other CompositeRules.
     * 
     * @param json JSON string to parse.
     * @return "rules" node.
     */
    private JsonNode getTopCompositeRule(JsonNode root) {
        
        // The child modules and courses are stored in "rule" nodes which are
        // stored in a "rules" node of a "CompositeRule" node.

        // First get the topmost rule node which all Kori JSON's should have.
        JsonNode rule = root.get("rule");

        // A Credits rule has nothing useful to us here except the next rule node.
        if (rule.get("type").asText().equals("CreditsRule"))
            rule = rule.get("rule");

        if (rule.get("type").asText().equals("CompositeRule"))
            return rule;
        
        // If there is no CompositeRule, return null.
        return null;
    }

    /**
     * Recursively parses the "rules" nodes of a Kori JSON file and adds the
     * child modules and courses to the given module.
     * @param module The parent module to add the children to.
     * @param ruleSet The "rules" node to parse.
     * @param coursesMandatory Whether the courses in this rule set are mandatory.
     */
    private void recurseRules(SisuNode sisuNode, JsonNode ruleSet, Boolean coursesMandatory) {

        for (JsonNode rule : ruleSet) {

            // If the rule is a CompositeRule, recurse.
            if (rule.get("type").asText().equals("CompositeRule")) {
                // Remember to pass the allMandatory value to the next recursion.
                Boolean allMandatory = rule.get("allMandatory").asBoolean();
                recurseRules(sisuNode, rule.get("rules"), allMandatory);
                continue;
            }

            // If the rule is a ModuleRule, add as child module.
            if (rule.get("type").asText().equals("ModuleRule")) {
                sisuNode.addChildModuleId(rule.get("moduleGroupId").asText());
                continue;
            }

            // If the rule is a CourseUnitRule, add as child course.
            if (rule.get("type").asText().equals("CourseUnitRule")) {
                String id = rule.get("courseUnitGroupId").asText();      
                sisuNode.addChildCourse(new Course(id));

                if (coursesMandatory) {
                    sisuNode.addMandatoryCourse(id);
                }
            }
        }

    }

}
