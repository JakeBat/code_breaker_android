package edu.cnm.deepdive.codebreakerandroid.model;

import com.google.gson.annotations.Expose;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

public class Guess {

  @Expose(deserialize = true, serialize = false)
  private UUID id;

  @Expose(deserialize = true, serialize = false)
  private URI href;

  @Expose(deserialize = true, serialize = false)
  private Date created;

  @Expose
  private String guess;

  @Expose(deserialize = true, serialize = false)
  private int inPlace;

  @Expose(deserialize = true, serialize = false)
  private int outOfPlace;

  public UUID getId() {
    return id;
  }

  public URI getHref() {
    return href;
  }

  public Date getCreated() {
    return created;
  }

  public String getGuess() {
    return guess;
  }

  public void setGuess(String guess) {
    this.guess = guess;
  }

  public int getInPlace() {
    return inPlace;
  }

  public int getOutOfPlace() {
    return outOfPlace;
  }

}
