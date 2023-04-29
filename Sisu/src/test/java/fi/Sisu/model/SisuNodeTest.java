package fi.Sisu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
public class SisuNodeTest {

    @Test
    public void addChildCourseTest() {
        SisuNode node = new SisuNode("nodeId");
        Course course = new Course("courseId");
        node.addChildCourse(course);

        assertTrue(node.getChildCourses().size() == 1);
        assertTrue(node.getChildCourses().values().contains(course));
        assertTrue(node.getMandatoryCourses().isEmpty());
        assertTrue(node.getChosenCourses().isEmpty());
    }

    @Test
    public void findCourseInTreeTest() {
        SisuNode node = new SisuNode("nodeId");
        Course course = new Course("courseToFind");

        // Create and populate a few nodes and courses in a loop
        for (int i = 0; i < 5; i++) {
            SisuNode childNode = new SisuNode("node" + i);
            Course childCourse = new Course("course" + i);
            childNode.addChildCourse(childCourse);
            node.addChildModule(childNode);

            // Add the course to the node we want to find
            if (i == 3) {
                node.addChildCourse(course);
            }
        }
        // Find the course
        Course foundCourse = node.findCourseInTree("courseToFind");
        assertEquals(course, foundCourse);
        assertEquals("courseToFind", foundCourse.getGroupId());
    }
    
}
