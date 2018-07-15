package com.example.junmung.hangangparksmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.junmung.hangangparksmap.RetrofitUtil.RetrofitClient;
import com.example.junmung.hangangparksmap.RetrofitUtil.TMapApiService;
import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.github.filosganga.geogson.model.Feature;
import com.github.filosganga.geogson.model.FeatureCollection;
import com.github.filosganga.geogson.model.Geometry;
import com.github.filosganga.geogson.model.positions.LinearPositions;
import com.github.filosganga.geogson.model.positions.SinglePosition;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skt.Tmap.TMapTapi;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


@SuppressWarnings("deprecation")
public class ARGuideActivity extends AppCompatActivity  {
    private static final String LOG_TAG = "ARGuideActivity";
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 3; //  meters
    private static final long MIN_TIME_BW_UPDATES = 1000;   // 1000 == 1 sec

    private SensorManager sensorManager;
    private LocationManager locationManager;

    private SurfaceView surfaceView;
    private ARCamera arCamera;
    private Camera camera;
    private AROverlayView overlayView;
    private GLClearRenderer renderer;

    private ViewGroup cameraContainer;
    private ViewGroup mapViewContainer;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap googleMap;
    private TMapTapi tMapApi;

    private Point point, currentPoint, endPoint;
    private List<Point> wayPoints;



