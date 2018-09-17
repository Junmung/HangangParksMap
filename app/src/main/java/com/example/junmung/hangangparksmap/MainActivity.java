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
import com.example.junmung.hangangparksmap.Culture.CultureActivity;
import com.example.junmung.hangangparksmap.CulturePointPOJO.CulturePojo;
import com.example.junmung.hangangparksmap.CulturePointPOJO.Mgishangang;
import com.example.junmung.hangangparksmap.CulturePointPOJO.Row;
import com.example.junmung.hangangparksmap.DataBase.DBHelper;
import com.example.junmung.hangangparksmap.Map.MapActivity;
import com.example.junmung.hangangparksmap.RetrofitUtil.ApiService;
import com.example.junmung.hangangparksmap.RetrofitUtil.RetrofitClient;
import com.example.junmung.hangangparksmap.SearchPointPOJO.SearchPoint;
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
    Button arguide, map, gate;
    DBHelper dbHelper;
    boolean is = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arguide = findViewById(R.id.btn_arguide);
        arguide.setOnClickListener(btnClickListener);
        map = findViewById(R.id.btn_map);
        map.setOnClickListener(btnClickListener);
        gate = findViewById(R.id.btn_gate);
        gate.setOnClickListener(btnClickListener);


        permissionCheck();


        DataBaseInit();
//        dbHelper.deleteFavoriteAll();

        sharingCheck();
        // 첫실행인지 쉐어드로 판단해서 실행하기
//        SharedPreferences preferences = getSharedPreferences("", MODE_PRIVATE);
//        getCultureInfos();



    }


    private Button.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_arguide:
                    Intent intent = new Intent(MainActivity.this, ARGuideActivity.class);
                    intent.putExtra("Destination", "목적지");
                    intent.putExtra("Latitude", 37.579540d);
                    intent.putExtra("Longitude", 127.086526d);
                    startActivity(intent);
                    break;

                case R.id.btn_map:
                    Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(mapIntent);
                    break;

                case R.id.btn_gate:
//                    Intent exitIntent = new Intent(MainActivity.this, MapActivity.class);
                    Intent exitIntent = new Intent(MainActivity.this, CultureActivity.class);
                    exitIntent.putExtra("Exiting", true);
                    startActivity(exitIntent);
                    break;
            }
        }
    };



    private void DataBaseInit() {
        Realm.init(getBaseContext());
        dbHelper = DBHelper.getInstance();
    }

    // 공유하기 기능에 의해 실행됐는지 확인하기
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


    // 권한체크
    private void permissionCheck(){
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("앱을 원활히 이용하기 위해선 권한이 필요합니다")
                .setDeniedMessage("거부하시면 앱의 사용이 어렵습니다")
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
