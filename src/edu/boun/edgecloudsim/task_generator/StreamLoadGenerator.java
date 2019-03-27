/*
 * Title:        EdgeCloudSim - Cloud Stream Load Generator implementation
 * 
 * Description:  The CloudStreamLoadGenerator implements a Stream of tasks
 * 
 * License:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2019 Tobias Baumann
 */

package edu.boun.edgecloudsim.task_generator;

import java.util.ArrayList;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.TaskProperty;
import edu.boun.edgecloudsim.utils.AppProperties;

public class StreamLoadGenerator extends LoadGeneratorModel{
	private double resolution;
	// Each mobile device runs exactly one task type. This array is used to save which task type a device is running.
	private int taskTypeOfDevices[];
	;

	public StreamLoadGenerator(int _numberOfMobileDevices, double _simulationTime, String _simScenario, double _resolution) {
		super(_numberOfMobileDevices, _simulationTime, _simScenario);
		resolution = _resolution;
	}

	@Override
	public void initializeModel() {
		taskList = new ArrayList<TaskProperty>();
		taskTypeOfDevices = new int[numberOfMobileDevices];
		
		final ArrayList<AppProperties> typeList = SimSettings.getInstance().getTaskLookUpTable();
		final int myTaskType = 0; // use 0 as default task for now
		final AppProperties myAppProperties = typeList.get(myTaskType);
		final int PEs = 1; // TODO: add to AppProperties
		final long downloadSize = myAppProperties.getDownloadSize();
		final long bytesPerTimeUnit = (int) Math.ceil(((double) myAppProperties.getBitsPerSecond()) * resolution / 8);
		final long MIPerTimeUnit = (long) Math.ceil(myAppProperties.getInstructionsPerFrame() * myAppProperties.getFps() * resolution / 1000000);

		for(int i=0; i<numberOfMobileDevices; i++) {
			taskTypeOfDevices[i] = myTaskType;
			for(double virtualTime=SimSettings.CLIENT_ACTIVITY_START_TIME; virtualTime<simulationTime; virtualTime+=resolution) {
				taskList.add(new TaskProperty(virtualTime, i, myTaskType, PEs, MIPerTimeUnit, bytesPerTimeUnit, downloadSize));
			}
		}
	}

	@Override
	public int getTaskTypeOfDevice(int deviceId) {
		return taskTypeOfDevices[deviceId];
	}

}
