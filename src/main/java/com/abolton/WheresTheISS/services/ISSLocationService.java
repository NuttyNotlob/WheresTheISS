package com.abolton.WheresTheISS.services;

import com.abolton.WheresTheISS.APIStore;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ISSLocationService {
    private static String latitude;
    private static String longitude;

    @Autowired
    APIStore apiStore;

    @PostConstruct
    public void fetchAPIData() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiStore.getIssApiUrl())).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(ISSLocationService::parseData).join();
    }

    private static String parseData(String dataBody) {
        // This method simply extracts the latitude and longitude from the JSON, and brings it into the object's
        // latitude & longitude fields
        JSONObject overallJSON = new JSONObject(dataBody);
        JSONObject positionDetails = overallJSON.getJSONObject("iss_position");

        latitude = positionDetails.getString("latitude");
        longitude = positionDetails.getString("longitude");

        System.out.println(latitude);
        System.out.println(longitude);

        return null;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
