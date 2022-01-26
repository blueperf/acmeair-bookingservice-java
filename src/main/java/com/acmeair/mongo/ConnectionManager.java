/*******************************************************************************
 * Copyright (c) 2017 IBM Corp.
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

package com.acmeair.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.io.StringReader;
import java.util.Optional;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;

import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConnectionManager {

  private static final JsonReaderFactory factory = Json.createReaderFactory(null);
  private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());

  protected MongoClient mongoClient;
  protected MongoClientURI mongoUri = null;
  protected MongoDatabase db; 

  @Inject 
  @ConfigProperties
  MongoProperties mongoProps; 

  @Inject 
  @ConfigProperty(name = "VCAP_SERVICES") 
  Optional<String> vcapJsonString;

  @PostConstruct
  private void initialize() {     

    ServerAddress dbAddress = null;
    MongoClientOptions.Builder options = new MongoClientOptions.Builder();

    if (mongoProps.connectionsPerHost.isPresent()) {
      options.connectionsPerHost(mongoProps.connectionsPerHost.get());
    }
    if (mongoProps.minConnectionsPerHost.isPresent()) {
      options.minConnectionsPerHost(mongoProps.minConnectionsPerHost.get());
    }
    if (mongoProps.maxWaitTime.isPresent()) {
      options.maxWaitTime(mongoProps.maxWaitTime.get());
    }
    if (mongoProps.connectTimeOut.isPresent()) {
      options.connectTimeout(mongoProps.connectTimeOut.get());
    }
    if (mongoProps.socketTimeOut.isPresent()) {
      options.socketTimeout(mongoProps.socketTimeOut.get());
    }
    if (mongoProps.sslEnabled.isPresent()) {
      options.sslEnabled(mongoProps.sslEnabled.get());
    }
    if (mongoProps.threadsAllowedToBlockForConnectionMultiplier.isPresent()) {
      options.threadsAllowedToBlockForConnectionMultiplier(
          mongoProps.threadsAllowedToBlockForConnectionMultiplier.get());
    }
    if (mongoProps.maxConnectionIdleTime.isPresent()) {
      options.maxConnectionIdleTime(mongoProps.maxConnectionIdleTime.get());
    }

    MongoClientOptions builtOptions = options.build();

    try {
      // Check if VCAP_SERVICES exist, and if it does, look up the url from the
      // credentials.
      if (vcapJsonString.isPresent()) {
        logger.info("Reading VCAP_SERVICES");

        JsonReader jsonReader = factory.createReader(new StringReader(vcapJsonString.get()));
        JsonObject vcapServices = jsonReader.readObject();
        jsonReader.close();

        JsonArray mongoServiceArray = null;
        for (Object key : vcapServices.keySet()) {
          if (key.toString().startsWith("mongo")) {
            mongoServiceArray = (JsonArray) vcapServices.get(key);
            logger.info("Service Type : MongoLAB - " + key.toString());
            break;
          }
          if (key.toString().startsWith("user-provided")) {
            mongoServiceArray = (JsonArray) vcapServices.get(key);
            logger.info("Service Type : MongoDB by Compost - " + key.toString());
            break;
          }
        }

        if (mongoServiceArray == null) {
          logger.info(
              "VCAP_SERVICES existed, but a MongoLAB or MongoDB by COMPOST service was "
                  + "not definied. Trying DB resource");
          // VCAP_SERVICES don't exist, so use the DB resource
          dbAddress = new ServerAddress(mongoProps.host, mongoProps.port);

          // If username & password exists, connect DB with username & password
          if ((!mongoProps.username.isPresent()) || (!mongoProps.password.isPresent())) {
            mongoClient = new MongoClient(dbAddress, builtOptions);
          } else {
            MongoCredential credential = MongoCredential
                .createCredential(mongoProps.username.get(), mongoProps.database, 
                    mongoProps.password.get().toCharArray());
            mongoClient = new MongoClient(dbAddress, credential, builtOptions);
          }
        } else {
          JsonObject mongoService = (JsonObject) mongoServiceArray.get(0);
          JsonObject credentials = (JsonObject) mongoService.get("credentials");
          String url = (String) credentials.getString("url");
          logger.fine("service url = " + url);
          mongoUri = new MongoClientURI(url, options);
          mongoClient = new MongoClient(mongoUri);
        }
      } else {

        // VCAP_SERVICES don't exist, so use the DB resource
        dbAddress = new ServerAddress(mongoProps.host, mongoProps.port);

        // If username & password exists, connect DB with username & password
        if ((!mongoProps.username.isPresent()) || (!mongoProps.password.isPresent())) {
          mongoClient = new MongoClient(dbAddress, builtOptions);
        } else {
          MongoCredential credential = MongoCredential
              .createCredential(mongoProps.username.get(), mongoProps.database, 
                  mongoProps.password.get().toCharArray());
          mongoClient = new MongoClient(dbAddress, credential, builtOptions);
        }
      }

      
      if (mongoUri == null) {
        db = mongoClient.getDatabase(mongoProps.database);
        logger.info("#### Mongo DB Server " + mongoProps.host + " ####");
        logger.info("#### Mongo DB Port " + mongoProps.port + " ####");
        logger.info("#### Mongo DB is created with DB name " + mongoProps.database + " ####");
      } else {
        db = mongoClient.getDatabase(mongoUri.getDatabase());
        logger.info("#### Mongo URI is" + mongoUri.getURI() + " ####");
      }
      logger.info("#### MongoClient Options ####");
      logger.info("maxConnectionsPerHost : " + builtOptions.getConnectionsPerHost());
      logger.info("minConnectionsPerHost : " + builtOptions.getMinConnectionsPerHost());
      logger.info("maxWaitTime : " + builtOptions.getMaxWaitTime());
      logger.info("connectTimeout : " + builtOptions.getConnectTimeout());
      logger.info("socketTimeout : " + builtOptions.getSocketTimeout());
      logger.info("sslEnabled : " + builtOptions.isSslEnabled());
      logger.info("threadsAllowedToBlockForConnectionMultiplier : "
          + builtOptions.getThreadsAllowedToBlockForConnectionMultiplier());
      logger.info("Complete List : " + builtOptions.toString());

    } catch (Exception e) {
      logger.severe("Caught Exception : " + e.getMessage());
    }

  }

  public MongoDatabase getDb() {
    return db;
  }
}
