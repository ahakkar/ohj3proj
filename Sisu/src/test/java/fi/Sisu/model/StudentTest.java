package fi.Sisu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import fi.Sisu.datasource.ApiDataSource;

public class StudentTest {

    @Test
    public void testGettersAndSetters() {
        ApiDataSource source = new ApiDataSource();
        Optional<SisuNode> degreeProgramme = source.getStudyProgramme("tut-dp-g-1242");
        assertTrue(degreeProgramme.isPresent(), "The Degree Programme was not found");

        degreeProgramme.ifPresent(programme -> {
            Student student = new Student("123456", "Jim", "Halpert", programme);
            student.setStudyProgrammeGroupID("tut-dp-g-1242");
            student.setStudyProgrammeName("Master's Programme in Electrical Engineering");
    
            assertEquals("123456", student.getStudentID());
            assertEquals("Jim", student.getFirstName());
            assertEquals("Halpert", student.getLastName());
            assertEquals("tut-dp-g-1242", student.getStudyProgrammeGroupID());
            assertEquals("Master's Programme in Electrical Engineering", student.getStudyProgrammeName());
    
            assertTrue(student.setGrade("tut-sm-g-3713", "uta-ykoodi-26621", "pass"));
            assertFalse(student.setGrade(null, "uta-ykoodi-26621", "pass"));
            assertFalse(student.setGrade("tut-sm-g-3713", null, "pass"));
    
        });
    }

    @Test
    public void testToString() {
        ApiDataSource source = new ApiDataSource();
        Optional<SisuNode> degreeProgramme = source.getStudyProgramme("tut-dp-g-1242");
        assertTrue(degreeProgramme.isPresent(), "The Degree Programme was not found");

        degreeProgramme.ifPresent(programme -> {
            Student student = new Student("123456", "Jim", "Halpert", programme);
            String excpected = "Student saveObject [studentID=123456, firstName=Jim, lastName=Halpert, studyProgramme=SisuNode saveObject [programme=Master's Programme in Electrical Engineering, chosenCourses=[]]]";
            assertEquals(excpected, student.toString());
        });
    }

    @Test
    public void testRemoveCourseTest() {
        Student student = new Student();

        SisuNode degreeProgramme = new SisuNode("nodeId");
        Course course = new Course("courseId");
        degreeProgramme.addChildCourse(course);
        degreeProgramme.addChosenCourse(course.getGroupId());

        student.setStudyProgramme(degreeProgramme);

        student.getStudyProgramme().ifPresent(programme -> {
            assertEquals(1, programme.getChosenCourses().size());
        });
        assertTrue(student.removeCourse("nodeId", "courseId"));
        student.getStudyProgramme().ifPresent(programme -> {
            assertTrue(programme.getChosenCourses().isEmpty());
        });  
        assertFalse(student.removeCourse(null, "courseId"));
        assertFalse(student.removeCourse("nodeId", null));
    }
}
