package fi.Sisu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import fi.Sisu.datasource.ApiDataSource;


public class CourseTest {
    @Test
    public void testCourseGettersAndSetters() {
        ApiDataSource source = new ApiDataSource();
        Optional<Course> course = source.getCourse("uta-ykoodi-26621");
        assertTrue(course.isPresent(), "The Course was not found");

        course.ifPresent(action -> {
            action.setGrade("pass");
            action.setContentDescription("Description");
            action.setLearningOutcomes("Outcome");

            assertEquals("Description", action.getContentDescription());
            assertEquals("Outcome", action.getLearningOutcomes());
            assertEquals(false, action.getGraded());
            assertEquals("pass", action.getGrade());
        
        });
        
    }

    @Test
    public void testCourseToString() {
        ApiDataSource source = new ApiDataSource();
        Optional<Course> course = source.getCourse("uta-ykoodi-26621");
        assertTrue(course.isPresent(), "The Course was not found");

        course.ifPresent(action -> {
            assertEquals("LANG.SUV.001, Suomi 1, 3op", action.toString());
        });
    }    
}
