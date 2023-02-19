package com.example.coviddata.service.api;

import com.example.coviddata.entity.netEaseJson.BaseResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface DataSourceApi {
    @GET("/news/wap/fymap2020_data.d.json")
    public Call<com.example.coviddata.entity.sinaJson.BaseResponse> getSinaData();

    @GET("/ug/api/wuhan/app/data/list-total")
    public Call<BaseResponse> getNetEaseData();

    @GET("/contentdtos.js")
    public Call<ResponseBody> getSoJsonData();
}
