package com.rrreyes.prototype.timekeeping.Interfaces;

import com.rrreyes.prototype.timekeeping.Models.BasicResponse;
import com.rrreyes.prototype.timekeeping.Models.DTRSync;
import com.rrreyes.prototype.timekeeping.Models.Employee;
import com.rrreyes.prototype.timekeeping.Models.Login;
import com.rrreyes.prototype.timekeeping.Models.LoginCredentials;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public interface TKService {

    @POST("/user/jar/v1/login")
    Call<Login> loginUser(@Body LoginCredentials data);

    @GET("/employee/jar/v1/{compID}")
    Call<Employee> getAllEmployees(@Path("compID") int id);

    /*@FormUrlEncoded
    @POST("/employee/jar/v1/sync/dtr/{compID}")
    Call<BasicResponse> sendDTR(
            @Path("compID") int id,
            @Field("start_date") String sd,
            @Field("end_date") String ed,
            @Field("branch_id") int bid,
            @Field("company_id") int cid,
            @Field("user_id") int uid,
            @Field("dtr") DTRSyncList list);*/

    @POST("/employee/jar/v1/sync/dtr/{compID}")
    Call<BasicResponse> submitDTR(@Path("compID") int id, @Body DTRSync data);

    @FormUrlEncoded
    @POST("/employee/jar/v1/sync/dtr/{compID}")
    Call<BasicResponse> sendDTR(
            @Path("compID") int id,
            @Field("branch_id") int bid,
            @Field("user_id") int uid,
            @Field("start_date") String date,
            @Field("emp_id") String barcode,
            @Field("time_in") String timeIn,
            @Field("time_out") String timeOut,
            @Field("lunch_in") String lunchIn,
            @Field("lunch_out") String lunchOut,
            @Field("img_url") String imgUrl);
}
