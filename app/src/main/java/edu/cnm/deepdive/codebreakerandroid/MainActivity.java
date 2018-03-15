package edu.cnm.deepdive.codebreakerandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.cnm.deepdive.codebreakerandroid.model.Game;
import edu.cnm.deepdive.codebreakerandroid.model.Guess;
import edu.cnm.deepdive.codebreakerandroid.service.GameService;
import edu.cnm.deepdive.codebreakerandroid.service.GuessService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {


  private static final String SERVICE_URL_KEY = "baseUrl";
  private static final String GAME_PROMPT_KEY = "gamePrompt";
  private static final String GUESS_PROMPT_KEY = "guessPrompt";
  private static final String GUESS_LENGTH_ERROR_KEY = "guessLengthError";
  private static final String GUESS_RESULT_KEY = "guessResult";
  private static final String POSITIVE_CONDITION_KEY = "positivePluralCondition";
  private static final String NEGATIVE_CONDITION_KEY = "negativePluralCondition";
  private static final String SURRENDER_PROMPT_KEY = "surrenderPrompt";
  private static final String AFFIRMATIVE_CHARACTER_KEY = "affirmativeCharacter";
  private static final String SURRENDER_RESULT_KEY = "surrenderResult";
  private static final String SOLVED_RESULT_KEY = "solvedResult";

  private GameService gameService;
  private GuessService guessService;
  private Game game;
  private TextView codeInfo;
  private TextView guessText;
  private TextView guessResponse;
  private EditText guessEdit;
  Guess guess = new Guess();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    codeInfo = findViewById(R.id.code_info);
    guessText = findViewById(R.id.guess_text);
    guessResponse = findViewById(R.id.guess_response);
    guessEdit = findViewById(R.id.guess);

    Client client = new Client();
    try {
      client.setup();
      client.play();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private class Client {

    private void setup() throws IOException {
      Gson gson = new GsonBuilder()
          .excludeFieldsWithoutExposeAnnotation()
          .create();
      Retrofit retrofit = new Retrofit.Builder()
          .baseUrl(getString(R.string.base_url))
          .addConverterFactory(GsonConverterFactory.create(gson))
          .build();
      gameService = retrofit.create(GameService.class);
      guessService = retrofit.create(GuessService.class);
    }

    private void play() throws IOException {
      game = new Game();
      // TODO Set game fields here (e.g. from properties or command line).
      gameService.create(game).enqueue(new Callback<Game>() {
        @Override
        public void onResponse(Call<Game> call, Response<Game> response) {
          game = response.body();
        }

        @Override
        public void onFailure(Call<Game> call, Throwable t) {

        }
      });
      boolean keepPlaying = true;
      int guessCount = 1;
      codeInfo.setText(String.format(getString(R.string.game_prompt),
          game.getLength(), game.getCharacters(), game.isRepetitionAllowed()
              ? getString(R.string.positive_plural_condition)
              : getString(R.string.negative_plural_condition)));
      while (keepPlaying) {
        guessText.setText(String.format(getString(R.string.guess_prompt), guessCount));
        String input = guessEdit.getText().toString();
        while (input.length() != game.getLength() && !input.isEmpty()) {
          guessResponse.setText(String.format(getString(R.string.guess_length_error), game.getLength()));
          input = guessEdit.getText().toString();
        }
        if (input.isEmpty()) {
          try {
            keepPlaying = !surrender();
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          try {
            if (guess(input)) {
              keepPlaying = false;
            } else {
              guessCount++;
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    private boolean guess(final String input) throws IOException {
          guess.setGuess(input);
            guess = guessService.create(game.getId(), guess).execute().body();
            game = gameService.read(game.getId()).execute().body();
      
      if (input.equals("lazy")) {
        solve();
        return true;
      }
      if (game.isSolved()) {
        guessResponse.setText(String.format(getString(R.string.solved_result), guess.getGuess()));
        return true;
      } else {
        guessResponse.setText(String.format(getString(R.string.guess_result), guess.getInPlace(), guess
            .getOutOfPlace()));
        return false;
      }
    }


    private boolean surrender() throws IOException {
      guessResponse.setText(String.format(getString(R.string.surrender_prompt)));
      String input = guessEdit.getText().toString();
      if (!input.isEmpty()
          && (input.charAt(0) == getString(R.string.affirmative_character).charAt(0))) {
        gameService.surrender(game.getId(), true).execute();
        game = gameService.read(game.getId()).execute().body();
        if (game.isSurrendered()) {
          guessResponse.setText(String.format(getString(R.string.surrender_result), game.getCode()));
          return true;
        }
      }
      return false;
    }

    private void solve() throws IOException {
      String characters = game.getCharacters();
      List<String> charList = new ArrayList<>();
      Guess test = new Guess();
      for (int i = 0; i < characters.length(); i++) {
        charList.add(String.valueOf(characters.charAt(i)));
      }
      for (int j = 0; j < charList.size(); j++) {
        String one = charList.get(j);
        if (game.isSolved()) {
          break;
        }
        for (int x = 0; x < charList.size(); x++) {
          String two = charList.get(x);
          if (game.isSolved()) {
            break;
          }
          for (int y = 0; y < charList.size(); y++) {
            String three = charList.get(y);
            if (game.isSolved()) {
              break;
            }
            for (int z = 0; z < charList.size(); z++) {
              String four = charList.get(z);
              test.setGuess(one + two + three + four);
              test = guessService.create(game.getId(), test).execute().body();
              game = gameService.read(game.getId()).execute().body();
              if (game.isSolved()) {
                guessResponse.setText(String.format(getString(R.string.solved_result), test.getGuess()));
                break;
              }
            }
          }
        }
      }
    }
  }
}
