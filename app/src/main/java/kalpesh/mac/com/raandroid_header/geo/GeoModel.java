package kalpesh.mac.com.raandroid_header.geo;

import java.io.Serializable;

/**
 * Created by Windows on 16/04/2016.
 */
@SuppressWarnings("serial")
public class GeoModel implements Serializable{
    int id;
    String postCode, name;
    double latitude, longitude;


        public GeoModel(int id, String postCode, String name) {
            this.id = id;
            this.postCode = postCode;
            this.name = name;
        }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
