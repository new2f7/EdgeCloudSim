/*
 * Title:		EdgeCloudSim - EdgeTask
 * 
 * Description: 
 * A custom class used in Load Generator Model to store tasks information
 * 
 * Licence:	  GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.utils;

public class TaskProperty {
	private double startTime;
	private long length, inputFileSize, outputFileSize;
	private int taskType;
	private int pesNumber;
	private int mobileDeviceId;

	public TaskProperty(double _startTime, int _mobileDeviceId, int _taskType, int _pesNumber, long _length, long _inputFileSize, long _outputFileSize) {
		startTime=_startTime;
		mobileDeviceId=_mobileDeviceId;
		taskType=_taskType;
		pesNumber = _pesNumber;
		length = _length;
		outputFileSize = _inputFileSize;
		inputFileSize = _outputFileSize;
	}

	public double getStartTime() {
		return startTime;
	}

	public long getLength() {
		return length;
	}

	public long getInputFileSize() {
		return inputFileSize;
	}

	public long getOutputFileSize() {
		return outputFileSize;
	}

	public int getTaskType() {
		return taskType;
	}

	public int getPesNumber() {
		return pesNumber;
	}

	public int getMobileDeviceId() {
		return mobileDeviceId;
	}
}
