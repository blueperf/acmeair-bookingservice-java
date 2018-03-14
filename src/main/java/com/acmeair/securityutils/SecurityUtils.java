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

package com.acmeair.securityutils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SecurityUtils {
    
  private static final Logger logger =  Logger.getLogger(SecurityUtils.class.getName());
    
  // TODO: Hardcode for now
  private static final String secretKey = "acmeairsecret128";
    
  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final String SHA_256 = "SHA-256";
  private static final String UTF8 = "UTF-8";

  private static Mac macCached = null;
  
  /* microprofile-1.1 */
  @Inject @ConfigProperty(name = "SECURE_USER_CALLS", defaultValue = "true") 
  private Boolean secureUserCalls;
  
  /* microprofile-1.1 */
  @Inject @ConfigProperty(name = "SECURE_SERVICE_CALLS", defaultValue = "true") 
  protected Boolean secureServiceCalls;
  
  @PostConstruct
  private void initialize() {
    
    System.out.println("SECURE_USER_CALLS: " + secureUserCalls);
    System.out.println("SECURE_SERVICE_CALLS: " + secureServiceCalls);
    
    // Cache MAC to avoid cost of getInstance/init.
    try {
      macCached = Mac.getInstance(HMAC_ALGORITHM);
      macCached.init(new SecretKeySpec(secretKey.getBytes(UTF8), HMAC_ALGORITHM));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
       
  /**
   *  Validate simple JWT. 
   */
  public boolean validateJwt(String customerid, String jwtToken) {
        
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("validate : customerid " + customerid);
    }
     
    try {
      JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build(); //Cache?
            
      DecodedJWT jwt = verifier.verify(jwtToken);
      return jwt.getSubject().equals(customerid);
            
    } catch (Exception exception) {
      exception.printStackTrace();
    }
        
    return false;
  }
    
  public boolean secureUserCalls() {
    return secureUserCalls;
  }
  
  public boolean secureServiceCalls() {
    return secureServiceCalls;
  }
  
  /**
   * Build Hash of data.
   */
  public String buildHash(String data)
          throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance(SHA_256);
    md.update(data.getBytes(UTF8));
    byte[] digest = md.digest();
    return Base64.getEncoder().encodeToString(digest);
  }
   
  /**
   * Build signature of all this junk.
   */
  public String buildHmac(String method, String baseUri, String userId, String dateString, 
          String sigBody)
            throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        
    List<String> toHash = new ArrayList<String>();
    toHash.add(method);
    toHash.add(baseUri);
    toHash.add(userId);
    toHash.add(dateString);
    toHash.add(sigBody);   
  
    Mac mac = null;
    try {
      mac = (Mac) macCached.clone();
    } catch (CloneNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    for (String s: toHash) {
      mac.update(s.getBytes(UTF8));
    }
        
    return Base64.getEncoder().encodeToString(mac.doFinal());
  }  
}
