package com.example.junmung.hangangparksmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.gen.DaumMapLibraryAndroidMeta;

import java.util.List;

@SuppressWarnings("deprecation")
public class ARGuideActivity extends AppCompatActivity implements SensorEventListener, MapView.CurrentLocationEventListener, LocationListener{
    private static final String LOG_TAG = "ARGuideActivity";
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000;//1000 * 60 * 1; // 1 minute


    private SurfaceView surfaceView;
    private ARCamera arCamera;
    private Camera camera;
    private AROverlayView overlayView;
    private GLClearRenderer renderer;

    private SensorManager sensorManager;
    private LocationManager locationManager;
    public Location location;

    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;


    private ViewGroup cameraContainer;
    private ViewGroup mapViewContainer;

    private int _yDelta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_arguide);


        getID();

        GLSurfaceView glView = new GLSurfaceView(this);

        glView.setEGLConfigChooser( 8,8, 8, 8, 16, 0 );
        glView.getHolder().setFormat( PixelFormat.TRANSPARENT);
        glView.setZOrderOnTop(true);


        renderer = new GLClearRenderer();
        glView.setRenderer( renderer );
        cameraContainer.addView(glView);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        MapView mapView = new MapView(this);
        overlayView = new AROverlayView(this);

        mapView.setCurrentLocationEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
        mapViewContainer.addView(mapView);

        initARCameraView();


    }


    //
    public double bearingP1toP2(double P1_latitude, double P1_longitude, double P2_latitude, double P2_longitude) {
        // 현재 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Cur_Lat_radian = P1_latitude * (Math.PI / 180);
        double Cur_Lon_radian = P1_longitude * (Math.PI / 180);

        // 목표 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Dest_Lat_radian = P2_latitude * (Math.PI / 180);
        double Dest_Lon_radian = P2_longitude * (Math.PI / 180);

        // radian distance
        double radian_distance = (Math.acos(Math.sin(Cur_Lat_radian) * Math.sin(Dest_Lat_radian)
                + Math.cos(Cur_Lat_radian) * Math.cos(Dest_Lat_radian) * Math.cos(Cur_Lon_radian - Dest_Lon_radian)));


        // 목적지 이동 방향을 구한다.(현재 좌표에서 다음 좌표로 이동하기 위해서는 방향을 설정해야 한다. 라디안값이다.
        // acos의 인수로 주어지는 x는 360분법의 각도가 아닌 radian(호도)값이다.
        double radian_bearing =
                (Math.acos((Math.sin(Dest_Lat_radian) - Math.sin(Cur_Lat_radian) * Math.cos(radian_distance))
                / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance))));


        double true_bearing;

        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0) {
            true_bearing = radian_bearing * (180 / Math.PI);
            true_bearing = 360 - true_bearing;
        }
        else {
            true_bearing = radian_bearing * (180 / Math.PI);
        }

        return true_bearing;
    }


    @Override
    public void onResume() {
        super.onResume();
        requestLocationPermission();
        requestCameraPermission();
        registerSensors();
        initAROverlayView();


    }

    @Override
    public void onPause() {
        releaseCamera();
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }
    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }
    }
    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void initAROverlayView() {
        if (overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
        }
        cameraContainer.addView(overlayView);
    }


    private void releaseCamera() {
        if(camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void getID(){
        mapViewContainer = findViewById(R.id.map_view);
        surfaceView = findViewById(R.id.surfaceView);
        cameraContainer = findViewById(R.id.camera_view);
        findViewById(R.id.separator).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        _yDelta = Y - lParams.bottomMargin;
//                        dY = v.getY() - event.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        layoutParams.bottomMargin = (Y - _yDelta);
                        layoutParams.topMargin = -layoutParams.bottomMargin;
                        v.setLayoutParams(layoutParams);

                        v.animate().translationY(Y - _yDelta).setDuration(0);




                        break;
                }
                findViewById(R.id.rootView).invalidate();
                return true;
            }
        });
    }


    // 현재위치 업데이트 함수
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
//        overlayView.updateCurrentLocation(mapPointGeo.latitude, mapPointGeo.longitude);
        // 현재 위치가 바뀔때마다 이 함수를 들어오게 되는데
        // AROverlayView 에 들어오는 값들을 전달해주면 위치값을 바꿀수있다.


    }


    // AR 카메라 뷰 컨테이너 설정
    public void initARCameraView() {

        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        cameraContainer.addView(surfaceView);

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainer.addView(arCamera);
        arCamera.setKeepScreenOn(true);

        initCamera();
    }


    // 카메라 프리뷰 실행
    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initLocationService() {

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        try   {
            this.locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d("GPS boolean", "" + isGPSEnabled);
            Log.d("Network boolean", "" + isNetworkEnabled);

            if (!isNetworkEnabled && !isGPSEnabled)    {
                // cannot get location
                this.locationServiceAvailable = false;
            }

            this.locationServiceAvailable = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null)   {
//                    location = getLastKnownLocation();
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    updateLatestLocation();
                }
            }

            if (isGPSEnabled)  {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("locationManager ", "" + locationManager);

                if (locationManager != null)  {
//                    location = getLastKnownLocation();

                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLatestLocation();
                    Log.d("location ", "" + location);

                }
            }
        } catch (Exception ex)  {

        }
    }


    /*
    private Location getLastKnownLocation() {
        locationManager= (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }*/

    private void updateLatestLocation() {
        if (overlayView !=null && location != null) {
            overlayView.updateCurrentLocation(location);
        }
    }


    // 센서 변환시
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];
            float[] orientation = new float[3];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);
            SensorManager.getOrientation(rotationMatrixFromVector, orientation);

            float azimuth = (float)((Math.toDegrees(orientation[0])));
            azimuth = azimuth > 0 ? azimuth : azimuth + 360;
            Log.e("방위각", String.format("%f", azimuth));


            // 이 부분 부터 경유지들의 리스트들을 가지고 통과했는지 확인하면서 각도를 바꿔줘야함
            boolean isPassPoint = true;
            if(isPassPoint){
                
            }

            // 현재위치와 목표지점 북쪽기준의 방위각을 구한다.
            double bearing = bearingP1toP2(location.getLatitude(), location.getLongitude(), 37.580547,127.088488);

            float arArrowAngle;
            if(bearing > azimuth)
                arArrowAngle = -(Math.abs(Math.abs(azimuth) - Math.abs((float)bearing)));
            else
                arArrowAngle = (Math.abs(Math.abs(azimuth) - Math.abs((float)bearing)));
            renderer.setCurrentAngle(arArrowAngle);


            // 이 부분은 두점사이의 거리를 표현해주는데, AROverlayView 클래스에서 표현해주므로
            // 여기서 표현해줄 필요가 없을듯
            ARPoint arPoint = new ARPoint("사가정", 37.580547,127.088488,0);
            double distance = location.distanceTo(arPoint.getLocation());
            Log.e("Distance", ""+distance );


            if (arCamera != null)
                projectionMatrix = arCamera.getProjectionMatrix();

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            overlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        updateLatestLocation();

    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }





    /* 다음 맵뷰 오버라이드 함수들 */
    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
        Log.e("DaumMap Y Angle", ""+v);
//        renderer.setCurrentAngle(v);

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

}
