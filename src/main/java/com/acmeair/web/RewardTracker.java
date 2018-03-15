/*******************************************************************************
* Copyright (c) 2018 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.acmeair.web;

import com.acmeair.faultTolerance.CustomerClientConnection;
import com.acmeair.faultTolerance.FlightClientConnection;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Asynchronous;

@ApplicationScoped
public class RewardTracker {


  @Inject
  private CustomerClientConnection customerClientConection;
  
  @Inject
  private FlightClientConnection flightClientConnection;
  
  /* microprofile-1.1 */
  @Inject 
  @ConfigProperty(name = "TRACK_REWARD_MILES", defaultValue = "true") 
  private Boolean trackRewardMiles;

  @PostConstruct
  private void initialize() {
    System.out.println("TRACK_REWARD_MILES: " + trackRewardMiles);
  }
    
  public boolean trackRewardMiles() {
    return trackRewardMiles;
  }
  
  /**
   * Update rewards.
   */
  //Make this asynchrnous so the client gets the booking confirmed message faster.
  //This can be done in the background
  @Asynchronous
  public Future<Long> updateRewardMiles(String userid, String flightSegId, boolean add) {
    
    if (trackRewardMiles) {
      
      Long miles = null;     
      try {
        miles = flightClientConnection.connect(userid, flightSegId, add);
      } catch (Exception e) {
        e.printStackTrace();
        return CompletableFuture.completedFuture(null);
      }    
      
      Long totalMiles = null;
      if (miles != null && !miles.equals(-1)) {
        try {
          totalMiles = customerClientConection.connect(userid, miles);
        } catch (Exception e) {
          e.printStackTrace();
          return CompletableFuture.completedFuture(null);
        }
      } else {
        System.out.println("FlightSevice Call Failed: Updating Reward Miles Failed for " 
            + userid + ", flightSegment " + flightSegId);
        return CompletableFuture.completedFuture(null);
      }  
      if (totalMiles != null && !totalMiles.equals(-1)) {
        return CompletableFuture.completedFuture(totalMiles);
      } else {
        System.out.println("CustomerSevice Call Failed: Updating Reward Miles Failed for " 
            + userid + ", flightSegment " + flightSegId);
        return CompletableFuture.completedFuture(null);
      }  
    }
    
    return CompletableFuture.completedFuture(null);
  }
}
