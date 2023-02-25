/*******************************************************************************
 * Copyright (c) 2013 IBM Corp.
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


import com.acmeair.service.BookingService;

import java.io.StringReader;

import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

@Path("/")
public class BookingServiceRest {

  //@Inject
  //io.opentracing.Tracer configuredTracer;

  @Inject
  BookingService bs;

  @Inject
  private JsonWebToken jwt;

  @Inject 
  RewardTracker rewardTracker; 

  private static final JsonReaderFactory factory = Json.createReaderFactory(null);  

  /**
   * Book flights.
   */
  @POST
  @Consumes({ "application/x-www-form-urlencoded" })
  @Path("/bookflights")
  @Produces("text/plain")
  @SimplyTimed(name = "com.acmeair.web.BookingServiceRest.bookFlights",tags = "app=bookingservice-java")
  @RolesAllowed({"user"})
  public /* BookingInfo */ Response bookFlights(@FormParam("userid") String userid,
      @FormParam("toFlightId") String toFlightId, 
      @FormParam("toFlightSegId") String toFlightSegId,
      @FormParam("retFlightId") String retFlightId, 
      @FormParam("retFlightSegId") String retFlightSegId,
      @FormParam("oneWayFlight") boolean oneWay) {
    try {

      // make sure the user isn't trying to bookflights for someone else
      if (!userid.equals(jwt.getSubject())) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }

      String bookingIdTo = bs.bookFlight(userid, toFlightSegId, toFlightId);
      rewardTracker.updateRewardMiles(userid, toFlightSegId, true); 

      String bookingInfo = "";
      String bookingIdReturn = null;

      if (!oneWay) {
        bookingIdReturn = bs.bookFlight(userid, retFlightSegId, retFlightId);        
        rewardTracker.updateRewardMiles(userid, retFlightSegId, true); 

        bookingInfo = "{\"oneWay\":false,\"returnBookingId\":\"" 
            + bookingIdReturn + "\",\"departBookingId\":\""
            + bookingIdTo + "\"}";
      } else {
        bookingInfo = "{\"oneWay\":true,\"departBookingId\":\"" + bookingIdTo + "\"}";
      }
      return Response.ok(bookingInfo).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  /**
   * Get bookins for a customer.
   */
  @GET
  @Path("/byuser/{user}")
  @Produces("text/plain")
  @SimplyTimed(name = "com.acmeair.web.bookFlights.BookingServiceRest.getBookingsByUser",
  tags = "app=bookingservice-java")
  @RolesAllowed({"user"})
  public Response getBookingsByUser(@PathParam("user") String userid) {

    try {  
      // make sure the user isn't trying to bookflights for someone else
      if (!userid.equals(jwt.getSubject())) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }

      return Response.ok(bs.getBookingsByUser(userid).toString()).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Cancel bookings.
   */
  @POST
  @Consumes({ "application/x-www-form-urlencoded" })
  @Path("/cancelbooking")
  @Produces("text/plain")
  @SimplyTimed(name = "com.acmeair.web.bookFlights.BookingServiceRest.cancelBookingsByNumber",
  tags = "app=bookingservice-java")
  @RolesAllowed({"user"})
  public Response cancelBookingsByNumber(@FormParam("number") String number, 
      @FormParam("userid") String userid) {
    try {
      // make sure the user isn't trying to bookflights for someone else
      if (!userid.equals(jwt.getSubject())) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }   
      
      JsonObject booking;
      
      try {
        JsonReader jsonReader = factory.createReader(new StringReader(bs
            .getBooking(userid, number)));
        booking = jsonReader.readObject();
        jsonReader.close();
      
        bs.cancelBooking(userid, number);
      } catch (RuntimeException npe) {
        // Booking has already been deleted...
        return Response.ok("booking " + number + " deleted.").build();
      }
      
      rewardTracker.updateRewardMiles(userid, booking.getString("flightSegmentId"), false);

      return Response.ok("booking " + number + " deleted.").build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GET
  @PermitAll
  @Path("/status")
  public Response status() {
    return Response.ok("OK").build();
  }

  @GET
  @PermitAll
  @Path("/rewards/customerfailures")
  public Response customerFailures() {
    return Response.ok(rewardTracker.getCustomerFailures()).build();
  }

  @GET
  @PermitAll
  @Path("/rewards/flightfailures")
  public Response flightFailures() {
    return Response.ok(rewardTracker.getFlightFailures()).build();
  }

  @GET
  @PermitAll
  @Path("/rewards/customersuccesses")
  public Response customerSucceses() {
    return Response.ok(rewardTracker.getCustomerSuccesses()).build();
  }

  @GET
  @PermitAll
  @Path("/rewards/flightsuccesses")
  public Response flightSuccesseses() {
    return Response.ok(rewardTracker.getFlightSucesses()).build();
  }
}
