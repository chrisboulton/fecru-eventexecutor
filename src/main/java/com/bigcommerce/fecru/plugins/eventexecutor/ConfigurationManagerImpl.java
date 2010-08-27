package com.bigcommerce.fecru.plugins.eventexecutor;

import com.atlassian.plugin.util.Assertions;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class ConfigurationManagerImpl implements ConfigurationManager {

	private final String RUNAS_CFG = "com.bigcommerce.fecru.plugins.eventexecutor.runAsUser";
	private final String EVENT_CFG = "com.bigcommerce.fecru.plugins.eventexecutor.event.";

	private final PluginSettings store;

	public ConfigurationManagerImpl(PluginSettingsFactory settingsFactory) {
		this(settingsFactory.createGlobalSettings());
	}

	ConfigurationManagerImpl(PluginSettings store) {
		this.store = store;
	}

	public String loadRunAsUser() {
		final Object value = store.get(RUNAS_CFG);
		return value == null ? null : value.toString();
	}

	public void storeRunAsUser(String username) {
		store.put(RUNAS_CFG, username);
	}

	public String getEventCommand(String event) {
		final String configVar = EVENT_CFG + event;
		final Object value = store.get(configVar);
		return value == null ? null : value.toString();
	}

	public void storeEventCommand(String event, String value) {
		final String configVar = EVENT_CFG + event;
		store.put(configVar, value);
	}
}