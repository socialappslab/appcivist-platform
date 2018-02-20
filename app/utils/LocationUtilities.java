package utils;

import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import play.Logger;

public class LocationUtilities {

    public static Point polygonCenter(FeatureCollection featureCollection) {
        //if geojson doesnt has a center, calculate it.
        Point centerPoint = new Point();
        if (!(featureCollection.getFeatures().get(0).getGeometry() instanceof Point)) {
            Logger.debug(" geoJson has not center");
            Polygon polygon = (Polygon) featureCollection.getFeatures().get(0).getGeometry();
            double[][] points = new double[polygon.getCoordinates().get(0).size()][2];
            int i = 0;
            for (LngLatAlt lat : polygon.getCoordinates().get(0)) {
                double[] point = {0, 0};
                point[0] = lat.getLatitude();
                point[1] = lat.getLongitude();
                points[i] = point;
                i++;
            }
            double[] center = calculateCenter(points);
            LngLatAlt lon = new LngLatAlt();
            lon.setLongitude(center[0]);
            lon.setLatitude(center[1]);
            centerPoint.setCoordinates(lon);

        }
        return centerPoint;
    }

        private static double[] calculateCenter ( double[]...polygonPoints){
            double cumLon = 0;
            double cumLat = 0;
            for (double[] coordinate : polygonPoints) {
                cumLon += coordinate[0];
                cumLat += coordinate[1];
            }
            return new double[]{cumLon / polygonPoints.length, cumLat / polygonPoints.length};
        }
    }