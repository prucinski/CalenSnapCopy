package com.example.ocrhotel;

import androidx.annotation.RequiresPermission;
import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;
import java.io.File;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.UUID;


public class OCR_Azure_REST {

    // Add your Computer Vision subscription key and endpoint to your environment variables.
    // After setting, close and then re-open your command shell or project for the changes to take effect.

    // TODO: Encapsulate those somehow.
    private static final String subscriptionKey = "db0abec60f8c4ea4a4d69cde1102939e";
    private static final String endpoint = "https://ocr-app.cognitiveservices.azure.com";

    private static final String POST_URLBase = endpoint + "/vision/v3.2/read/analyze";
    // private static final String GET_URLBase = endpoint + "/vision/v3.2/read/analyzeResults/";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType MEDIA_TYPE_FILE = MediaType.parse("application/octet-stream");
    private static final MediaType MEDIA_TYPE_URL = MediaType.parse("application/json");
    private static final ComputerVisionClient compVisClient = ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);

    /**
     * Processes raw file data.
     * @param file File to be received
     * @return results as a ReadOperationResult to format later
     * */
    public ReadOperationResult GetImageTextData(File file) throws Exception{

        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .url(POST_URLBase)
                .post(RequestBody.create(file,MEDIA_TYPE_FILE))
                .build();

        Response response = client.newCall(request).execute();
        try {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            System.out.println(response.headers().toString());
            System.out.println(response.body().string());
        }
        catch(IOException e){}

        String operationId = extractOperationIdFromOpLocation(response.headers().get("operation-location"));
        ComputerVisionImpl vision = (ComputerVisionImpl) compVisClient.computerVision();

        return GetReadResult(vision, operationId);
    }

    /**
     * Processes raw file data from URL.
     * @param url URL of the file to be received
     * @return results as a ReadOperationResult to format later
     * Note: It is only here for testing purposes, don't mind it.
     * */
    public ReadOperationResult GetImageTextDataFromURL(String url) throws Exception{

        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .url(POST_URLBase)
                .post(RequestBody.create(String.join("{\"url\":\"" + url + "\"}"),MEDIA_TYPE_URL))
                .build();

        Response response = client.newCall(request).execute();
        try {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            System.out.println(response.headers().toString());
            System.out.println(response.body().string());
        }
        catch(IOException e){}

        String operationId = extractOperationIdFromOpLocation(response.headers().get("operation-location"));
        ComputerVisionImpl vision = (ComputerVisionImpl) compVisClient.computerVision();

        return GetReadResult(vision, operationId);
    }


    private static String extractOperationIdFromOpLocation(String operationLocation) {
        if (operationLocation != null && !operationLocation.isEmpty()) {
            String[] splits = operationLocation.split("/");

            if (splits != null && splits.length > 0) {
                return splits[splits.length - 1];
            }
        }
        throw new IllegalStateException("Something went wrong: Couldn't extract the operation id from the operation location");
    }

    private ReadOperationResult GetReadResult(ComputerVisionImpl vision, String operationId) throws Exception {

        boolean pollForResult = true;
        ReadOperationResult readResults = null;

        while (pollForResult) {
            // Poll for result every second
            Thread.sleep(1000);
            readResults = vision.getReadResult(UUID.fromString(operationId));

            // The results will no longer be null when the service has finished processing the request.
            if (readResults != null) {
                // Get request status
                OperationStatusCodes status = readResults.status();

                if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                    pollForResult = false;
                }
            }
        }

        return readResults;
    }

    /**
     * Reads text from raw file data using the Azure API.
     * @param file File to be received.
     *             Supported image formats: JPEG, PNG, BMP, PDF and TIFF.
     *             For the free tier, only the first 2 pages are processed. File size less than 50MB (4MB for the free tier).
     * @return String object containing the text inside the image separated by lines and pages.
     * */
    public String GetOnlyText(File file) throws Exception{
        ReadOperationResult readResults = GetImageTextData(file);

        // Print read results, page per page
        StringBuilder builder = new StringBuilder();
        for (ReadResult pageResult : readResults.analyzeResult().readResults()) {
            System.out.println();
            System.out.println("Printing Read results for page " + pageResult.page());

            for (Line line : pageResult.lines()) {
                builder.append(line.text());
                builder.append("\n");
            }

            System.out.println(builder.toString());
        }
        return builder.toString();
    }


    public static void main(String[] args) throws Exception{
        File file = new File("C:\\Users\\matey\\Downloads\\image.jpg");
        System.out.println(new OCR_Azure_REST().GetOnlyText(file));
    }
}