    int _yDelta;
    boolean isPointsSetting = false;
    int pointIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_arguide);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        requestLocationPermission();
        registerSensors();

        endPoint = getDestination();


        getID();


        new Thread(new Runnable() {
            @Override
            public void run() {
                initOpenGLView();
                overlayView = new AROverlayView(getApplicationContext(), currentPoint);
                initAROverlayView(overlayView);
            }
        }).start();


        requestCameraPermission();

        tMapApi = new TMapTapi(this);
        tMapApi.setSKTMapAuthentication(TMapApiService.TMAP_APPKEY);
        tMapApi.setOnAuthenticationListener(new TMapTapi.OnAuthenticationListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                Log.d("Tmap Success", "인증완료");
            }

            @Override
            public void SKTMapApikeyFailed(String errorMsg) {
                Log.d("Tmap Error", errorMsg);
            }
        });
    }


    private void getWayPoints(String startName, String endName, double startX, double startY, double endX, double endY){
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new GeometryAdapterFactory())
                .create();

        Retrofit retrofit = RetrofitClient.getClient(gson);
        TMapApiService apiService = retrofit.create(TMapApiService.class);

        Call<FeatureCollection> call = apiService.getMapPointInfos(TMapApiService.TMAP_APPKEY,
                startName, endName, startX, startY, endX, endY);

        call.enqueue(new Callback<FeatureCollection>() {
            @Override
            public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
                if(response.isSuccessful()){
                    List<Feature> features = response.body().features();
                    wayPoints = new ArrayList<>();
                    int j, k = 1;

                    for(int i = 0; i < features.size(); i++){
                        Feature feature = features.get(i);
                        Geometry geometry = feature.geometry();
                        String type = geometry.type().getValue();

                        if(type.equals("LineString")){
                            Geometry<LinearPositions> linearGeometry = geometry;
                            LinearPositions linearPositions = linearGeometry.positions();
                            Iterator<SinglePosition> iterator = linearPositions.children().iterator();
                            List<SinglePosition> positions = Lists.newArrayList(iterator);

                            if(i != 0){
                                for(j = 1; j < positions.size(); j++){
                                    wayPoints.add(new Point("이동경로_"+k,
                                            positions.get(j).coordinates().getLat(),
                                            positions.get(j).coordinates().getLon(),0));
                                    k++;
                                }
                            }
                            else{
                                for(j = 0; j < positions.size(); j++){
                                    wayPoints.add(new Point("이동경로_"+k,
                                            positions.get(j).coordinates().getLat(),
                                            positions.get(j).coordinates().getLon(),0));
                                    k++;
                                }
                            }
                        }
                    }
                    isPointsSetting = true;
                    overlayView.updateDestPoint(wayPoints.get(pointIndex));

                    // GoogleMap 설정
//                    setGoogleMap();

                    setDaumMap();

                    Log.d("Retrofit Success", "성공");
//
//                    for(j = 0; j < wayPoints.size(); j++){
//                        double lat = wayPoints.get(j).getLocation().getLatitude();
//                        double lon = wayPoints.get(j).getLocation().getLongitude();
//                        Log.d("WayPoints_"+j, "Lat : " + lat +", Lon : " + lon);
//                    }
//                    Log.d("WayPoints Size", ""+wayPoints.size());
                }
            }

            @Override
            public void onFailure(Call<FeatureCollection> call, Throwable t) {
                Log.d("Retrofit Fail", "실패");
                t.printStackTrace();
            }
        });
    }

    private void setDaumMap(){
        final MapView mapView = new MapView(this);
        mapView.setCurrentLocationEventListener(daumLocationListener);
        mapView.setPOIItemEventListener(daumPOIListener);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);

        mapViewContainer.addView(mapView);

        final MapPolyline polyline = new MapPolyline();
        polyline.setLineColor(Color.argb(128, 255, 51, 0));


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(currentPoint.latitude, currentPoint.longitude));
                for(Point point : wayPoints){
                    polyline.addPoint(MapPoint.mapPointWithGeoCoord(point.latitude, point.longitude));
                }
                mapView.addPolyline(polyline);
                Log.d("CurrentPoint", "lat : " + currentPoint.latitude +", lon:"+ currentPoint.longitude);
            }
        });


        MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, 100));

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("목적지");
        marker.setTag(0);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(endPoint.latitude, endPoint.longitude));
        marker.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);
    }

    private void setGoogleMap(){
        // GoogleMap 설정
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
//        mapFragment.getMapAsync(this);
    }

    private Point getDestination(){
        Intent intent = getIntent();
        String destination = intent.getStringExtra("Destination");
        double latitude = intent.getDoubleExtra("Latitude", 0d);
        double longitude = intent.getDoubleExtra("Longitude", 0d);

        return new Point(destination, latitude, longitude, 0);
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        // 구글 맵 객체를 불러온다.
//        this.googleMap = googleMap;
//
//        // 사가정에 대한 위치 설정
//        LatLng myLocation = new LatLng(currentPoint.latitude, currentPoint.longitude);
//        LatLng destination = new LatLng(
//                wayPoints.get(wayPoints.size() - 1).latitude,
//                wayPoints.get(wayPoints.size() - 1).longitude
//        );
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            return;
//
//        googleMap.setMyLocationEnabled(true);
//        UiSettings uiSettings = this.googleMap.getUiSettings();
//        uiSettings.setCompassEnabled(true);
//        uiSettings.setMyLocationButtonEnabled(true);
//
//
//        // 구글 맵에 표시할 마커에 대한 옵션 설정
//        MarkerOptions makerOptions = new MarkerOptions();
//        makerOptions.position(destination).title("목적지");
//
//        // 마커를 생성한다.
//        this.googleMap.addMarker(makerOptions);
//
//        //카메라를 여의도 위치로 옮긴다.
////        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
//
//
//
//
//        ArrayList<LatLng> latLngs = new ArrayList<>();
//        latLngs.add(myLocation);
//        for(Point point : wayPoints){
//            latLngs.add(new LatLng(point.latitude, point.longitude));
//        }
//
//        PolylineOptions polyOptions = new PolylineOptions();
//        polyOptions
//                .color(Color.RED)
//                .width(35);
//        polyOptions.addAll(latLngs);
//
//        this.googleMap.addPolyline(polyOptions);
//    }


    @Override
    public void onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }

    // 리줌에서의 부분은 나중에 추가해주자
    @Override
    public void onResume() {
        super.onResume();

//        requestCameraPermission();
//        registerSensors();

    }

    @Override
    public void onPause() {
        releaseCamera();
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }
    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }
    }
    private void registerSensors() {
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
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


    private void initOpenGLView(){
        GLSurfaceView glView = new GLSurfaceView(this);
        glView.setEGLConfigChooser( 8,8, 8, 8, 16, 0 );
        glView.getHolder().setFormat( PixelFormat.TRANSPARENT);
        glView.setZOrderOnTop(true);

        renderer = new GLClearRenderer();
        glView.setRenderer( renderer );
        cameraContainer.addView(glView);
    }

    // AR 카메라 뷰 컨테이너 설정
    private void initARCameraView() {
        if (surfaceView.getParent() != null)
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);

        cameraContainer.addView(surfaceView);

        if (arCamera == null)
            arCamera = new ARCamera(this, surfaceView);

        if (arCamera.getParent() != null)
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);

        cameraContainer.addView(arCamera);
        arCamera.setKeepScreenOn(true);

        initCamera();
    }

    private void initAROverlayView(AROverlayView overlayView) {
        if (overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
        }
        cameraContainer.addView(overlayView);
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
                Toast.makeText(this, "AR 화면 구성중입니다", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 위치 서비스 시작
    private void initLocationService() {



        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(googleConnectionCallbacksListener)
                .addOnConnectionFailedListener(googleConnectionFailListener)
                .addApi(LocationServices.API)
                .build();






//        try   {
//            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
//
//            // Get GPS and network status
//            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//            if (isNetworkEnabled) {
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                        MIN_TIME_BW_UPDATES,
//                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                if (locationManager != null)   {
////                    location = getLastKnownLocation();
//                    currentPoint = new Point(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
//
//                    updateLatestLocation();
//                }
//            }
//
//
//            if (isGPSEnabled)  {
//
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                        MIN_TIME_BW_UPDATES,
//                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//
//                if (locationManager != null)  {
////                    location = getLastKnownLocation();
//
//                    currentPoint = new Point(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
//                    updateLatestLocation();
//                }
//            }
//        } catch (Exception ex)  {
//
//        }
    }


    // 마지막위치 업데이트
    private void updateLatestLocation() {
        if (overlayView !=null && currentPoint != null) {
            overlayView.updateCurrentPoint(currentPoint);
        }
    }


    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && currentPoint != null) {
                float[] rotationMatrixFromVector = new float[16];
                float[] projectionMatrix = new float[16];
                float[] rotatedProjectionMatrix = new float[16];
                float[] orientation = new float[3];


                SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);
                SensorManager.getOrientation(rotationMatrixFromVector, orientation);

                float azimuth = (float)((Math.toDegrees(orientation[0])));
                azimuth = azimuth > 0 ? azimuth : azimuth + 360;


                // 이 부분 부터 경유지들의 리스트들을 가지고 통과했는지 확인하면서 각도를 바꿔줘야함
                if(!isPointsSetting){
                    point = currentPoint;

                }
                else{
                    int distance = currentPoint.distanceTo(wayPoints.get(pointIndex));
                    if(distance < 2){
                        pointIndex++;
                    }
                    point = wayPoints.get(pointIndex);
                    overlayView.updateDestPoint(point);
                }


                // 현재위치와 목표지점 북쪽기준의 방위각을 구한다.
                double bearing = currentPoint.bearingTo(point);


                // AR 화살표 방향구해서 렌더러에 전달해주기 ( 나중에 빼야할듯 )
                float arArrowAngle;
                if(bearing > azimuth)
                    arArrowAngle = -(Math.abs(Math.abs(azimuth) - Math.abs((float)bearing)));
                else
                    arArrowAngle = (Math.abs(Math.abs(azimuth) - Math.abs((float)bearing)));
                renderer.setCurrentAngle(arArrowAngle);


                if (arCamera != null)
                    projectionMatrix = arCamera.getProjectionMatrix();

                Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
                overlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private MapView.POIItemEventListener daumPOIListener = new MapView.POIItemEventListener() {
        @Override
        public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

        }

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

        }

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

        }

        @Override
        public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

        }
    };

    private MapView.CurrentLocationEventListener daumLocationListener = new MapView.CurrentLocationEventListener() {
        @Override
        public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

        }

        @Override
        public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

        }

        @Override
        public void onCurrentLocationUpdateFailed(MapView mapView) {

        }

        @Override
        public void onCurrentLocationUpdateCancelled(MapView mapView) {

        }
    };

    private GoogleApiClient.ConnectionCallbacks googleConnectionCallbacksListener = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            LocationRequest locationRequest = new LocationRequest()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setFastestInterval(500);

            if ( Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                return  ;
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, googleLocationListener);
            currentPoint = new Point(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
            currentPoint.setName("현위치");
            getWayPoints(
                    currentPoint.getName(), endPoint.getName(),
                    currentPoint.longitude, currentPoint.latitude,
                    endPoint.longitude, endPoint.latitude
            );

            Log.d("GoogleApiClient", "onConnected");
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d("GoogleApiClient", "Fail");
        }
    };

    private GoogleApiClient.OnConnectionFailedListener googleConnectionFailListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        }
    };


    private LocationListener googleLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentPoint.updateLocation(location);
            updateLatestLocation();
        }
    };
}
