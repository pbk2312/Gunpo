package com.example.gunpo.service.functions;


import com.example.gunpo.constants.errorMessage.LocationErrorMessage;
import com.example.gunpo.exception.location.DistanceCalculationException;
import com.example.gunpo.exception.location.InvalidCoordinateException;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private static final double GUNPO_LATITUDE = 37.2981;
    private static final double GUNPO_LONGITUDE = 126.936;
    private static final double GUNPO_RADIUS_KM = 10.0;
    private static final double EARTH_RADIUS_KM = 6371; // 지구 반경 (단위: km)

    public boolean isWithinGunpoRadius(double userLatitude, double userLongitude) {
        validateCoordinates(userLatitude, userLongitude);
        return verifyWithinRadius(userLatitude, userLongitude);
    }

    private boolean verifyWithinRadius(double lat1, double lon1) {
        double dLat = toRadians(GUNPO_LATITUDE - lat1);
        double dLon = toRadians(GUNPO_LONGITUDE - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(toRadians(lat1)) * Math.cos(toRadians(GUNPO_LATITUDE))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        if (Double.isNaN(distance)) {
            throw new DistanceCalculationException(LocationErrorMessage.DISTANCE_CALCULATION_ERROR.getMessage());
        }

        return distance <= GUNPO_RADIUS_KM;
    }

    private void validateCoordinates(double latitude, double longitude) {
        validateLatitude(latitude);
        validateLongitude(longitude);
    }

    private static void validateLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new InvalidCoordinateException(LocationErrorMessage.INVALID_LONGITUDE.getMessage());
        }
    }

    private static void validateLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new InvalidCoordinateException(LocationErrorMessage.INVALID_LATITUDE.getMessage());
        }
    }

    private double toRadians(double angle) {
        return Math.toRadians(angle);
    }

}
