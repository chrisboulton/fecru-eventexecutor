package com.bigcommerce.fecru.plugins.eventexecutor;

import com.atlassian.crucible.spi.data.*;
import com.atlassian.crucible.spi.PermId;
import com.atlassian.crucible.spi.services.*;
import com.atlassian.event.Event;
import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.fisheye.event.CommitEvent;
import com.atlassian.fisheye.spi.data.ChangesetDataFE;
import com.atlassian.fisheye.spi.services.RevisionDataService;
import com.atlassian.fisheye.spi.data.FileRevisionKeyData;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class CommitListener implements EventListener {
	private final Logger logger = Logger.getLogger("fisheye.app");
	private final ConfigurationManager config;
	private final RevisionDataService revisionService;
	private final ProjectService projectService;
	private final ImpersonationService impersonator;
	
	public CommitListener(ConfigurationManager config,
		RevisionDataService revisionService,
		ProjectService projectService,
		ImpersonationService impersonator) {
		this.config = config;
		this.revisionService = revisionService;
		this.projectService = projectService;
		this.impersonator = impersonator;
	}
	
	public Class[] getHandledEventClasses() {
		return new Class[] { CommitEvent.class };
	}
	
	public void handleEvent(Event event) {
		final CommitEvent commit = (CommitEvent) event;
		
		final String command = config.getEventCommand("commit");
		if (command == null) {
			return;
		}

		try {
			// Run as the configured run user to ensure we have full access to all projects
			impersonator.doAsUser(null, config.loadRunAsUser(),
				new Operation<Void, ServerException>() {
					public Void perform() throws ServerException {

						final ChangesetDataFE cs = revisionService.getChangeset(
							commit.getRepositoryName(), commit.getChangeSetId());
		
		
						final List<FileRevisionKeyData> files = cs.getFileRevisions();
						String fileList = "";
						for (FileRevisionKeyData file : files) {
							fileList += file.getPath() + "\n";
						}
		
						final List<ProjectData> allProjects = projectService.getAllProjects();
						String projectList = "";
						for (ProjectData project : allProjects) {
							if (commit.getRepositoryName().equals(project.getDefaultRepositoryName())) {
								projectList += project.getKey() + " ";
							}
						}
		
						String[] env = {
							"FECRU_REPOSITORY=" + commit.getRepositoryName(),
							"FECRU_REVISION=" + commit.getChangeSetId(),
							"FECRU_AUTHOR=" + cs.getAuthor(),
							"FECRU_BRANCH=" + cs.getBranch(),
							"FECRU_FILES=" + fileList,
							"FECRU_PROJECTS=" + projectList,
						};
		
						try {
							logger.info(String.format("Exec %s", command));
							Process p = Runtime.getRuntime().exec(command, env);
						}
						catch(IOException e) {
							final String error = String.format("Unable to execute commitHook %s: %s",
								command, e.getMessage());
							logger.error(error);
						}
						
						return null;
					}
				}
			);
		}
		catch (Exception e) {
			final String error = String.format("unable to execute commutHook %s: %s",
				command, e.getMessage());
			logger.error(error);
		}
	}
}