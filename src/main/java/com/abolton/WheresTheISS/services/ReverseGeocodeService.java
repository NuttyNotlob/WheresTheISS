package com.abolton.WheresTheISS.services;

import com.abolton.WheresTheISS.APIStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

@Service
public class ReverseGeocodeService {

    private static String locationLongName;
    private static String locationShortName;

    private static boolean IssFound;
    private static boolean aboveOcean;

    @Autowired
    APIStore apiStore;
    @Autowired
    ISSLocationService issLocationService;

    @PostConstruct
    public void fetchAPIData() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiStore.getReverseGeocodeURL() + issLocationService.getLatitude() + ","
                        + issLocationService.getLongitude() + "&key=" + apiStore.getGeocodingKey()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(ReverseGeocodeService::parseData).join();

    }

    private static String parseData(String dataBody) {
        JSONObject overallJSON = new JSONObject(dataBody);

        // The reverse geocoding API will not return results for all locations, notably those in the Pacific Ocean for
        // example. As such, we must check the status of the request - if we do not get a status of OK, we can't
        // continue to use the data
        if (!overallJSON.getString("status").equals("OK")) {
            System.out.println("ISS is above a location where it can't be found");
            IssFound = false;
            return null;
        } else {
            IssFound = true;
        }

        // First we get down to the address details from the reverse geo-code

        JSONArray results = overallJSON.getJSONArray("results");
        JSONArray addressDetails = results.getJSONObject(0).getJSONArray("address_components");

        System.out.println(addressDetails);

        // Now we scan through each of the JSONs in the addressDetails array, to find the one where it contains the type
        // of 'country'. We do this rather than set to a particular index as we get additional address details such as
        // Postal Code, depending on the country the ISS is over. Once we find this, we set this object's details. We
        // start from the end as it's typically towards the end
        for (int i = addressDetails.length()-1; i >= 0; i--) {
            JSONObject addressComponent = addressDetails.getJSONObject(i);
            if (addressComponent.has("types")) {
                String types = addressComponent.get("types").toString();
                if (types.contains("country")) {
                    aboveOcean = false;
                    locationLongName = addressComponent.getString("long_name");
                    locationShortName = addressComponent.getString("short_name");
                }
                // We also scan to see if we're above an ocean - if the geocode does detect this, it will put
                // it under the type 'natural_feature'
                else if (types.contains("natural_feature")) {
                    aboveOcean = true;
                    locationLongName = addressComponent.getString("long_name");
                    locationShortName = addressComponent.getString("short_name");
                }
            }
        }

        System.out.println("Ocean: " + aboveOcean);
        System.out.println(locationLongName);
        System.out.println(locationShortName);

        return null;
    }

    public static String getLocationLongName() { return locationLongName; }

    public static String getLocationShortName() { return locationShortName; }
}
