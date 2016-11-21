package com.busme_usuario;

import com.busme_usuario.POJO.Example;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by navneet on 17/7/16.
 */
public interface RetrofitMaps {

    /*
     * Retrofit get annotation with our URL
     * And our method that will return us details of student.
     */
    @GET("api/directions/json?key=AIzaSyC22GfkHu9FdgT9SwdCWMwKX1a4aohGifM")
    Call<Example> getDistanceDuration(@Query("units") String units, @Query("origin") String origin, @Query("destination") String destination, @Query("mode") String mode);

}
