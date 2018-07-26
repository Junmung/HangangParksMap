package com.example.junmung.hangangparksmap.Map;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.example.junmung.hangangparksmap.FavoritePoint;
import com.example.junmung.hangangparksmap.Map.Dialog.FilterDialogFragment;
import com.example.junmung.hangangparksmap.R;
import com.example.junmung.hangangparksmap.RetrofitUtil.ApiService;
import com.example.junmung.hangangparksmap.RetrofitUtil.RetrofitClient;
import com.example.junmung.hangangparksmap.SearchPointPOJO.Document;
import com.example.junmung.hangangparksmap.SearchPointPOJO.SearchPoint;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class MapActivity extends AppCompatActivity implements FilterDialogFragment.DialogDismissListener{
    private int SEARCH_RADIUS = 2000;

    private AnimatingLayout fabContainer;
    private FloatingActionButton fab_currentLocation, fab_filter, fab_ARGuide;

    private MapView mapView;
    private BottomSheetBehaviorGoogleMapsLike bottomSheetBehavior;
    private MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior;

    // Bottom Sheet Header
    private RelativeLayout layout_bottomHeader;
    private TextView text_pointName, text_pointAddress;

    private NestedWebView webView;
    private View bottomScrollView;

    public interface RetroCallback<T> {
        void onError(Error error);

        void onSuccess(T receivedData);

        void onFailure(Error error);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getID_SetListener();

        // MapActivity 는 추후에 카카오톡 공유기능을 써야하기 때문에
        // Intent 로 좌표값을 받아온후 표시해줘야하는 것을 인지해야함

    }

    private void getID_SetListener(){
        ViewGroup mapViewContainer = findViewById(R.id.activity_Map_mapView);
        mapView = new MapView(this);
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
        mergedAppBarLayoutBehavior.setToolbarTitle("스타벅스 사가정");


        // BottomSheet Header
        layout_bottomHeader = findViewById(R.id.activity_Map_bottomSheet_header);
        text_pointName = findViewById(R.id.activity_Map_textView_pointName);
        text_pointAddress = findViewById(R.id.activity_Map_textView_pointAddress);

        setWebView();
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
                    // 클릭했을때 버튼의 색이 강색으로 바뀌고,
                    // 지도를 드래그했을때 나침반모드 및 버튼의 색이 검은색으로 다시 바뀐다.
                    updateCurrentLocation();
                    fab_currentLocation.getDrawable().mutate().setTint(getResources().getColor(R.color.colorRiver));

                    break;

                case R.id.activity_Map_fab_filter:
                    // 필터 다이얼로그 띄우기 혹은 애니메이션 처리된 뷰 띄우기
                    bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                    fab_ARGuide.show();

                    break;

                case R.id.activity_Map_fab_ARGuide:
                    FilterDialogFragment dialogFragment = new FilterDialogFragment();
//                    getSupportFragmentManager().beginTransaction().add(dialogFragment, dialogFragment.getTag()).commit();

                    dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());

                    // 현재 보여지고 있는 POI Item 에서 좌표값을 얻어낸다
                    // Intent 에 값을 넣은 후 ARGuide 액티비티를 실행

                    break;
            }

        }
    };

    // 다음 맵 이벤트 리스너
    private MapView.MapViewEventListener mapViewEventListener = new MapView.MapViewEventListener() {
        @Override
        public void onMapViewInitialized(MapView mapView) {

        }

        @Override
        public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewZoomLevelChanged(MapView mapView, int i) {

        }

        @Override
        public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

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

    private MapView.POIItemEventListener mapPOIEventListener = new MapView.POIItemEventListener() {
        @Override
        public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
            // MapView 에 표시 돼 있는 마커를 선택했을 경우
            // 마커 위쪽에 말풍선으로 이름이 나오며 마커 색이 변한다.
            // 바텀시트에는 해당하는 마커의 이름과 주소가 나오고
            // 올렸을경우 webView 가 표시된다.
            Document document = (Document)mapPOIItem.getUserObject();
            text_pointName.setText(document.getPlaceName());
            text_pointAddress.setText(document.getAddressName());
            webView.loadUrl(document.getPlaceUrl());
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




    // 현재위치 업데이트
    private void updateCurrentLocation() {


    }



    private void setWebView() {
        webView = findViewById(R.id.activity_Map_bottomSheet_WebView);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);


        // 웹뷰의 페이지 로딩이 끝났을경우
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });


        webView.loadUrl("http://place.map.daum.net/27121156");
    }



    private void searchList(String keyword, final RetroCallback<MapPOIItem[]> callback){
        Retrofit retrofit = RetrofitClient.getSearchClient();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<SearchPoint> call = apiService.getSearchPoints("KakaoAK " +ApiService.KAKAO_REST_KEY,
                keyword, 127.068976, 37.529235, SEARCH_RADIUS);

        call.enqueue(new Callback<SearchPoint>() {
            @Override
            public void onResponse(Call<SearchPoint> call, Response<SearchPoint> response) {
                if(response.isSuccessful()){
                    SearchPoint searchPoint = response.body();
                    ArrayList<Document> documents = (ArrayList<Document>) searchPoint.getDocuments();
                    int size = documents.size();

                    MapPOIItem[] poiItems;

                    if(size < 0)
                        poiItems = null;
                    else{
                        poiItems = new MapPOIItem[size];

                        for(int i = 0; i < size; i++) {
                            poiItems[i] = new MapPOIItem();
                            poiItems[i].setItemName(documents.get(i).getPlaceName());
                            poiItems[i].setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(documents.get(i).getY()), Double.parseDouble(documents.get(i).getX())));
                            poiItems[i].setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                            poiItems[i].setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                            poiItems[i].setUserObject(documents.get(i));
                        }
                    }

                    callback.onSuccess(poiItems);
                }
                else{
                    callback.onError(new Error(response.message()));
                }
            }

            @Override
            public void onFailure(Call<SearchPoint> call, Throwable t) {
                callback.onError(new Error(t.getLocalizedMessage()));
                Log.d("Retrofit Fail", "실패");
            }
        });
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

        else {
            super.onBackPressed();
        }
    }


    /**
     *  필터 다이얼로그 공통
     *
     *  다이얼로그가 닫히게 되면 name, point 일 경우로 나눠진다.
     *
     *  name 일 경우 RestApi 를 사용해서 키워드 검색이 이루어지고, 얻은 목록들을 지도에 마커로 표시해준다.
     *
     *  point 일 경우 지도에 마커표시, point 의 이름, 주소만을 바텀시트에 표시한다.
     *
     */
    @Override
    public void onFilterDialogDismiss(String keyword) {
        RetroCallback<MapPOIItem[]> retroCallback = new RetroCallback<MapPOIItem[]>() {
            @Override
            public void onError(Error error) {
                Log.d("MapActivity Keyword", error.getLocalizedMessage());
            }

            @Override
            public void onSuccess(MapPOIItem[] receivedData) {
                // 이부분에 지도로 마커 표시해주기
                if(receivedData != null)
                    mapView.addPOIItems(receivedData);
                else
                    Log.d("onSuccess", "MapPoIItems is null");
            }

            @Override
            public void onFailure(Error error) {
                Log.d("MapActivity Keyword", error.getLocalizedMessage());

            }
        };

        searchList("화장실", retroCallback);
    }

    @Override
    public void onFilterDialogDismiss(FavoritePoint point) {
        text_pointName.setText(point.getName());
    }

    // ----------- 옵션메뉴  --------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo, menu);
        MenuItem item = menu.findItem(R.id.action_toggle);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_toggle: {

                return true;
            }
            case R.id.action_anchor: {

                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


}
