/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package it.com.acmeair.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.runtime.Network;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.runners.MethodSorters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookingServiceTests {

  private static String BASE_URL;
  private static String BASE_URL_WITH_CONTEXT_ROOT;

  private static final String LOAD_ENDPOINT = "/loader/load";
  private static final String LOAD_RESPONSE =  "Cleared bookings in ";

  private static final String PING_ENDPOINT = "/";
  private static final String PONG_RESPONSE = "OK";
  
  private static final String HEALTH_ENDPOINT="/health";
  private static final String HEALTH_RESPONSE="],\"status\":\"UP\"}";

  private static final String OPENAPI_ENDPOINT="/openapi";


  private static final String GET_BOOKINGS_ENDPOINT="/byuser/uid0@email.com";
  private static final String GET_BOOKINGS_RESPONSE="";

  private static final String BOOKFLIGHT_ENDPOINT = "/bookflights";
  private static final String CANCELFLIGHT_ENDPOINT = "/cancelbooking";

  private static MongodExecutable mongodExe;
  private static MongodProcess mongod;

  private Client client;
  private static String date;

  @BeforeClass
  public static void oneTimeSetup() throws UnknownHostException, IOException {

    String port = System.getProperty("liberty.test.port");
    
    BASE_URL = "http://localhost:" + port;
    BASE_URL_WITH_CONTEXT_ROOT = BASE_URL + "/booking";

    IFeatureAwareVersion version = de.flapdoodle.embed.mongo.distribution.Versions.withFeatures(new GenericVersion("4.0.0"), 
Version.Main.PRODUCTION.getFeatures());

    MongodStarter starter = MongodStarter.getDefaultInstance();
        String bindIp = "localhost";
        int mongoPort = 27017;
        IMongodConfig mongodConfig = new MongodConfigBuilder()
        .version(version)
        .net(new Net(bindIp, mongoPort, Network.localhostIsIPv6()))
        .build();
        mongodExe = starter.prepare(mongodConfig);
        mongod = mongodExe.start();     
        
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE MMM dd");

    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime nowChanged = now.toLocalDate().atStartOfDay(now.getZone());
    date = nowChanged.format(dtf) + " 00:00:00 UTC 2020";
  }

  @Before
  public void setup() {
    client = ClientBuilder.newClient();
    client.register(JsrJsonpProvider.class);
  }

  @After
  public void teardown() {
    client.close();
  }

  @AfterClass
  public static void tearDownMongo() {
    if (mongod != null) {
      mongod.stop();
      mongodExe.stop();
    } 
  }

  @Test   
  public void test1_Load() {
    String url = BASE_URL_WITH_CONTEXT_ROOT + LOAD_ENDPOINT; 
    doTest(url,Status.OK,LOAD_RESPONSE);
  }

  @Test
  public void test2_Ping() {
    String url = BASE_URL_WITH_CONTEXT_ROOT + PING_ENDPOINT; 
    doTest(url,Status.OK,PONG_RESPONSE);
  }

  @Test   
  public void test3_Health() {
    String url = BASE_URL + HEALTH_ENDPOINT; 
    doTest(url,Status.OK,HEALTH_RESPONSE);
  }

  //@Test   
  // openapi does not work in loose config mode
  public void test3_OpenAPI() {
    String url = BASE_URL + OPENAPI_ENDPOINT; 
    doTest(url,Status.OK,LOAD_RESPONSE);
  }
  
  @Test
  public void test04_bookFlight() throws InterruptedException, ParseException {
    String url = BASE_URL_WITH_CONTEXT_ROOT + BOOKFLIGHT_ENDPOINT; 
    
    WebTarget target = client.target(url);

    Form form = new Form();
    form.param("userid","uid0@email.com");
    form.param("fromAirport", "CDG");
    form.param("toAirport", "LHR");
    form.param("oneWayFlight", "true");
    form.param("fromDate", date);
    form.param("returnDate", date);
     
    Response response = target.request().post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE),Response.class);

    Thread.sleep(20);
    assertEquals("Incorrect response code from " + url, 
      Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
   
    response.close();
  }

  @Test
  public void test5_GetBookings() {
    String url = BASE_URL_WITH_CONTEXT_ROOT + GET_BOOKINGS_ENDPOINT; 
    doTest(url,Status.UNAUTHORIZED,GET_BOOKINGS_RESPONSE);
  }
     
  private void doTest(String url, Status status, String expectedResponse) {
    WebTarget target = client.target(url);
    Response response = target.request().get();

    assertEquals("Incorrect response code from " + url, 
        status.getStatusCode(), response.getStatus());

    if (expectedResponse != null) {
      String result = response.readEntity(String.class);
      assertThat(result, containsString(expectedResponse));
    }
    
    response.close();
  }

  @Test
  public void test06_cancelBooking() throws InterruptedException, ParseException {
    String url = BASE_URL_WITH_CONTEXT_ROOT + CANCELFLIGHT_ENDPOINT; 
    
    WebTarget target = client.target(url);

    Form form = new Form();
    form.param("userid","uid0@email.com");
    form.param("number","bogus");
     
    Response response = target.request().post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE),Response.class);

    Thread.sleep(20);
    assertEquals("Incorrect response code from " + url, 
      Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
       
    
    response.close();
  }
      
}
