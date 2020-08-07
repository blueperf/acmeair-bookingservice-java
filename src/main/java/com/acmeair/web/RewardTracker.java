package com.acmeair.web;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.acmeair.client.CustomerClient;
import com.acmeair.client.FlightClient;
import com.acmeair.service.BookingService;

@ApplicationScoped
public class RewardTracker {

  @Inject
  BookingService bs;

  @Inject
  @RestClient
  private CustomerClient customerClient;

  @Inject
  @RestClient
  private FlightClient flightClient;
    
  //TODO: For now, use the Fault Tolerance to make this done async.
  @Timeout(500) //throws a timeout exception if method does not return withing 400 ms
  @Asynchronous
  public CompletionStage<Long> updateRewardMiles(String userid, String flightSegId, boolean add) throws InterruptedException  {
    
    Long miles = flightClient.getRewardMiles(flightSegId).getMiles();

    if (!add ) {
      miles = miles * -1;
    }

    Long totalMiles = customerClient.updateCustomerTotalMiles(userid,miles).getMiles();

    return CompletableFuture.completedFuture(totalMiles);
  }
}
