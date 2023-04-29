package fi.Sisu.datasource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import fi.Sisu.app.Constants;
import fi.Sisu.model.Student;
import fi.Sisu.utils.MyJavaFXUtils;
import fi.Sisu.utils.ObjectMapperFactory;
import javafx.scene.control.Alert.AlertType;

/**
 * FileDataSource is a class that handles all file operations for the application.
 * Rewritten to use a separate file for each student, and to use an interface 
 * instead of exposing everything to the application.
 * 
 * @author Heikki Hohtari
 * @author Antti Hakkarainen (rewrite)
 */
public class FileDataSource implements IFileDataSource, IDataFileOperations {

    private final String filePrefix = "student_";
    private final String fileExtension = ".json";
    private final String studentsFolderPath = "students";

    @Override
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();        
    
        // Create a 'students' folder if it does not exist
        try {
            Path studentsDirectory = Paths.get(studentsFolderPath);
            createStudentsFolderIfNotExists(studentsDirectory);
        } 
        // Return empty list if folder creation failed
        catch (IOException e) {
            e.printStackTrace();
            return students; 
        }
    
        // Get all files in the students folder
        File folder = new File(studentsFolderPath);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json")); 
        
        // There may be zero or more files in the folder, we can't know for sure
        Optional<File[]> listOfFilesOptional = Optional.ofNullable(listOfFiles);
        students.addAll(getStudentsFromFiles(listOfFilesOptional));
    
