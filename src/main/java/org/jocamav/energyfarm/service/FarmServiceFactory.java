package org.jocamav.energyfarm.service;

public interface FarmServiceFactory {
	FarmService getFarmService(String type);
}
