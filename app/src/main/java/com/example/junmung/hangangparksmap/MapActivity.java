package com.example.junmung.hangangparksmap;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.widget.NestedScrollView;
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


import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;



public class MapActivity extends AppCompatActivity {
    private FloatingActionButton fab_currentLocation, fab_filter, fab_ARGuide;
    private AnimatingLayout fabContainer ;

    private MapView mapView;
    private BottomSheetBehaviorGoogleMapsLike bottomSheetBehavior;
    private MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior;

    // Bottom Sheet Header
    private RelativeLayout layout_bottomHeader;
    private TextView text_pointName, text_pointAddress;

    NestedWebView webView;
    View bottomScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getID();

        // MapActivity 는 추후에 카카오톡 공유기능을 써야하기 때문에
        // Intent 로 좌표값을 받아온후 표시해줘야하는 것을 인지해야함

    }

    private void getID(){
        ViewGroup mapViewContainer = findViewById(R.id.activity_Map_mapView);
        mapView = new MapView(this);
        mapView.setMapViewEventListener(mapEventListener);
        mapViewContainer.addView(mapView);



        fabContainer = findViewById(R.id.activity_Map_fabContainer);
        fab_currentLocation = findViewById(R.id.activity_Map_fab_currentLocation);
        fab_currentLocation.setOnClickListener(fabClickListener);
        fab_filter = findViewById(R.id.activity_Map_fab_filter);
        fab_filter.setOnClickListener(fabClickListener);
        fab_ARGuide = findViewById(R.id.activity_Map_fab_ARGuide);
        fab_ARGuide.setOnClickListener(fabClickListener);


        CoordinatorLayout rootView = findViewById(R.id.activity_Map_rootView);
        bottomScrollView = rootView.findViewById(R.id.activity_Map_bottomSheet);
        bottomSheetBehavior = BottomSheetBehaviorGoogleMapsLike.from(bottomScrollView);
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);


        MergedAppBarLayout mergedAppBarLayout = findViewById(R.id.activity_Map_mergedAppbarLayout);
        mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle("스타벅스 사가정");


        layout_bottomHeader = findViewById(R.id.activity_Map_bottomSheet_header);
        text_pointName = findViewById(R.id.activity_Map_textView_pointName);
        text_pointAddress = findViewById(R.id.activity_Map_textView_pointAddress);

        setupWebView();


    }


    // BottomSheetCall 변수
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


                    // 현재 보여지고 있는 POI Item 에서 좌표값을 얻어낸다
                    // Intent 에 값을 넣은 후 ARGuide 액티비티를 실행

                    break;
            }

        }
    };

    // 다음 맵 이벤트 리스너
    private MapView.MapViewEventListener mapEventListener = new MapView.MapViewEventListener() {
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

    // 현재위치 업데이트
    private void updateCurrentLocation() {


    }

    @Override
    public void onBackPressed() {
        // 뒤로가기 눌렀을때 bottomScrollView 접기 및 POI Item 선택 해제
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


    private void setupWebView() {
        webView = findViewById(R.id.webView3);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });


        webView.loadUrl("http://place.map.daum.net/27121156");
    }





    // ----------- 옵션메뉴 넣을지 고민 --------
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
