package io.github.jlameira;


import io.github.jlameira.amazonconfig.AmazonService;
import io.github.jlameira.amazonconfig.IAmazonService;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class ConversorController {

    private final Logger logger = LoggerFactory.getLogger(ConversorController.class);
    private final IAmazonService amazonService;

    private static String UPLOADED_FOLDER = "F://temp//";
    public static final String TEST_API_KEY = "4a08ca50cf2218b50c6a51e6e0b85ab1";



    @Autowired
    public ConversorController( IAmazonService amService) {
        this.amazonService = amService;
    }

    @RequestMapping(value = "/api/hello")
    public String hello() {
        return "hello";
    }

    @CrossOrigin(origins = "http://localhost:8081")
    @PostMapping("/api/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile uploadfile) {

        logger.debug("Single file upload!");

        if (uploadfile.isEmpty()) {
            return new ResponseEntity("please select a file!", HttpStatus.OK);
        }

        try {

        this.amazonService.store(uploadfile, "input");

            String input_filename = uploadfile.getOriginalFilename();
            String output_filename =
                    FilenameUtils.removeExtension(input_filename) + ".mp4";

//            saveUploadedFiles(Arrays.asList(uploadfile));
            // Create json request body.
            String request_body = createJSONRequestBody(
                    "/" +  "input" + "/" + input_filename,
                    "/" + "output" + "/" + output_filename
            );
            // Create and send encoding request to Zencoder.
            String response = sendEncodeRequest(request_body);
            // Prepare return string.
            JSONObject response_JSON = new JSONObject(response);
            Object input_id = response_JSON.get("id");
            Object output_id = ((JSONObject) ((JSONArray) response_JSON.get("outputs")).get(0)).get("id");
            Object output_url = ((JSONObject) ((JSONArray) response_JSON.get("outputs")).get(0)).get("url");

            JSONObject return_JSON = new JSONObject();
            return_JSON.put("input_id", input_id);
            return_JSON.put("output_id", output_id);
            return_JSON.put("output_url", output_url);
            return_JSON.put("zencoder_key", TEST_API_KEY);
            // Return string



            return new ResponseEntity( return_JSON.toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    //save file
    private void saveUploadedFiles(List<MultipartFile> files) throws IOException {

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue; //next pls
            }

            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);

        }

    }

    private String createJSONRequestBody(
            String input,
            String output
    ) {
        try {
            JSONObject request_body = new JSONObject();
            // Set API key with full access to Zencoder service.
            request_body.put("api_key",TEST_API_KEY);
            // Set input url from Amazon AWS.
            request_body.put(
                    "input",
                    "s3://" +
                            "conversor-jonathan" + input
            );
            request_body.put("region","us-virginia" );
            // Set output configuration data
            JSONObject json_output = new JSONObject();
            json_output.put(
                    "url",
                    "s3://"
                            +"conversor-jonathan" + output
            );
            json_output.put("region","us-virginia" );
            json_output.put("public", "true");
            request_body.put("output", json_output);
            // return request JSON
            return request_body.toString();
        } catch (Exception e) {
            return String.valueOf(e.getStackTrace());
//            throw new EncodingException("Error creating request body for encoding job.");
        }
    }

    /*
    Send POST request to run encoding job.
    params: Request body in JSON format.
    return: Request's response.
    */
    private String sendEncodeRequest(String request_body) {
        try {
            // Create connection.
            URL url = new URL("https://app.zencoder.com/api/v2/jobs");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // Setting Header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            // Setting output and Sending
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(request_body.toString());
            wr.flush();
            wr.close();
            // Get response code.
            int response_code = con.getResponseCode();
            //Log.
//            Application.logger.info("\nSending 'POST' request to URL : " + url);
//            Application.logger.info("Response Code : " + response_code);
            // Read response
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            // Return response in string format.
//            Application.logger.info("RESPONSE:");
//            Application.logger.info(response.toString());
            return response.toString();
        } catch (Exception e) {
//            throw new EncodingException("Error when sending Zencoder job.");
            return String.valueOf(e.getStackTrace());
        }
    }

}

