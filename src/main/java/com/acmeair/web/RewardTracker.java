package com.acmeair.web;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.acmeair.rm.MilesUpdate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RewardTracker {

  @Inject @Channel("rewards")
  private Emitter<MilesUpdate> emitter;

  private static AtomicLong rewardRequestsSent = new AtomicLong();
    
  public void updateRewardMiles(String userId, int miles) throws InterruptedException  {
    MilesUpdate milesUpdate = new MilesUpdate(userId, miles);
    emitter.send(milesUpdate);
    rewardRequestsSent.incrementAndGet();
  }

  public Long getRewardRequestsSent() {
    return rewardRequestsSent.get();
  }
   
}
