package com.example.junmung.hangangparksmap.RetrofitUtil;

import com.github.filosganga.geogson.model.FeatureCollection;


import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TMapApiService {
    static final String TMAP_APPKEY = "b6c4c25a-f4d1-46e9-8e8f-15a8132a23b2";

    @POST("tmap/routes/pedestrian?version=1" +
            "&format=json" +
            "&reqCoordType=WGS84GEO" +
            "&resCoordType=WGS84GEO")
    Call<FeatureCollection> getMapPointInfos(@Header("appKey")String appKey, @Query("startName")String startName, @Query("endName")String endName,
                                             @Query("startX")Number startX, @Query("startY")Number startY,
                                             @Query("endX")Number endX, @Query("endY")Number endY);

}