        return students;
    }


    @Override
    public Optional<Student> getStudent(String studentId) {
        isStudentIdValid(studentId);
        if (!this.studentDataFileExists(studentId)) {
            return Optional.empty();
        }
        return Optional.of(loadStudentDataFromFile(studentId));
    }


    @Override
    public Boolean editStudent(String studentId, String firstName, String lastName) {
        isStudentIdValid(studentId);
        Student student = this.loadStudentDataFromFile(studentId);

        student.setFirstName(firstName);
        student.setLastName(lastName);

        return this.saveStudentDataToFile(student);
    }


    @Override
    public Boolean saveStudent(Student student) {
        return this.saveStudentDataToFile(student);
    }


    @Override
    public Boolean addStudent(String studentId, String firstName, String lastName) {
        isStudentIdValid(studentId);
        // Check if a student already exists with the given ID
        if (this.studentDataFileExists(studentId)) {
            System.err.println("Student with given ID already exists!");
            return false;
        }

        // Otherwise create new Student and save data
        Student student = new Student();

        student.setStudentID(studentId);
        student.setFirstName(firstName);
        student.setLastName(lastName);

        return this.saveStudentDataToFile(student);
    }


    @Override
    public Boolean deleteStudent(String studentId) {
        isStudentIdValid(studentId);   
        String studentDataFileName = getStudentDataFileName(studentId);
        Path studentDataFilePath = Paths.get(studentDataFileName);        
    
        // Check if the file exists before attempting to delete it
        if (Files.exists(studentDataFilePath)) {
            try {
                Files.delete(studentDataFilePath);
                return true;
            } catch (IOException e) {
                System.err.println("Could not delete student data file: " + studentDataFileName);
                e.printStackTrace();                
            }
        }

        System.out.println("Delete was successful");

        // File does not exist, so there's nothing to delete
        return false;        
    }


    @Override
    public Boolean studentExists(String studentId) {
        isStudentIdValid(studentId);   
        return this.studentDataFileExists(studentId);
    };


    @Override
    public List<Student> getStudentsFromFiles(Optional<File[]> listOfFiles) {
        List<Student> students = new ArrayList<>();
    
        if (listOfFiles.isPresent()) {
            for (File file : listOfFiles.get()) {
                String fileName = file.getName();
                String studentId = fileName.substring(0, fileName.lastIndexOf(".json")).replace("student_", "");
    
                Optional<Student> student = getStudent(studentId);
                student.ifPresent(students::add);             
            }
        }
    
        return students;
    }    


    @Override
    public void createStudentsFolderIfNotExists(Path studentsDirectory) throws IOException {
        if (!Files.exists(studentsDirectory)) {
            try {
                Files.createDirectory(studentsDirectory);
            } catch (IOException e) {
                System.err.println("Could not create students/ folder.");
                throw e;
            }
        }
    }


    @Override
    public File initializeDataFile(String studentId) {
        isStudentIdValid(studentId);   
        File file = new File(getStudentDataFileName(studentId));

        try {
            file.createNewFile();
        }
        catch (IOException e) {
            System.err.println("Could not read or create .json for student: " + studentId +".");
            e.getMessage();
        }

        return file;
    }
    

    @Override
    public Student loadStudentDataFromFile(String studentId) {
        isStudentIdValid(studentId);        

        Student student = new Student();        
        ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();

        try {                        
            student = objectMapper.readValue(this.initializeDataFile(studentId), Student.class);
        }
        catch (MismatchedInputException e) {
            String errMsg = "JSON structure mismatch in student:\n"
                + getStudentDataFileName(studentId) + "\n" +  e.getMessage();
            MyJavaFXUtils.displayAlertWithMoreInfo(
                AlertType.ERROR,
                "Error",
                "Error while loading student JSON:",
                "Student data is corrupted or invalid.",
                errMsg
            );

            System.err.println("JSON structure mismatch: " + e.getMessage());
            e.printStackTrace();
        }
        catch (JsonProcessingException e) {
            String errMsg = "Invalid JSON content in student:\n" 
                + getStudentDataFileName(studentId) + "\n" +  e.getMessage();

            MyJavaFXUtils.displayAlertWithMoreInfo(
                AlertType.ERROR,
                "Error",
                "Error while loading student JSON",
                "Student data is corrupted or invalid.",
                errMsg
            );

            System.err.println("Invalid JSON content: " + e.getMessage());
            e.printStackTrace();         
        } 
        catch (IOException e) {
            String errMsg = "An I/O error occurred in student:\n" 
                + getStudentDataFileName(studentId) + "\n" +  e.getMessage();

            MyJavaFXUtils.displayAlertWithMoreInfo(
                AlertType.ERROR,
                "Error",
                "Error while loading student JSON",
                "Student data is corrupted or invalid.",
                errMsg
            );

            System.err.println("An I/O error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return student;
    }


    @Override
    public Boolean saveStudentDataToFile(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }

        ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(this.initializeDataFile(student.getStudentID()), student);
        }
        catch (JsonProcessingException e) {
            String errMsg = "Invalid data content with student:\n" + student.getStudentID();    

            MyJavaFXUtils.displayAlertWithMoreInfo(
                AlertType.ERROR,
                "Error",
                "Error while saving student data to JSON",
                "Student data is corrupted or invalid.",
                errMsg
            );

            System.err.println("Invalid data content: " + e.getMessage());
            e.printStackTrace();

            return false;
        }
        catch (IOException e) {
            String errMsg = "An I/O error occurred with student:\n" + student.getStudentID();    

            MyJavaFXUtils.displayAlertWithMoreInfo(
                AlertType.ERROR,
                "Error",
                "Error while saving student data to JSON",
                "Student data is corrupted or invalid.",
                errMsg
            );
            
            System.err.println("An I/O error occurred: " + e.getMessage());
            e.printStackTrace();

            return false;
        }

        return true;
    }


    @Override
    public Boolean studentDataFileExists(String studentId) {
        isStudentIdValid(studentId);   
        String fileName = this.getStudentDataFileName(studentId); 
        File file = new File(fileName);

        return file.exists();
    }


    /**
     * Construct a file name for a student data file.
     * 
     * @param studentId
     * @return
     */
    private String getStudentDataFileName(String studentId) {
        isStudentIdValid(studentId);   
        return studentsFolderPath + File.separator + filePrefix + studentId + fileExtension;
    }

    private void isStudentIdValid(String studentId) {
        if (studentId == null || studentId.isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty.");
        }
        if    (studentId.length() < Constants.STUDENT_ID_MIN_LENGTH 
            || studentId.length() > Constants.STUDENT_ID_MAX_LENGTH) {
            throw new IllegalArgumentException("Student ID must be between 3 and 16 characters.");
        }
        if (!studentId.matches(Constants.STUDENT_ID_ALLOWED_REGEXP)) {
            throw new IllegalArgumentException("Student ID can only contain alphanumeric characters (a-z, A-Z, 0-9).");
        }
    }

}
