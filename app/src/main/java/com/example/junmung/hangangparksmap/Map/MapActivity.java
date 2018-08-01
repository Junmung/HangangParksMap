package com.example.junmung.hangangparksmap.Map;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.junmung.hangangparksmap.ARGuide.ARGuideActivity;
import com.example.junmung.hangangparksmap.ARGuide.Point;
import com.example.junmung.hangangparksmap.CommonPoint;
import com.example.junmung.hangangparksmap.CulturePoint;
import com.example.junmung.hangangparksmap.CultureWebViewClient;
import com.example.junmung.hangangparksmap.DataBase.DBHelper;
import com.example.junmung.hangangparksmap.FavoritePoint;
import com.example.junmung.hangangparksmap.Map.Dialog.FilterDialogFragment;
import com.example.junmung.hangangparksmap.R;
import com.example.junmung.hangangparksmap.RetrofitUtil.ApiService;
import com.example.junmung.hangangparksmap.RetrofitUtil.RetrofitClient;
import com.example.junmung.hangangparksmap.SearchPointPOJO.Document;
import com.example.junmung.hangangparksmap.SearchPointPOJO.SearchPoint;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class MapActivity extends AppCompatActivity implements FilterDialogFragment.DialogDismissListener{
    private int SEARCH_RADIUS = 500;

    private AnimatingLayout fabContainer;
    private FloatingActionButton fab_currentLocation, fab_filter, fab_ARGuide;

    private MapView mapView;
    private BottomSheetBehaviorGoogleMapsLike bottomSheetBehavior;
    private MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior;

    // Bottom Sheet Header
    private RelativeLayout layout_bottomHeader;
    private TextView text_pointName, text_pointAddress, text_pointDistance;

    private NestedWebView webView;
    private View bottomScrollView;

    private Point currentPoint;
    private MapPOIItem selectedPOIItem;

    private Toolbar toolbar;

    private GoogleApiClient mGoogleApiClient;


    public interface POICallback<T> {
        void onNotFound();
        void onError(Error error);
        void onSuccess(T receivedData);
        void onFailure(Error error);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initLocationService();
        getID_SetListener();

        // MapActivity 는 추후에 카카오톡 공유기능을 써야하기 때문에
        // Intent 로 좌표값을 받아온후 표시해줘야하는 것을 인지해야함

    }

    private void getID_SetListener(){
        ViewGroup mapViewContainer = findViewById(R.id.activity_Map_mapView);
        mapView = new MapView(MapActivity.this);
        mapView.setMapViewEventListener(mapViewEventListener);
        mapView.setCurrentLocationEventListener(mapLocationListener);
        mapView.setPOIItemEventListener(mapPOIEventListener);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
        mapViewContainer.addView(mapView);


        // Floating Button
        fabContainer = findViewById(R.id.activity_Map_fabContainer);
        fab_currentLocation = findViewById(R.id.activity_Map_fab_currentLocation);
        fab_currentLocation.setOnClickListener(fabClickListener);
        fab_filter = findViewById(R.id.activity_Map_fab_filter);
        fab_filter.setOnClickListener(fabClickListener);
        fab_ARGuide = findViewById(R.id.activity_Map_fab_ARGuide);
        fab_ARGuide.setOnClickListener(fabClickListener);

        // 최상위 뷰에서 스크롤뷰 가져오기
        CoordinatorLayout rootView = findViewById(R.id.activity_Map_rootView);
        bottomScrollView = rootView.findViewById(R.id.activity_Map_bottomSheet);
        bottomSheetBehavior = BottomSheetBehaviorGoogleMapsLike.from(bottomScrollView);
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);


        // 바텀시트를 올렸을때 내려오는 AppBar
        MergedAppBarLayout mergedAppBarLayout = findViewById(R.id.activity_Map_mergedAppbarLayout);
        mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);


        // BottomSheet Header
        layout_bottomHeader = findViewById(R.id.activity_Map_bottomSheet_header);
        text_pointName = findViewById(R.id.activity_Map_textView_pointName);
        text_pointAddress = findViewById(R.id.activity_Map_textView_pointAddress);
        text_pointDistance = findViewById(R.id.activity_Map_textView_pointDistance);

        setWebView();


        // 검색창 툴바

        toolbar = findViewById(R.id.activity_Map_toolBar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBlack));
        toolbar.inflateMenu(R.menu.search);
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.menu_search);

        SearchView searchView = (SearchView)toolbar.getMenu().findItem(R.id.menu_search).getActionView();
        EditText searchBox = searchView.findViewById (android.support.v7.appcompat.R.id.search_src_text);
        searchBox.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        searchBox.setHintTextColor(getResources().getColor(R.color.colorHalfInvisibleBlack));
        searchBox.setTextColor(getResources().getColor(R.color.colorBlack));
