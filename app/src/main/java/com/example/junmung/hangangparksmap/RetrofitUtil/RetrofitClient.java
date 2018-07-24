package com.example.junmung.hangangparksmap.RetrofitUtil;

import com.google.gson.Gson;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit guideRetrofit = null;
    private static Retrofit searchRetrofit = null;

    public static Retrofit getGuideClient(Gson gson){
        if(guideRetrofit == null){
            guideRetrofit = new Retrofit.Builder()
                    .baseUrl("https://api2.sktelecom.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return guideRetrofit;
    }

    public static Retrofit getSearchClient(){
        if(searchRetrofit == null){
            searchRetrofit = new Retrofit.Builder()
                    .baseUrl("https://dapi.kakao.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return searchRetrofit;
    }
}
