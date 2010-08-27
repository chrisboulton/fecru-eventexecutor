package com.bigcommerce.fecru.plugins.eventexecutor;

public interface ConfigurationManager {
	String getEventCommand(String hook);
	void storeEventCommand(String hook, String value);

	String loadRunAsUser();
	void storeRunAsUser(String username);
}