package com.example.junmung.hangangparksmap;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.example.junmung.hangangparksmap.ARGuide.ARGuideActivity;
import com.example.junmung.hangangparksmap.CulturePointPOJO.CulturePojo;
import com.example.junmung.hangangparksmap.CulturePointPOJO.Mgishangang;
import com.example.junmung.hangangparksmap.CulturePointPOJO.Row;
import com.example.junmung.hangangparksmap.DataBase.DBHelper;
import com.example.junmung.hangangparksmap.Map.MapActivity;
import com.example.junmung.hangangparksmap.RetrofitUtil.ApiService;
import com.example.junmung.hangangparksmap.RetrofitUtil.RetrofitClient;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    Button arguide, map;
    WebView webView;
    DBHelper dbHelper;
    boolean is = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arguide = findViewById(R.id.btn);
        arguide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btn){
                    Intent intent = new Intent(MainActivity.this, ARGuideActivity.class);
                    intent.putExtra("Destination", "목적지");
                    intent.putExtra("Latitude", 37.579540d);
                    intent.putExtra("Longitude", 127.086526d);
                    startActivity(intent);
                }
            }
        });

        map = findViewById(R.id.btn_map);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btn_map){
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);
                }
            }
        });



        permissionCheck();


        DataBaseInit();
//        dbHelper.deleteFavoriteAll();

        sharingCheck();
        // 첫실행인지 쉐어드로 판단해서 실행하기
//        SharedPreferences preferences = getSharedPreferences("", MODE_PRIVATE);
//        getCultureInfos();

    }

    private void DataBaseInit() {
        Realm.init(getBaseContext());
        dbHelper = DBHelper.getInstance();
    }

    private void sharingCheck() {
        Intent sharingIntent= getIntent();
        if(sharingIntent.getAction() == Intent.ACTION_VIEW){
            Uri uri = sharingIntent.getData();

            Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
            mapIntent.setData(uri);
            mapIntent.putExtra("Sharing", true);
            startActivity(mapIntent);
        }
    }


    /**
     *  한강몽땅 자료 파싱
     *  서울시에서는 한강관련한 여름 행사인 '한강몽땅'을 진행하고있다.
     *  이 앱에서는 행사들의 정보를 사용자에게 보여주어야 하는데
     *  서울시가 제공하는 '지도태깅 API' 에서는 행사사진이나 Url 등 자세한 정보를 제공하지 않는다.
     *  먼저, Android WebView 로 '한강몽땅' 웹페이지에 들어가서 검색 Url 을 가져왔다.
     *  사용자가 보고싶어하는 행사의 제목을 검색 하였을 때,
     *  WebView 내의 웹페이지에서는 해당하는 목록이 뜨게 된다.
     *  웹페이지 Html Tag 중 행사제목과 같은 Tag 를 찾아내어, href 주소를 통해 들어가야만
     *  해당하는 행사의 세부정보를 볼 수 있다.
     *  WebView 의 loadUrl() 함수를 사용하여 Javascript + jQuery 문법으로 href 를 찾아낸 후,
     *  최종적으로 window.location.href 를 사용하여 세부정보를 사용자에게 보여준다.
     */
    private void setWebView(){
        webView = findViewById(R.id.webview);
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        // 웹뷰의 페이지 로딩이 끝났을경우
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(is){
                    webView.loadUrl("javascript:(" +
                                    "function($) {" +
                                        "window.location.href = $(\".cnt-theme h4 a span:contains('미스터캡틴(Mr.Captain)')\").parent().attr('href');" +
                                    "}"+
                                    ")(jQuery)");

                    is = false;
                }
                else
                    webView.setVisibility(View.VISIBLE);
            }
        });

        webView.loadUrl("http://hangang.seoul.go.kr/project2018/search?keyword=미스터캡틴&search_type=title_content");

    }


    private void getCultureInfos(){
        Retrofit retrofit = RetrofitClient.getCultureCilent();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<CulturePojo> call = apiService.getCulturePoints();

        call.enqueue(new Callback<CulturePojo>() {
            @Override
            public void onResponse(Call<CulturePojo> call, Response<CulturePojo> response) {
                if(response.isSuccessful()){
                    CulturePojo culturePoint = response.body();
                    Mgishangang mgishangang = culturePoint.getMgishangang();
                    ArrayList<Row> rows = (ArrayList<Row>) mgishangang.getRow();

                    dbHelper.insertCultureInfos(rows);
                }
            }

            @Override
            public void onFailure(Call<CulturePojo> call, Throwable t) {
                Log.d("Retrofit Fail", "실패");
                t.printStackTrace();
            }
        });
    }

    private void permissionCheck(){
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("앱을 원활히 이용하기 위해선 권한이 필요합니다")
                .setDeniedMessage("이걸 거부한다고?")
                .setPermissions(
                        Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION
                )
                .check();
    }

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
//            Toast.makeText(getApplicationContext(), "권한허가", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한거부\n"+deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

        }
    };
}
