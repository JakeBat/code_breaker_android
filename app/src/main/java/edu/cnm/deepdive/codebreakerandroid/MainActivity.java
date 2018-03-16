package edu.cnm.deepdive.codebreakerandroid;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

  private int guessCount;
  private GameService gameService;
  private GuessService guessService;
  private Game game;
  private TextView codeInfo;
  private TextView guessText;
  private TextView guessResponse;
  private EditText guessEdit;
  private EditText lengthEdit;
  private EditText charEdit;
  private EditText dupEdit;
  private Button guessButton;
  private Button resetButton;
  Guess guess = new Guess();
  Guess test = new Guess();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    codeInfo = findViewById(R.id.code_info);
    guessText = findViewById(R.id.guess_text);
    guessResponse = findViewById(R.id.guess_response);
    guessEdit = findViewById(R.id.guess_edit);
    lengthEdit = findViewById(R.id.length_edit);
    charEdit = findViewById(R.id.char_edit);
    dupEdit = findViewById(R.id.dup_edit);
    guessButton = findViewById(R.id.guess_button);
    resetButton = findViewById(R.id.reset_button);

    guessButton.setEnabled(false);

    try {
      setup();
    } catch (IOException e) {
      e.printStackTrace();
    }

    guessButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        guessButton.setEnabled(false);
        guessEdit.setEnabled(false);
        resetButton.setEnabled(false);
        String input = guessEdit.getText().toString();
        if (input.length() != game.getLength() && !input.equals("lazy") && !input.equals("surrender")) {
          guessResponse
              .setText(String.format(getString(R.string.guess_length_error), game.getLength()));
          guessButton.setEnabled(true);
          guessEdit.setEnabled(true);
          resetButton.setEnabled(true);
        } else if (input.equals("lazy")) {
          guess.setGuess(input);
          new LazySolve().execute(guess);
        } else if (input.equals("surrender")) {
          guess.setGuess(input);
          new Surrender().execute(guess);
        } else {
          guess.setGuess(input);
          new GuessRequest().execute(guess);
        }
      }
    });

    resetButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          setup();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }


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
    game = new Game();
    game.setLength(Integer.parseInt(lengthEdit.getText().toString()));
    game.setCharacters(charEdit.getText().toString());
    game.setRepetitionAllowed(Boolean.parseBoolean(dupEdit.getText().toString()));
    new CreateGame().execute(game);
  }

  private class GuessRequest extends AsyncTask<Guess, Void, Guess> {

    Exception exception;

    @Override
    protected Guess doInBackground(Guess... guesses) {
      try {
        guess = guessService.create(game.getId(), guesses[0]).execute().body();
        game = gameService.read(game.getId()).execute().body();
        return guess;
      } catch (IOException e) {
        exception = e;
        cancel(true);
        return null;
      }

    }


    @Override
    protected void onPostExecute(Guess guess) {
      if (game.isSolved()) {
        // TODO play again button
        guessResponse.setText(String.format(getString(R.string.solved_result), guess.getGuess()));
        resetButton.setEnabled(true);
      } else {
        guessResponse
            .setText(String.format(getString(R.string.guess_result), guess.getInPlace(), guess
                .getOutOfPlace()));
        guessText.setText(String.format(getString(R.string.guess_prompt), ++guessCount));
        guessButton.setEnabled(true);
        guessEdit.setEnabled(true);
      }
    }

    @Override
    protected void onCancelled(Guess guess) {

    }
  }

  private class CreateGame extends AsyncTask<Game, Void, Game> {


    private Exception exception;


    @Override
    protected Game doInBackground(Game... games) {
      try {
        return gameService.create(games[0]).execute().body();
      } catch (IOException e) {
        exception = e;
        cancel(true);
        return null;
      }
    }

    @Override
    protected void onPostExecute(Game game) {
      MainActivity.this.game = game;
      guessButton.setEnabled(true);
      guessEdit.setEnabled(true);
      guessCount = 1;
      codeInfo.setText(String.format(getString(R.string.game_prompt),
          game.getLength(), game.getCharacters(), game.isRepetitionAllowed()
              ? getString(R.string.positive_plural_condition)
              : getString(R.string.negative_plural_condition)));
      guessText.setText(String.format(getString(R.string.guess_prompt), guessCount));
    }

    @Override
    protected void onCancelled(Game game) {

    }
  }

  private class LazySolve extends AsyncTask<Guess, Integer, Guess> {

    @Override
    protected Guess doInBackground(Guess... guesses) {
      String characters = game.getCharacters();
      List<String> charList = new ArrayList<>();
      List<String> testList = new ArrayList<>();
      int correct;
      for (int i = 0; i < game.getLength(); i++) {
        testList.add("A");
      }
      for (int i = 0; i < characters.length(); i++) {
        charList.add(String.valueOf(characters.charAt(i)));
      }
      String testGuess = "";
      for (int i = 0; i < testList.size(); i++) {
        testGuess += testList.get(i);
      }
      test.setGuess(testGuess);
      try {
        test = guessService.create(game.getId(), test).execute().body();
        game = gameService.read(game.getId()).execute().body();
      } catch (IOException e) {
        e.printStackTrace();
      }
      for (int i = 0; i != game.getLength(); i++) {
        for (int j = 0; test.getInPlace() != game.getLength(); j++) {
          testList.set(i, charList.get(j));
          testGuess = "";
          for (int x = 0; x < testList.size(); x++) {
            testGuess += testList.get(x);
          }
          test.setGuess(testGuess);
          correct = test.getInPlace();
          try {
            test = guessService.create(game.getId(), test).execute().body();
            game = gameService.read(game.getId()).execute().body();
          } catch (IOException e) {
            e.printStackTrace();
          }
          guessCount++;
          publishProgress(guessCount);
          if (test.getInPlace() < correct) {
            testList.set(i, charList.get(--j));
            break;
          } else if (test.getInPlace() > correct) {
            break;
          }
        }
      }
      return test;
    }


    @Override
    protected void onPostExecute(Guess guess) {
      guessResponse
          .setText(String.format(getString(R.string.solved_result), test.getGuess()));
      guessText.setText(String.format(getString(R.string.guess_prompt), guessCount));
      resetButton.setEnabled(true);
    }

    @Override
    protected void onProgressUpdate(final Integer... values) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          guessResponse.setText(String.format("%d guesses have been made!", values[0]));
        }
      });
    }

    @Override
    protected void onCancelled() {

    }
  }

  private class Surrender extends AsyncTask<Guess, Void, Guess> {

    Exception exception;

    @Override
    protected Guess doInBackground(Guess... guesses) {
      try {
        gameService.surrender(game.getId(), true).execute();
        game = gameService.read(game.getId()).execute().body();
        return guess;
      } catch (IOException e) {
        exception = e;
        cancel(true);
        return null;
      }

    }


    @Override
    protected void onPostExecute(Guess guess) {
      guessResponse.setText(String.format(getString(R.string.surrender_result), game.getCode()));
      resetButton.setEnabled(true);
    }

    @Override
    protected void onCancelled(Guess guess) {

    }
  }
}

