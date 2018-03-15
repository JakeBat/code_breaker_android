package edu.cnm.deepdive.codebreakerandroid.service;

import edu.cnm.deepdive.codebreakerandroid.model.Guess;
import java.util.List;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GuessService {

  @Headers("Accept: application/json")
  @GET("games/{gameId}/guesses")
  Call<List<Guess>> list(@Path("gameId") UUID gameId);

  @Headers({
      "Accept: application/json",
      "Content-type: application/json"
  })
  @POST("games/{gameId}/guesses")
  Call<Guess> create(@Path("gameId") UUID gameId, @Body Guess guess);

  @Headers("Accept: application/json")
  @GET("games/{gameId}/guesses/{guessId}")
  Call<Guess> read(@Path("gameId") UUID gameId, @Path("guessId") UUID guessId);

}
