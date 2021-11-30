package com.example.ocrhotel;


import androidx.annotation.NonNull;

import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;

import java.io.File;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.UUID;
import java.util.function.Consumer;


public class OCR_Azure_REST {

    // TODO: Encapsulate those somehow.
    private static final String subscriptionKey = "db0abec60f8c4ea4a4d69cde1102939e";
    private static final String endpoint = "https://ocr-app.cognitiveservices.azure.com";

    private static final String POST_URLBase = endpoint + "/vision/v3.2/read/analyze";
    private static final String GET_URLBase = endpoint + "/vision/v3.2/read/analyzeResults/";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType MEDIA_TYPE_FILE = MediaType.parse("application/octet-stream");
    private static final MediaType MEDIA_TYPE_URL = MediaType.parse("application/json");
    private static final ComputerVisionClient compVisClient = ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);

    private ReadOperationResult results = null;
    private String resultsText;


    private Call postFile(File file, Callback callback){
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .url(POST_URLBase)
                .post(RequestBody.create(file, MEDIA_TYPE_FILE))
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Reads text from raw file data using the Azure API.
     * @param file File to be received.
     *             Supported image formats: JPEG, PNG, BMP, PDF and TIFF.
     *             For the free tier, only the first 2 pages are processed. File size less than 50MB (4MB for the free tier).
     * After that, get the ReadOperationResults results variable in order to process it.
     * */
    public void GetImageTextData(File file, Consumer<String> callback) {
        postFile(file, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                String operationId = extractOperationIdFromOpLocation(response.headers().get("operation-location"));
                ComputerVisionImpl vision = (ComputerVisionImpl) compVisClient.computerVision();

                boolean pollForResult = true;
                while (pollForResult) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    results = vision.getReadResult(UUID.fromString(operationId));

                    OperationStatusCodes status = results.status();

                    if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                        pollForResult = false;
                    }
                }
                ExtractText();

                callback.accept(resultsText);

            }

        });
    }


    private Call post(String json, Callback callback) {
        RequestBody body = RequestBody.create(json, MEDIA_TYPE_URL);
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .url(POST_URLBase)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Processes raw file data from URL.
     *
     * @param url URL of the file to be received
     * After that, get the ReadOperationResults results variable in order to process it.
     * Note: It is only here for testing purposes, don't mind it.
     */
    public void GetImageTextDataFromURL(String url, Consumer<String> callback) {
        post(String.join("", "{\"url\":\"", url, "\"}"), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                String operationId = extractOperationIdFromOpLocation(response.headers().get("operation-location"));
                ComputerVisionImpl vision = (ComputerVisionImpl) compVisClient.computerVision();

                boolean pollForResult = true;
                while (pollForResult) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    results = vision.getReadResult(UUID.fromString(operationId));

                    OperationStatusCodes status = results.status();

                    if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                        pollForResult = false;
                    }
                }
                ExtractText();

                callback.accept(resultsText);

            }
        });
    }

    private static String extractOperationIdFromOpLocation(String operationLocation) {
        if (operationLocation != null && !operationLocation.isEmpty()) {
            String[] splits = operationLocation.split("/");

            if (splits.length > 0) {
                return splits[splits.length - 1];
            }
        }
        throw new IllegalStateException("Something went wrong: Couldn't extract the operation id from the operation location");
    }

    private void ExtractText() {
        // Print read results, page per page
        StringBuilder builder = new StringBuilder();

        for (ReadResult pageResult : results.analyzeResult().readResults()) {

            for (Line line : pageResult.lines()) {
                builder.append(line.text());
                builder.append("\n");
            }
        }
        resultsText = builder.toString();
    }

    public String getResultsText() {
        return resultsText;
    }


    public static void main(String[] args) {
        File file = new File("C:\\Users\\matey\\Downloads\\image.jpg");

        String url = "https://s3.amazonaws.com/thumbnails.venngage.com/template/112a39f4-2d97-44aa-ae3a-0e95a60abbce.png";
        OCR_Azure_REST ocrClient = new OCR_Azure_REST();

        ocrClient.GetImageTextData(file,System.out::println);

//        ocrClient.GetImageTextDataFromURL(url, System.out::println);

//        System.out.println(ocrClient.getResultsText());
//        System.out.println(res);
    }
}
