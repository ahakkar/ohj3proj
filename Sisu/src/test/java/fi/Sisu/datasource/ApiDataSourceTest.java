package fi.Sisu.datasource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import fi.Sisu.model.Course;
import fi.Sisu.model.SisuNode;


/**
 * Tests for ApiDataSource class.
 * 
 * @author Antti Hakkarainen
 */
public class ApiDataSourceTest {

    private ApiDataSource apiDataSource;

    @BeforeEach
    public void setUp() {
        this.apiDataSource = new ApiDataSource();
    }

    @Test
    public void retrieveNodeTest() {
        Optional<Course> course = apiDataSource.retrieveCourseNode("otm-c7233e84-953d-4e97-b4c4-8343702494c1");
        assertTrue(course.isPresent(), "The Course was not found");

        course.ifPresent(action -> {
            assertEquals(action.getName(), "Academic Writing B");
            assertEquals(action.getAbbreviation(), "LANG.ENG.012");
            assertEquals(action.getGroupId(), "otm-c7233e84-953d-4e97-b4c4-8343702494c1");
            assertEquals(action.getTargetCredits(), 2);
            assertEquals(action.isGraded(), false);
            assertEquals(action.getPrerequisites(), "<p>LANG.ENG.012 Academic Writing B ja LANG.ENG.011 Academic Writing A ovat \nvaihtoehtoisia; opiskelija voi saada suoritusmerkinnän vain toisesta. \nTarkista oman tutkinto-ohjelmasi tutkintorakenne.</p>");
       
        });
    }

    @Test
    public void retrieveModuleAndPopulateChildrenOneLevelTest() {
        Optional<SisuNode> node = apiDataSource.retrieveModuleNode("uta-tohjelma-1705");
        assertTrue(node.isPresent(), "The SisuNode was not found");

        node.ifPresent(action -> {
            apiDataSource.populateModuleChildrenOneLevel(action);
            assertTrue(action.getChildModules().size() == 5);
            assertTrue(action.getChildCourses().size() == 0);
        });        
    }

    @Test
    public void retrieveModuleTreeRecursiveTest() {
        Optional<SisuNode> node = apiDataSource.getStudyProgramme("uta-tohjelma-1705");
        assertTrue(node.isPresent(), "The SisuNode was not found");

        node.ifPresent(action -> {
            assertEquals(action.getName(), "Tietojenkäsittelytieteiden kandidaattiohjelma");
            assertTrue(action.getChildModules().size() > 0);
            for (SisuNode child : action.getChildModules()) {
                assertTrue(child.getChildModules().size() + child.getChildCourses().size() > 0);
            }
        });
    }

    @Test
    public void retrieveModuleTreeRecursiveTest2() {
        Optional<SisuNode> node = apiDataSource.getStudyProgramme("tut-dp-g-1180");
        assertTrue(node.isPresent(), "The SisuNode was not found");

        node.ifPresent(action -> {
            for (SisuNode child : action.getChildModules()) {
                assertTrue(child.getChildModules().size() + child.getChildCourses().size() > 0);
            }
        });
    }


    @Test
    public void retrieveAllStudyProgrammesTest() {
        Optional<ArrayList<SisuNode>> studyProgrammes = apiDataSource.getAllStudyProgrammes();
        assertTrue(studyProgrammes.isPresent(), "The study programmes were not found");

        studyProgrammes.ifPresent(action -> {
            assertTrue(action.size() == 273);
            assertEquals(action.get(0).getName(), "Akuuttilääketieteen erikoislääkärikoulutus (55/2020)");
            assertEquals(action.get(0).getAbbreviation(), "MEDAAKUEL2020");
        });
    } 
    
    @ParameterizedTest
    @MethodSource("searchForCoursesTestProvider")
    public void searchForCoursesTest(String keyword) {
        Optional<ArrayList<Course>> courses = apiDataSource.searchForCourses(keyword);
        assertTrue(courses.isPresent(), "The courses were not found");
        
        // Check that the amount of courses returned is the same as the amount of courses in the response.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(KoriAPIRequester.requestCoursesWithKeyword(keyword));
        } catch (Exception e) { return; }

        courses.ifPresent(action -> {
            assertEquals(root.get("total").asInt(), action.size());        
            // Check that no null values are returned.
            assertFalse(action.contains(null));
        });
    }

    
    static Stream<Arguments> searchForCoursesTestProvider() {
        // Any keyword with special characters should be null.
        return Stream.of(
            Arguments.of("tietorakenteet"),
            Arguments.of("tiet"),
            Arguments.of("algoritmit"),
            Arguments.of("kaa"),
            Arguments.of("3df03f"),
            Arguments.of("juuh")
        );
    }
}
