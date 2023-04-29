package fi.Sisu.datasource;

import java.util.List;
import java.util.Optional;

import fi.Sisu.model.Student;

/**
 * Interface for saving and loading data from disk.
 * Used by UI's viewmodels.
 * 
 * @author Antti Hakkarainen
 */
public interface IFileDataSource {

    /**
     * Returns a list of all students.
     * 
     * @return List<Student>
     */
    public List<Student> getAllStudents();


    /**
     * Returns a student object with the given student ID.
     * 
     * @param studentId The student ID of the student to be returned.
     * @return Student object with the given student ID.
     */
    public Optional<Student> getStudent(String studentId);


    /**
     * Edits student's first and last name.
     * TODO combine with addStudent?
     * 
     * @param studentID ID of the student to be edited.
     * @param firstName New first name.
     * @param lastName New last name.
     * @return Boolean is operation successful.
     */
    public Boolean editStudent(String studentId, String firstName, String lastName);


    /**
     * Saves the changes made to a student object to a JSON file.
     * 
     * @param student The Student object with updated information.
     * @throws IOException If there is an issue writing to the JSON file.
     */
    public Boolean saveStudent(Student student);


    /**
     * Adds student to the json file.
     * 
     * @param studentID
     * @param firstName
     * @param lastName
     * @return boolean value of wheter or not the add was succesful.
     */
    public Boolean addStudent(String studentId, String firstName, String lastName);


    /**
     * Deletes student's JSON file.
     * 
     * @param studentID ID of the student to be deleted.
     * @return Boolean is operation successful.
     */
    public Boolean deleteStudent(String studentId);


    /**
     * Checks if a student data file exists for the given student ID.
     * @param studentId String
     * @return Boolean does student exist?
     */
    public Boolean studentExists(String studentId);
}