//        ImageView searchIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
//        searchIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.menu_search_64p));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }


    // BottomSheet 상태가 바뀔때마다 호출되는 콜백 함수
    private BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback bottomSheetCallback =
            new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState){
                // 살짝 보임
                case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                    bottomHeaderColorChange(false);
                    break;

                // 드래그
                case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                    bottomHeaderColorChange(true);
                    break;


                // 숨김
                case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                    // 맵뷰에서 POI 가 선택된 상태라면 상태를 해제한다.
                    break;

                // 전체화면
                case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:

                    break;

                case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:

                    break;

            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            // 1 STATE_EXPANDED
            // 0 peekHeight
            // -1 STATE_HIDDEN
            if(slideOffset > 0.7f)
                fabContainer.hide();
            else
                fabContainer.show();
        }
    };

    // BottomSheet 클릭할때 마다 색 바꿔주는 함수
    private void bottomHeaderColorChange(boolean isDragging) {
        if(isDragging){
            layout_bottomHeader.setBackgroundColor(getResources().getColor(R.color.colorRiver));
            text_pointAddress.setTextColor(getResources().getColor(R.color.colorWhite));
            text_pointName.setTextColor(getResources().getColor(R.color.colorWhite));
            fab_ARGuide.getBackground().mutate().setTint(getResources().getColor(R.color.colorWhite));
            fab_ARGuide.getDrawable().mutate().setTint(getResources().getColor(R.color.colorRiver));
        }else{
            layout_bottomHeader.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            text_pointAddress.setTextColor(getResources().getColor(R.color.colorBlack));
            text_pointName.setTextColor(getResources().getColor(R.color.colorBlack));
            fab_ARGuide.getBackground().mutate().setTint(getResources().getColor(R.color.colorRiver));
            fab_ARGuide.getDrawable().mutate().setTint(getResources().getColor(R.color.colorWhite));
        }

    }


    // 플로팅버튼 클릭리스너
    private FloatingActionButton.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.activity_Map_fab_currentLocation:
                    // 현재위치를 받아오고 맵뷰를 현재위치로 바꾼다.
                    fab_currentLocation.getDrawable().mutate().setTint(getResources().getColor(R.color.colorRiver));
                    moveMapCamera(currentPoint.latitude, currentPoint.longitude, 200, 100);
                    break;

                case R.id.activity_Map_fab_filter:
                    // 애니메이션 처리된 FilterDialog 띄우기
                    FilterDialogFragment dialogFragment = new FilterDialogFragment();
                    dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
                    break;

                case R.id.activity_Map_fab_ARGuide:
                    // 현재 보여지고 있는 POI Item 에서 좌표값을 얻어낸다
                    // Intent 에 값을 넣은 후 ARGuide 액티비티를 실행
                    Intent intent = new Intent(MapActivity.this, ARGuideActivity.class);

                    if( selectedPOIItem != null ){
                        Document document = (Document)selectedPOIItem.getUserObject();
                        intent.putExtra("Destination", document.getPlaceName());
                        intent.putExtra("Latitude", Double.parseDouble(document.getLatitude()));
                        intent.putExtra("Longitude", Double.parseDouble(document.getLongitude()));
                        startActivity(intent);
                    }
                    else
                        Toast.makeText(getApplicationContext(), "선택된 장소가 없습니다", Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };


    // WebView Setting
    private void setWebView() {
        webView = findViewById(R.id.activity_Map_bottomSheet_WebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // 웹뷰의 페이지 로딩이 끝났을경우
        webView.setWebViewClient(new WebViewClient());
    }



    @Override
    public void onBackPressed() {
        // 뒤로가기 눌렀을때 bottomScrollView 접기
        // POI Item 선택 해제
        // Bottom Sheet Scroll 맨위로 올리기
        // Bottom Sheet state change.
        int state = bottomSheetBehavior.getState();

        if (state == BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            bottomScrollView.setScrollY(0);
        }
        else if(state == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED){
            bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        }
        else if(state == BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT){
            bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        }

        else {
            super.onBackPressed();
        }
    }


    /**
     *  다음 맵 관련
     */

    // View Event 리스너
    private MapView.MapViewEventListener mapViewEventListener = new MapView.MapViewEventListener() {
        @Override
        public void onMapViewInitialized(MapView mapView) {
            // intent(카톡공유)가 없을시엔 GoogleApi 를 사용해서 현재위치 얻어오기

        }

        @Override
        public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewZoomLevelChanged(MapView mapView, int i) {

        }

        @Override
        public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
            // 바텀시트 가리기
            if(bottomSheetBehavior.getState() != BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN)
                onBackPressed();
        }

        @Override
        public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewLongPressed(MapView mapView, MapPoint point) {
            // 마커가 생성되며 지도 말풍선이 생성된다
            // 누르게되면 즐겨찾기 할수있게 기능 넣자.
            // 좌표에서 주소가져오는 함수 써야할듯
            removeMarkers();

            double pointLat = point.getMapPointGeoCoord().latitude;
            double pointLon = point.getMapPointGeoCoord().longitude;
            int distance = Point.distance(new LatLng(currentPoint.latitude, currentPoint.longitude), new LatLng(pointLat, pointLon));

            CommonPoint commonPoint = new CommonPoint("위치를 찾는중입니다..", pointLat, pointLon, 0, "");
            commonPoint.setDistance(distance);

            MapPOIItem poiItem = addMarker(commonPoint);
            poiItem.setUserObject(commonPoint);

            mapView.addPOIItem(poiItem);
            mapView.selectPOIItem(poiItem, true);
            mapPOIEventListener.onPOIItemSelected(mapView, poiItem);


            // 1. 찾기전 바텀시트를 collapse 상태로 만든다.
            bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);

            // 2. 찾는동안 progressbar 를 통해 찾는 중임을 보여준다.
            // 3. 찾았다면 바텀시트에 뿌려준다.

            new MapReverseGeoCoder(ApiService.KAKAO_APP_KEY, point, new MapReverseGeoCoder.ReverseGeoCodingResultListener(){
                        @Override
                        public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String address) {
                            text_pointName.setText(address);
                            selectedPOIItem.setItemName(address);
                            mergedAppBarLayoutBehavior.setToolbarTitle(address);
                        }

                        @Override
                        public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                            Toast.makeText(getApplicationContext(), "주소를 찾는데 실패했습니다", Toast.LENGTH_SHORT).show();
                        }
                    }, MapActivity.this
            ).startFindingAddress();

        }

        @Override
        public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
            fab_currentLocation.getDrawable().mutate().setTint(getResources().getColor(R.color.colorBlack));
        }

        @Override
        public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

        }
    };

    // Location 리스너
    private MapView.CurrentLocationEventListener mapLocationListener = new MapView.CurrentLocationEventListener() {
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

    // POI Event 리스너
    private MapView.POIItemEventListener mapPOIEventListener = new MapView.POIItemEventListener() {
        @Override
        public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
            // MapView 에 표시 돼 있는 마커를 선택했을 경우
            // 마커 위쪽에 말풍선으로 이름이 나오며 마커 색이 변한다.
            // 바텀시트에는 해당하는 마커의 이름과 주소가 나오고
            // 올렸을경우 webView 가 표시된다.

            selectedPOIItem = mapPOIItem;

            // object 의 클래스 타입에 따라서 다르게 세팅해주는 함수 만들어야함
            Object object = mapPOIItem.getUserObject();
            setBottomSheetContents(object);

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


    // 바텀시트의 내용물을 바꾼다.
    private void setBottomSheetContents(Object object) {
        String locationName = "";
        String locationAddress = "";
        String distance = "";

        if(object instanceof CommonPoint) {
            CommonPoint point = (CommonPoint)object;

            locationName = point.getName();
            locationAddress = point.getAddress();
            distance = String.format("%d", point.getDistance());

            if(point.hasUrl()) {
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(point.getUrl());
            }
            else
                webView.setVisibility(View.INVISIBLE);
        }
        else if(object instanceof CulturePoint){
            CulturePoint point = (CulturePoint)object;

            locationName = point.getName();
            locationAddress = point.getAddress();
            distance = String.format("%d", point.getDistance());

            webView.setVisibility(View.INVISIBLE);
            Log.d("LocationName In Bottom", locationName);

            webView.setWebViewClient(new CultureWebViewClient(locationName));
            webView.loadUrl("http://hangang.seoul.go.kr/project2018/search?keyword="+locationName+"&search_type=title_content");
        }

        text_pointName.setText(locationName);
        text_pointAddress.setText(locationAddress);
        text_pointDistance.setText(distance+" m");
        mergedAppBarLayoutBehavior.setToolbarTitle(locationName);
        fab_ARGuide.show();

        bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
    }


    // 모든 마커를 제거한다.
    private void removeMarkers(){
        mapView.removeAllPOIItems();
    }

    // 한개의 마커 추가
    private MapPOIItem addMarker(Point point){
        MapPOIItem poiItem = new MapPOIItem();
        poiItem.setItemName(point.getName());
        poiItem.setMapPoint(MapPoint.mapPointWithGeoCoord(point.latitude, point.longitude));
        poiItem.setMarkerType(MapPOIItem.MarkerType.BluePin);
        poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        mapView.addPOIItem(poiItem);

        return poiItem;
    }

    // 입력받은 위치로 카메라를 이동시킨다.
    private void moveMapCamera(double latitude, double longitude, float range, int padding) {
        mapView.moveCamera(CameraUpdateFactory.newMapPointAndDiameter(
                MapPoint.mapPointWithGeoCoord(latitude, longitude), range, padding));
    }





    /**
     *  Filter Dialog Dismiss CallBack
     *
     *  다이얼로그가 닫히게 되면 name, point 일 경우로 나눠진다.
     *
     *  name 일 경우 RestApi 를 사용해서 키워드 검색이 이루어지고, 얻은 목록들을 지도에 마커로 표시해준다.
     *
     *  point 일 경우 지도에 마커표시, point 의 이름, 주소만을 바텀시트에 표시한다.
     *
     */

    @Override
    public void onFilterDialogDismiss(FavoritePoint point) {
        removeMarkers();
        MapPOIItem poiItem = addMarker(point);
        mapView.selectPOIItem(poiItem, true);
        mapPOIEventListener.onPOIItemSelected(mapView, poiItem);
        moveMapCamera(point.latitude, point.longitude, SEARCH_RADIUS, 100);
    }

    @Override
    public void onFilterDialogDismiss(String keyword) {
        if(keyword.equals("행사장"))
            getPOIItemsByDB(poiCallback);
        else
            getPOIItemsByApi(keyword, poiCallback);
    }

    // POI CallBack
    private POICallback<MapPOIItem[]> poiCallback = new POICallback<MapPOIItem[]>() {
        @Override
        public void onNotFound() {
            removeMarkers();
            bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
            Toast.makeText(getApplicationContext(), "근처에 해당하는 장소가 없습니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Error error) {

        }

        @Override
        public void onSuccess(MapPOIItem[] receivedData) {
            // 이부분에 지도로 마커 표시해주기
            removeMarkers();
            mapView.addPOIItems(receivedData);
            MapPoint.GeoCoordinate viewPoint = MapPoint.mapPointWithScreenLocation(mapView.getX(), mapView.getY()).getMapPointGeoCoord();
            moveMapCamera(viewPoint.latitude, viewPoint.longitude, SEARCH_RADIUS, 100);
        }

        @Override
        public void onFailure(Error error) {
            Log.d("MapActivity Keyword", error.getLocalizedMessage());

        }
    };

    // DB 에서 행사장을 얻어와 POI Items 를 구한다.
    private void getPOIItemsByDB(POICallback<MapPOIItem[]> poiCallback) {
        DBHelper dbHelper = DBHelper.getInstance();
        ArrayList<CulturePoint> culturePoints = dbHelper.getCultureItems();
        int size = culturePoints.size();

        MapPoint.GeoCoordinate viewPoint = MapPoint.mapPointWithScreenLocation(mapView.getX(), mapView.getY()).getMapPointGeoCoord();
        LatLng mapLocation = new LatLng(viewPoint.latitude, viewPoint.longitude);

        Log.d("화면상 좌표", "lat : "+viewPoint.latitude + ", lon : " + viewPoint.longitude);

        ArrayList<MapPOIItem> poiItems = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            CulturePoint culturePoint = culturePoints.get(i);
            LatLng itemLocation = new LatLng(culturePoint.latitude, culturePoint.longitude);

            int distance = Point.distance(mapLocation, itemLocation);
            if (distance < SEARCH_RADIUS) {
                MapPOIItem poiItem = addMarker(culturePoint);
                culturePoint.setDistance(distance);
                poiItem.setUserObject(culturePoint);
                poiItems.add(poiItem);
            }
        }

        if(poiItems.size() == 0)
            poiCallback.onNotFound();
        else
            poiCallback.onSuccess(poiItems.toArray(new MapPOIItem[poiItems.size()]));

    }

    // Keyword RestApi 를 사용해 PoiItems 를 가져온다.
    private void getPOIItemsByApi(String keyword, final POICallback<MapPOIItem[]> poiCallback){
        Retrofit retrofit = RetrofitClient.getSearchClient();
        ApiService apiService = retrofit.create(ApiService.class);


        // API Call 할 때 mapPointWithScreenLocation() 함수를 사용해서
        // 현재 화면에서 중심점을 기준 Pixel 값을 기준으로 MapPoint 를 생성해서 대입한다.
        MapPoint.GeoCoordinate mapPoint = MapPoint.mapPointWithScreenLocation(mapView.getX(), mapView.getY()).getMapPointGeoCoord();

        Call<SearchPoint> call = apiService.getSearchPoints("KakaoAK " +ApiService.KAKAO_REST_KEY,
                keyword, mapPoint.longitude, mapPoint.latitude, SEARCH_RADIUS);

        call.enqueue(new Callback<SearchPoint>() {
            @Override
            public void onResponse(Call<SearchPoint> call, Response<SearchPoint> response) {
                if(response.isSuccessful()){
                    SearchPoint searchPoint = response.body();
                    ArrayList<Document> documents = (ArrayList<Document>) searchPoint.getDocuments();
                    int size = documents.size();

                    if(size == 0)
                        poiCallback.onNotFound();
                    else{
                        MapPOIItem[] poiItems = new MapPOIItem[size];

                        for(int i = 0; i < size; i++) {
                            CommonPoint commonPoint = new CommonPoint(documents.get(i));
                            poiItems[i] = addMarker(commonPoint);
                            poiItems[i].setUserObject(commonPoint);
                        }
                        poiCallback.onSuccess(poiItems);
                    }
                }
                else
                    poiCallback.onError(new Error(response.message()));
            }

            @Override
            public void onFailure(Call<SearchPoint> call, Throwable t) {
                poiCallback.onError(new Error(t.getLocalizedMessage()));
                Log.d("Retrofit Fail", "실패");
            }
        });
    }





    /**
     *  Google Location Api Service
     *   - 현재위치를 얻어 오는 Api
     */
    private void initLocationService() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(googleConnectionCallbacksListener)
                .addOnConnectionFailedListener(googleConnectionFailListener)
                .addApi(LocationServices.API)
                .build();
    }

    @SuppressWarnings("deprecation")
    private GoogleApiClient.ConnectionCallbacks googleConnectionCallbacksListener = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            LocationRequest locationRequest = new LocationRequest()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setFastestInterval(500);
            if ( Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
                return  ;

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, googleLocationListener);
            currentPoint = new Point(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
            currentPoint.setName("현재위치");
            moveMapCamera(currentPoint.latitude, currentPoint.longitude, 200, 100);
            Log.d("GoogleLocationApiClient", "onConnected");
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d("GoogleLocationApiClient", "onSuspended");
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
        }
    };

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}
