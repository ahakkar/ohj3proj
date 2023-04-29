package fi.Sisu.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.regex.Pattern;

import fi.Sisu.utils.MyJavaFXUtils;
import javafx.scene.control.Alert.AlertType;

/**
 * A static class used to make requests to Kori API about modules and courses.
 * Sends the raw JSON data to caller as a String. 
 * 
 * @author Kilian Kugge
 */
public class KoriAPIRequester {
    
    // URL template for Kori API requests. The first %s is replaced with the 
    // request type (course-units/modules) and the second %s is replaced with
    // the group ID of the requested course/module.
    private static final String KORI_API_URL_TEMPLATE = 
        "https://sis-tuni.funidata.fi/kori/api/%s/by-group-id?groupId=%s&universityId=tuni-university-root-id";
    private static final String API_COURSE_STRING = "course-units";
    private static final String API_MODULE_STRING = "modules";
    private static final String SEARCH_REGEX = "[a-zA-Z0-9]{3,}";

    private KoriAPIRequester () {
        // Private constructor to avoid accidental instantiation of this class.
    }


    /**
     * Sends a request to the Kori API for course info with the given group ID.
     * @param groupId Kori API Group ID of the course requested as a string.
     * @return
     */
    public static final String requestCourseInfo (String groupId) {
        return makeAPIRequest(getCourseInfoURL(groupId));
    }


    /**
     * Formats the URL to the Kori API course info request with the given group ID.
     * Separated from the request method to allow for easier testing of url format.
     * 
     * @param groupId Kori API Group ID of the course requested as a string.
     * @return String containing the URL to the Kori API course info request.
     */
    public static final String getCourseInfoURL (String groupId) {
        return String.format(KORI_API_URL_TEMPLATE, API_COURSE_STRING, groupId);
    }


    /**
     * Searches for TUNI courses with the given keyword from Kori API. 
     * Keyword characters are restricted to letters and numbers, and the keyword
     * must be at least 3 characters long. These should probably also be checked with 
     * regex before calling this method.
     * @param keyword String keyword to search for.
     * @return String containing the full JSON response from the Kori API. 
     */
    public static final String requestCoursesWithKeyword (String keyword) {
        Pattern pattern = Pattern.compile(SEARCH_REGEX);
        if (!pattern.matcher(keyword).matches()) {
            System.err.println("Invalid keyword: " + keyword);
            return null;
        }

        return makeAPIRequest("https://sis-tuni.funidata.fi/kori/api/course-unit-search?fullTextQuery=" + keyword + "&limit=10000&orgRootId=tuni-university-root-id&start=0&uiLang=fi&universityOrgId=tuni-university-root-id&validity=ONGOING_AND_FUTURE");
    }


    /**
     * Sends a request to the Kori API for module info with the given group ID.
     * @param groupId Kori API Group ID of the module requested as a string.
     * @return
     */
    public static final String requestModuleInfo (String groupId) {
        return makeAPIRequest(getModuleInfoURL(groupId));
    }


    /**
     * Formats the URL to the Kori API module info request with the given group ID.
     * Separated from the request method to allow for easier testing of url format.
     * 
     * @param groupId Kori API Group ID of the module requested as a string.
     * @return String containing the URL to the Kori API module info request.
     */
    public static final String getModuleInfoURL (String groupId) {
        return String.format(KORI_API_URL_TEMPLATE, API_MODULE_STRING, groupId);
    }
    

    /**
     * Gets all Degree Programmes from TUNI Kori API.
     * @return String containing the full JSON response from the Kori API.
     */
    public static final String requestDegreeProgrammes () {
        return makeAPIRequest("https://sis-tuni.funidata.fi/kori/api/module-search?curriculumPeriodId=uta-lvv-2021&universityId=tuni-university-root-id&moduleType=DegreeProgramme&limit=1000");
    }


    /**
     * Makes a request to the Kori API with the given URL and returns the full
     * JSON response as a string.
     * 
     * EXAMPLE METHHOD PULLED FROM THE INTERNET, detects used encoding from the
     * Content-Type header and uses it to read the response. This avoids problems
     * with åäö and other special characters.
     * 
     * @param urlString URL for the request.
     * @return  String containing the full JSON response from the Kori API.
     */
    private static final String makeAPIRequest (String urlString)
    {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Get the Content-Type header value
                String contentType = connection.getHeaderField("Content-Type");

                // Extract the encoding from the Content-Type header, if available
                String encoding = "UTF-8"; // Default to UTF-8
                String[] contentTypeParts = contentType.split(";");

                for (String part : contentTypeParts) {
                    part = part.trim();
                    if (part.toLowerCase().startsWith("charset=")) {
                        encoding = part.substring("charset=".length());
                        break;
                    }
                }

                // Read the response using the detected encoding
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                return content.toString();
            }
            else {
                System.err.println("GET request failed. Response code: " + responseCode);
                return null;
            }
        } 
        catch (ProtocolException e) {
            System.err.println("ProtocolException at KoriAPIRequester");
            e.printStackTrace();
            return null;
        }   
        catch (SecurityException e) {
            System.err.println("SecurityException at KoriAPIRequester");
            e.printStackTrace();
            return null;
        }     
        catch (MalformedURLException e) {
            System.err.println("Malformed URL provided to makeAPIRequest: " + urlString);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            MyJavaFXUtils.displayAlert(
                AlertType.ERROR, 
                "Error",
                "There was a problem connecting to the server.",
                "There might be a problem with internet connection,\n" +
                "or the server might be down. Please try again later.");
            System.err.println("IOException at KoriAPIRequester");
            e.printStackTrace();
            return null;
        }
    }
}
