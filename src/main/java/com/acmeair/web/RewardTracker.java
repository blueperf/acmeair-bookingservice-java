package com.acmeair.web;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.acmeair.rm.MilesUpdate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RewardTracker {

  @Inject @Channel("rewards")
  private Emitter<MilesUpdate> emitter;
    
  public void updateRewardMiles(String userId, int miles) throws InterruptedException  {
    MilesUpdate milesUpdate = new MilesUpdate(userId, miles);
    emitter.send(milesUpdate);
  }
   
}
