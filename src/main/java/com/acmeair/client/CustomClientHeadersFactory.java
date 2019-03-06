package com.acmeair.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@ApplicationScoped
public class CustomClientHeadersFactory {

  /* microprofile-1.1 */
  @Inject @ConfigProperty(name = "SECURE_SERVICE_CALLS", defaultValue = "true")
  private Boolean secureServiceCalls;
  
  //TODO: Hardcode for now
  private static final String secretKey = "acmeairsecret128";

  public String generateHeader(String headerName) {
    if (!secureServiceCalls) {
      return "";
    } 

    String token = "";

    try {
      Algorithm algorithm = Algorithm.HMAC256(secretKey);
      token = JWT.create()
          .withSubject("admin")
          .sign(algorithm);
    } catch (Exception exception) {
      exception.printStackTrace(); 
    }
    return "Bearer " + token;

  }
}
