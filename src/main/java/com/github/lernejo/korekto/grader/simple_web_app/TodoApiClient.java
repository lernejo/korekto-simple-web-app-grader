package com.github.lernejo.korekto.grader.simple_web_app;

import java.util.List;

import com.github.lernejo.korekto.grader.simple_web_app.parts.Todo;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TodoApiClient {

    @POST("api/todo")
    @Headers("Content-Type:application/json")
    Call<ResponseBody> addTodo(@Body Todo user);

    @GET("api/todo")
    @Headers("Accept:application/json")
    Call<List<Todo>> getTodos();
}
