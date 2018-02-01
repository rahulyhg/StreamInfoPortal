package com.hpm.sp.streaminfoportal.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.hpm.sp.streaminfoportal.BuildConfig;
import com.hpm.sp.streaminfoportal.Interfaces.ResponseInterface;
import com.hpm.sp.streaminfoportal.Models.ResultDataObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kumardivyarajat on 1/21/18.
 */

public class NetworkHelper {

    public static final int versionCode = BuildConfig.VERSION_CODE;
    public static final String versionName = BuildConfig.VERSION_NAME;
    public static final String APP_NAME = "Sri Sosale Vyasaraja Matha ";
    public static final String APP_NAME_WITH_VERSION_CODE = "Sri Sosale Vyasaraja Matha " + versionCode;

    private static final String TAG = NetworkHelper.class.getSimpleName();
    static volatile NetworkHelper singleton = null;
    private static NetworkHelper ourInstance = new NetworkHelper();
    private static AppConfig config;
    private static Retrofit retrofit = null;
    private static ApiInterface apiService;
    private static String baseUrl;

    private NetworkHelper() {
        Log.d(TAG, "Instance created");
    }

    public static NetworkHelper getInstance() {
        if (singleton == null) {
            synchronized (NetworkHelper.class) {
                if (singleton == null) {
                    singleton = ourInstance;
                }
            }
        }
        return singleton;
    }

    public static void initialize(AppConfig appConfig) {
        config = appConfig;
        if (config.isLoggingEnabled()) {
            Log.d("Request URL ---", appConfig.getBaseUrl());
        }
        baseUrl = appConfig.getBaseUrl();
        if (retrofit == null) {
            OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                okHttpBuilder.addInterceptor(interceptor);
            }
            OkHttpClient client = okHttpBuilder
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(appConfig.getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        apiService = retrofit.create(ApiInterface.class);
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static AppConfig getConfig() {
        return config;
    }


    public static void getAllEvents(final ResponseInterface responseInterface) {
        Call<ResultDataObject> call = apiService.getAllEvents();


        call.enqueue(new Callback<ResultDataObject>() {
            @Override
            public void onResponse(Call<ResultDataObject> call, Response<ResultDataObject> response) {
                Log.d(TAG, "onResponse: " + response.body());
                responseInterface.onResponseFromServer(response.body().getEvent(), null);
            }

            @Override
            public void onFailure(Call<ResultDataObject> call, Throwable t) {
                responseInterface.onResponseFromServer(null, new Exception(t));
            }
        });

    }

    public static void getAllVideos(final ResponseInterface responseInterface) {
        Call<ResultDataObject> call = apiService.getAllVideos();


        call.enqueue(new Callback<ResultDataObject>() {
            @Override
            public void onResponse(Call<ResultDataObject> call, Response<ResultDataObject> response) {
                responseInterface.onResponseFromServer(response.body().getVideos(), null);
            }

            @Override
            public void onFailure(Call<ResultDataObject> call, Throwable t) {
                responseInterface.onResponseFromServer(null, new Exception(t));
            }
        });

    }

}
