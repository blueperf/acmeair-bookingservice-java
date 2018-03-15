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

package com.acmeair.client.jaxrs;

import com.acmeair.securityutils.SecurityUtils;

import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class JaxRsClient {

  protected static final Boolean SECURE_SERVICE_CALLS = Boolean
      .valueOf((System.getenv("SECURE_SERVICE_CALLS") == null) ? "false" 
          : System.getenv("SECURE_SERVICE_CALLS"));

  @Inject
  private SecurityUtils secUtils;

  protected Builder createInvocationBuilder(WebTarget target, Form form, 
      String customerId, String path) {

    Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);

    if (SECURE_SERVICE_CALLS) {
      try {
        Date date = new Date();

        String body = "";
        MultivaluedMap<String, String> map = form.asMap();

        for (String key : map.keySet()) {
          body = body + key + "=" + map.getFirst(key) + "&";
        }
        body = body.substring(0, body.length() - 1);

        String sigBody = secUtils.buildHash(body);
        String signature = secUtils.buildHmac("POST", path, customerId, date.toString(), sigBody);

        builder.header("acmeair-id", customerId);
        builder.header("acmeair-date", date.toString());
        builder.header("acmeair-sig-body", sigBody);
        builder.header("acmeair-signature", signature);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return builder;
  }

}
