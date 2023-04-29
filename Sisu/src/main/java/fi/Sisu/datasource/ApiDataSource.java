package fi.Sisu.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import fi.Sisu.model.Course;
import fi.Sisu.model.SisuNode;

/**
 * Handles Connection and retriaval of data from SISU API. 
 * 
 * @author Kilian Kugge
 */
public class ApiDataSource implements IApiDataSource {

    private static KoriJSONParser parser;

    public ApiDataSource() {
         parser = new KoriJSONParser();
        
    }


    @Override
    public Optional<Course> getCourse(String groupId) {
        return retrieveCourseNode(groupId);
    }


    @Override
    public Optional<SisuNode> getStudyProgramme(String groupId) {
        return retrieveNodeTreeRecursive(groupId);
    }


    @Override
    public Optional<ArrayList<SisuNode>> getAllStudyProgrammes() {
        return retrieveAllStudyProgrammes();
    }



    public Optional<ArrayList<Course>> searchForCourses(String keyword) {        
        if (keyword == null || keyword.isEmpty() || keyword.length() < 3) {
            return null;
        }
        String json = KoriAPIRequester.requestCoursesWithKeyword(keyword);
        if (json == null) {
            return null;
        }
        
        return Optional.ofNullable(parser.parseCourseSearchResults(json));
    }


    /**
     * Populates the immediate child modules and courses of a module object.
     * @param module Module object to populate.
     */
    public void populateModuleChildrenOneLevel (SisuNode module) {
        // Go through the groupId strings of the child modules.
        for (String child : module.getChildModuleIds()) {

            // Parse and add immediate child modules.
            Optional<SisuNode> childModule = retrieveModuleNode(child);
            childModule.ifPresent(action -> module.addChildModule(action)); 
        }

        // Go through the groupId strings of the child courses and update them.
        for (String child : module.getChildCourses().keySet()) {

            Optional<Course> childCourse = retrieveCourseNode(child);

            childCourse.ifPresent(action -> module.updateChildCourse(action));  
        }
    }


     /**
     * Retrieves a full module tree recursively from the module's group id. 
     * 
     * @param groupId groupId of module as a string.
     * @return Module object populated with child modules and courses.
     */
    private Optional<SisuNode> retrieveNodeTreeRecursive(String groupId) { 
        
        // Get the top module.
        Optional<SisuNode> parentModule = retrieveModuleNode(groupId);

        // Recursively parse all child modules with parallel streams.
        // Maps each groupId to a module object and adds them to the top module.
        parentModule.ifPresent(module -> {
            List<String> childModuleIds = module.getChildModuleIds();
            List<SisuNode> childModules = childModuleIds.parallelStream()
                .map(id -> retrieveNodeTreeRecursive(id))
                .flatMap(optionalNode -> optionalNode.stream())
                .collect(Collectors.toList());
            module.addChildModules((ArrayList<SisuNode>) childModules);
        });        

        // Recursively parse all child courses with parallel streams.
        parentModule.ifPresent(module -> {
            Set<String> courseIds = module.getChildCourses().keySet();
            List<Course> courses = courseIds.parallelStream()
                .map(id -> getCourse(id))
                .flatMap(optionalCourse -> optionalCourse.stream())
                .collect(Collectors.toList());
            module.updateChildCourses((ArrayList<Course>) courses);
        });

        return parentModule;
    }


    /**
     * Retrieves a module from Sisu with the given group ID, without child
     * modules or courses.
     * 
     * @param groupId String of the group ID.
     * @return Module object.
     */
    public Optional<SisuNode> retrieveModuleNode(String groupId) {        
        String json = KoriAPIRequester.requestModuleInfo(groupId);
        SisuNode sisuNode = parser.parseModule(json);
        return Optional.ofNullable(sisuNode);
    }


    /**
     * Retrieves a course from Sisu with the given group ID.
     * 
     * @param groupId String of the group ID.
     * @return Module object.
     */
    public Optional<Course> retrieveCourseNode(String groupId) {        
        String json = KoriAPIRequester.requestCourseInfo(groupId);
        Course course = parser.parseCourse(json);
        return Optional.ofNullable(course);
    }



    /**
     * Retrieves all degree programmes from Sisu.
     * @return ArrayList of DegreeProgramme objects.
     */
    private Optional<ArrayList<SisuNode>> retrieveAllStudyProgrammes() {
        String json = KoriAPIRequester.requestDegreeProgrammes();
        return Optional.ofNullable(parser.parseAllStudyProgrammes(json));
    }

} 

