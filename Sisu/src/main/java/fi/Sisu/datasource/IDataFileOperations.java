package fi.Sisu.datasource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import fi.Sisu.model.Student;

/**
 * Interface for data file operations. Used because we can now use Mockito
 * to mock the interface and test the FileDataSource class without actually
 * creating any files to disk while running tests.
 * 
 * @author Antti Hakkarainen
 */
public interface IDataFileOperations {


    /**
     * Loads all students from JSON files.
     * 
     * @param listOfFiles List of files to be loaded.
     * @return List<Student> of all students found
     */
    public List<Student> getStudentsFromFiles(Optional<File[]> listOfFilesOptional);


    /**
     * Creates a student folder to disk, if it does not exist. Folder is used
     * to save student .json files.
     * 
     * @param studentsDirectory
     * @throws IOException
     */
    public void createStudentsFolderIfNotExists(Path studentsDirectory) throws IOException;


    /**
     * Initializes a new datastorage file. Creates a new file for studentid
     * if one doesn't exist yet. If file already exists, does nothing.
     * 
     * @return Boolean success of the operation.
     */
    public File initializeDataFile(String studentId);

    /**
     * Loads student data from JSON file, if data exists. 
     * 
     * @return Student object with data if found, otherwise an empty Student object.
     */
    public Student loadStudentDataFromFile(String studentId);

    /**
     * Saves student data to JSON file. Overwrites existing data.
     * 
     * @param student Student object to be saved.
     * @return Boolean value of whether or not the save was successful.
     */
    public Boolean saveStudentDataToFile(Student student);

    /**
     * Returns boolean value of whether or not student is in the file.
     * 
     * @param studentID studentId is case sensitive.
     * @return Boolean value of student existing or not
     */
    public Boolean studentDataFileExists(String studentId);
    
}
