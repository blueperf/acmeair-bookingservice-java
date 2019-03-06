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
package com.acmeair.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.acmeair.client.CustomerClient;
import com.acmeair.client.FlightClient;

@ApplicationScoped
public class RewardTracker {

  @Inject 
  ConfigPropertyHelper configProperties;
  
  @Inject
  @RestClient
  private CustomerClient customerClient;

  @Inject
  @RestClient
  private FlightClient flightClient;
    
  // TODO: For now, use the Fault Tolerance to make this done async.
  @Asynchronous
  public CompletionStage<Long> updateRewardMiles(String userid, String flightSegId, boolean add) throws InterruptedException  {
            
    if (!configProperties.trackRewardMiles()) {
      return CompletableFuture.completedFuture(new Long (0));
    }
   
    Long miles = flightClient.getRewardMiles(flightSegId).getMiles();
       
    if (!add ) {
      miles = miles * -1;
    }

    Long totalMiles;
    
    if (configProperties.secureServiceCalls()) {
      totalMiles = customerClient.updateCustomerTotalMilesWithAuthorization(userid,miles).getMiles();
    }
    else { 
      totalMiles = customerClient.updateCustomerTotalMilesWithoutAuthorization(userid,miles).getMiles();
    }
        
    return CompletableFuture.completedFuture(totalMiles);
  }    

}
