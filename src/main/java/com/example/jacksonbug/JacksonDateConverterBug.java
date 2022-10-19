package com.example.jacksonbug;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;
import java.util.Date;

public class JacksonDateConverterBug {

  public static void main(String[] args) throws JsonProcessingException {
    var objectMapper = new ObjectMapper();
    objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    // Using Scores
    {
      var scoreRef = new ScoreReference();
      scoreRef.setScore(new Score(1234567890000L));
      var json = objectMapper.writeValueAsString(scoreRef);
      System.out.println("json=" + json);

      var obj = objectMapper.readValue(json, Object.class);
      System.out.println("obj=" + obj);
    }

    // Using Dates
    {
      var dateRef = new DateReference();
      dateRef.setDate(new Date());
      var json = objectMapper.writeValueAsString(dateRef);
      System.out.println("json=" + json);

      var obj = objectMapper.readValue(json, Object.class);
      System.out.println("obj=" + obj);

      // ^^^^^^^^
      // Throws com.fasterxml.jackson.databind.exc.MismatchedInputException:
      // Cannot deserialize value of type `java.lang.Long` from Array value (token `JsonToken.START_ARRAY`)
      // at [Source: (String)"{"@class":"com.example.jacksonbug.DateReference","date":["java.lang.Long",1666213651605]}";
      // line: 1, column: 57] (through reference chain: com.example.jacksonbug.DateReference["date"])
    }
  }

}

record Score(long score) {
}

class ScoreReference {
  @JsonSerialize(converter = CustomScoreConverter.Serializer.class)
  @JsonDeserialize(converter = CustomScoreConverter.Deserializer.class)
  private Score score;

  public Score getScore() {
    return score;
  }

  public void setScore(Score score) {
    this.score = score;
  }

  @Override
  public String toString() {
    return "ScoreReference{" +
        "score=" + score +
        '}';
  }
}


class CustomScoreConverter {

  public static class Deserializer extends StdConverter<Long, Score> {
    @Override
    public Score convert(Long value) {
      return value == null ? null : new Score(value);
    }
  }

  public static class Serializer extends StdConverter<Score, Long> {
    @Override
    public Long convert(Score value) {
      return value == null ? null : value.score();
    }
  }
}

class DateReference {
  @JsonSerialize(converter = CustomDateConverter.Serializer.class)
  @JsonDeserialize(converter = CustomDateConverter.Deserializer.class)
  private Date date;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  @Override
  public String toString() {
    return "DateReference{" +
        "date=" + date +
        '}';
  }
}

class CustomDateConverter {

  public static class Deserializer extends StdConverter<Long, Date> {
    @Override
    public Date convert(Long value) {
      return value == null ? null : Date.from(Instant.ofEpochMilli(value));
    }
  }

  public static class Serializer extends StdConverter<Date, Long> {
    @Override
    public Long convert(Date value) {
      return value == null ? null : value.toInstant().toEpochMilli();
    }
  }
}