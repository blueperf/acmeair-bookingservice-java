package com.acmeair.web;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RewardTracker {

  @Inject @Channel("rewards")
  private Emitter<String> emitter;
    
  public void updateRewardMiles(String userId, int miles) throws InterruptedException  {
    emitter.send(userId + ":" + miles);
  }
   
}
