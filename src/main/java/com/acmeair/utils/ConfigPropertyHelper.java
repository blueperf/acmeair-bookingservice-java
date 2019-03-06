package com.acmeair.utils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConfigPropertyHelper {

  @Inject 
  @ConfigProperty(name = "SECURE_USER_CALLS", defaultValue = "true") 
  private Boolean secureUserCalls;

  @Inject 
  @ConfigProperty(name = "SECURE_SERVICE_CALLS", defaultValue = "true") 
  private Boolean secureServiceCalls;

  @Inject 
  @ConfigProperty(name = "TRACK_REWARD_MILES", defaultValue = "true") 
  private Boolean trackRewardMiles; 
   
  @PostConstruct
  private void initialize() {

    System.out.println("SECURE_USER_CALLS: " + secureUserCalls);
    System.out.println("SECURE_SERVICE_CALLS: " + secureServiceCalls);
    System.out.println("TRACK_REWARD_MILES: " + trackRewardMiles);

  }
  public boolean secureUserCalls() {
    return secureUserCalls;
  }
  public boolean secureServiceCalls() {
    return secureServiceCalls;
  }
  public boolean trackRewardMiles() {
    return trackRewardMiles;
  }
}
