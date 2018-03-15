package edu.cnm.deepdive.codebreakerandroid.service;

import edu.cnm.deepdive.codebreakerandroid.model.Game;
import java.util.List;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface GameService {

  @Headers("Accept: application/json")
  @GET("games")
  Call<List<Game>> list();

  @Headers("Content-type: application/json")
  @POST("games")
  Call<Game> create(@Body Game game);

  @Headers("Accept: application/json")
  @GET("games/{id}")
  Call<Game> read(@Path("id") UUID id);

  @DELETE("games/{id}")
  Call<Game> delete(@Path("id") UUID id);

  @Headers({
      "Content-type: text/plain",
      "Accept: text/plain"
  })
  @PUT("games/{id}/surrendered")
  Call<Boolean> surrender(@Path("id") UUID id, @Body boolean surrender);

}
