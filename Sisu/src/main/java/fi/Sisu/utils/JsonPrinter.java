package fi.Sisu.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import fi.Sisu.model.Course;
import fi.Sisu.model.SisuNode;

public class JsonPrinter {

    /**
     * Returns the SisuNode javaobject as a json string. This can be used for
     * saving the data to file.
     * 
     * @param SisuNode node
     * @return String json representation of the SisuModule
     */
    public String getJsonFromSisuModuleAsString(SisuNode node) {
        ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    
        try {
            String jsonString = objectMapper.writeValueAsString(node);
            return jsonString;
        }
        catch (MismatchedInputException e) {
            System.err.println("JSON structure mismatch: " + e.getMessage());
            e.printStackTrace();        
        } 
        catch (JsonProcessingException e) {
            System.err.println("Invalid JSON content: " + e.getMessage());
            e.printStackTrace();
        } 
        
        return null;
    }    

    /**
     * Returns the Course javaobject as a json string. This can be used for
     * saving the data to file.
     * 
     * @param Course course
     * @return String json representation of the Course
     */
    public String getJsonFromCourseAsString(Course course) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    
        try {
            String jsonString = objectMapper.writeValueAsString(course);
            return jsonString;
        }
        catch (MismatchedInputException e) {
            System.err.println("JSON structure mismatch: " + e.getMessage());
            e.printStackTrace();        
        } 
        catch (JsonProcessingException e) {
            System.err.println("Invalid JSON content: " + e.getMessage());
            e.printStackTrace();
        } 
        
        return null;
    }
}
