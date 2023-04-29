package fi.Sisu.datasource;

import java.util.ArrayList;
import java.util.Optional;

import fi.Sisu.model.Course;
import fi.Sisu.model.SisuNode;

/**
 * Interface for retrieving data from Sisu API.
 * Used by UI's viewmodels.
 * 
 * @author Antti Hakkarainen
 */
public interface IApiDataSource {

    /**     
     * Returns a Course object with the given groupId.
     * Contains all the information about the course.
     * 
     * @param groupId String of the groupId of the course.
     * @return Course object
     */
    public Optional<Course> getCourse(String groupId);


    /**
     * Returns a StudyProgramme object with the given groupId.
     * Contains all the information about the study programme.
     * 
     * @param groupId String of the groupId of the study programme.
     * @return StudyProgramme object
     */
    public Optional<SisuNode> getStudyProgramme(String groupId);


    /**
     * Retrieves all study programmes from Sisu.
     * 
     * @return List<StudyProgramme>
     */
    public Optional<ArrayList<SisuNode>> getAllStudyProgrammes();


    /**
     * Searches for TUNI courses with the given keyword.
     * @param keyword String of the keyword.
     * @return ArrayList of Course objects.
     */
    public Optional<ArrayList<Course>> searchForCourses(String keyword);
    
}
