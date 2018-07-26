package com.example.junmung.hangangparksmap;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.Toast;

import com.example.junmung.hangangparksmap.ARGuide.ARGuideActivity;
import com.example.junmung.hangangparksmap.Map.MapActivity;
import com.example.junmung.hangangparksmap.RetrofitUtil.ApiService;
import com.example.junmung.hangangparksmap.RetrofitUtil.RetrofitClient;
import com.example.junmung.hangangparksmap.SearchPointPOJO.Document;
import com.example.junmung.hangangparksmap.SearchPointPOJO.SearchPoint;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import net.daum.mf.map.api.MapReverseGeoCoder;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    Button arguide, map;

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


        test();

//
//        MapPoint aa = MapPoint.mapPointWithGeoCoord(37.5306703, 127.0670319);
//        MapReverseGeoCoder geoCoder = new MapReverseGeoCoder("6b0e7cad70e9269af5f7779a8c03903c",
//                aa,
//                listener, this);
//        geoCoder.startFindingAddress();

    }



    private void test(){
        Retrofit retrofit = RetrofitClient.getSearchClient();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<SearchPoint> call = apiService.getSearchPoints("KakaoAK " +ApiService.KAKAO_REST_KEY,
                "캠핑장",
                127.068976, 37.529235,
                2000);

        call.enqueue(new Callback<SearchPoint>() {
            @Override
            public void onResponse(Call<SearchPoint> call, Response<SearchPoint> response) {
                if(response.isSuccessful()){
                    SearchPoint searchPoint = response.body();
                    ArrayList<Document> documents = (ArrayList<Document>) searchPoint.getDocuments();
                    int i =0;
                    for(Document document: documents) {
                        Log.d("document Size", ""+ documents.size());
                        Log.d("Retrofit Success _ " + i, document.getPlaceName());
                        Log.d("Retrofit Success _ " + i, document.getPlaceUrl());

                        i++;
                    }


                }
            }

            @Override
            public void onFailure(Call<SearchPoint> call, Throwable t) {
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

    MapReverseGeoCoder.ReverseGeoCodingResultListener listener = new MapReverseGeoCoder.ReverseGeoCodingResultListener() {
        @Override
        public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
            Log.d("result2", s.toString());
        }

        @Override
        public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {

        }
    };

    PermissionListener permissionListener = new PermissionListener() {
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
