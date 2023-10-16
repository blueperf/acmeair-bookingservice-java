package com.acmeair.rm;

import java.util.Objects;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

public class MilesUpdate {

  private static final Jsonb jsonb = JsonbBuilder.create();

  private String userId;
  private int miles;

  public MilesUpdate(String userId, int miles) {
    this.userId = userId;
    this.miles = miles;
  }

  public MilesUpdate() {
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setMiles(int miles) {
    this.miles = miles;
  }

  public int getMiles() {
    return miles;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof MilesUpdate))
      return false;
    MilesUpdate sl = (MilesUpdate) o;
    return Objects.equals(userId, sl.userId)
        && Objects.equals(miles, sl.miles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, miles);
  }

  @Override
  public String toString() {
    return "MilesUpdate: " + jsonb.toJson(this);
  }

  public static class MilesUpdateSerializer implements Serializer<Object> {
    @Override
    public byte[] serialize(String topic, Object data) {
      return jsonb.toJson(data).getBytes();
    }
  }

  public static class MilesUpdateDeserializer implements Deserializer<MilesUpdate> {
    @Override
    public MilesUpdate deserialize(String topic, byte[] data) {
      if (data == null)
        return null;
      return jsonb.fromJson(new String(data), MilesUpdate.class);
    }
  }
}

