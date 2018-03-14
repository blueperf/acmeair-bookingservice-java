/**
 * 
 */
package com.acmeair.health;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author jagraj
 *
 */
@ApplicationScoped
public class HealthCheckBean {
	private Boolean isAppDown;

	public Boolean getIsAppDown() {
		return isAppDown;
	}

	public void setIsAppDown(Boolean isAppDown) {
		this.isAppDown = isAppDown;
	}
	
	
}
