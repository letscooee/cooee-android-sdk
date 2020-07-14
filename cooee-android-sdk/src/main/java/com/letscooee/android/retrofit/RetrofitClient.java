package com.letscooee.android.retrofit;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.1.12:5643/";
    private static Retrofit retrofit = null;

    public static ServerAPIService getServerAPIService() {
        Retrofit retrofit = getClient(BASE_URL);
        return retrofit.create(ServerAPIService.class);
    }

    public static Retrofit getClient(String baseUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
//                        Add Headers to all request
//                        Request originalRequest = chain.request();
//                        Request copyRequest = originalRequest.newBuilder()
//                                .header("header1","value1")
//                                .build();
//                        return chain.proceed(copyRequest);
                        return chain.proceed(chain.request());
                    }
                })
                .build();
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}
