package com.goeuroapitest;

/**
 * Created by Lucas on 26/08/2015.
 */
public class Destiny {
    private int _id;
    private String name;
    private String fullName;
    private String iata_airport_code;
    private String type;
    private String country;
    private GeoLocation geo_position;
    private long locationId;
    private boolean inEurope;
    private String countryCode;
    private boolean coreCountry;
    private Object distance;
    private Object key;

    public int getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getIata_airport_code() {
        return iata_airport_code;
    }

    public String getType() {
        return type;
    }

    public String getCountry() {
        return country;
    }

    public GeoLocation getGeo_position() {
        return geo_position;
    }

    public long getLocationId() {
        return locationId;
    }

    public boolean isInEurope() {
        return inEurope;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public boolean isCoreCountry() {
        return coreCountry;
    }

    public Object getDistance() {
        return distance;
    }

    public static class GeoLocation {
        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
