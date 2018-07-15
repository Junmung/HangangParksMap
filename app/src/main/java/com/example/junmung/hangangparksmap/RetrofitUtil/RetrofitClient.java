package com.example.junmung.hangangparksmap.RetrofitUtil;

import com.google.gson.Gson;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Gson gson){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api2.sktelecom.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }
}
