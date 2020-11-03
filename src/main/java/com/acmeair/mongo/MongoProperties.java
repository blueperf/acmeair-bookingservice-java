package com.acmeair.mongo;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ConfigProperties()
@Dependent
public class MongoProperties {
  public MongoProperties() {}

  @Inject 
  @ConfigProperty(name = "MONGO_HOST") 
  public String host;
  
  @Inject 
  @ConfigProperty(name = "MONGO_PORT") 
  public Integer port;
  
  @Inject 
  @ConfigProperty(name = "MONGO_DBNAME") 
  public String dbName;
  
  @Inject 
  @ConfigProperty(name = "MONGO_USERNAME") 
  public Optional<String> username;
  
  @Inject 
  @ConfigProperty(name = "MONGO_PASSWORD") 
  public Optional<String> password;
    
  @Inject 
  @ConfigProperty(name = "MONGO_SSL_ENABLED") 
  public Optional<Boolean> sslEnabled;
  
  @Inject 
  @ConfigProperty(name = "MONGO_MIN_CONNECTIONS_PER_HOST") 
  public Optional<Integer> minConnectionsPerHost;
  
  @Inject 
  @ConfigProperty(name = "MONGO_CONNECTIONS_PER_HOST") 
  public Optional<Integer> connectionsPerHost;
  
  @Inject 
  @ConfigProperty(name = "MONGO_MAX_WAIT_TIME") 
  public Optional<Integer> maxWaitTime;
  
  @Inject 
  @ConfigProperty(name = "MONGO_CONNECT_TIME_OUT") 
  public Optional<Integer>connectTimeOut;
  
  @Inject 
  @ConfigProperty(name = "MONGO_SOCKET_TIME_OUT") 
  public Optional<Integer> socketTimeOut;
  
  @Inject 
  @ConfigProperty(name = "MONGO_THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER") 
  public Optional<Integer> threadsAllowedToBlockForConnectionMultiplier;
  
  @Inject 
  @ConfigProperty(name = "MONGO_MAX_CONNECTION_IDLE_TIME") 
  public Optional<Integer> maxConnectionIdleTime;
  
}
