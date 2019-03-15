/*
 * Title:        EdgeCloudSim - Physical Distance network model implementation
 * 
 * Description:  Network model that only considers the physical distance between the communicating devices to calculate the network latency.
 * 
 * License:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2019, Tobias Baumann
 */

package edu.boun.edgecloudsim.network;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;


/**
 * Network model that only considers the physical distance between the communicating devices to calculate the network latency.
 */
public class PhysicalDistanceModel extends NetworkModel {
	final double speedOfLight = 299792458; // m/s
	final double wirelessSpeed = speedOfLight;
	final double ethernetSpeed = speedOfLight * 0.77; // TODO: make actually use of it and do not rely on config file values

	public PhysicalDistanceModel(int _numberOfMobileDevices, String _simScenario) {
		super(_numberOfMobileDevices, _simScenario);
	}

	@Override
	public void initialize() {
		// nothing to initialize
	}
	
	/**
	 * Calculate the physical latency between two given devices.
	 * 
	 * @param lowerDeviceId ID of the mobile device (or of an edge device if upperDeviceId is an edge orchestrator)
	 * @param upperDeviceId ID of the an edge device, an edge orchestrator or the cloud
	 * @return Latency in seconds
	 */
	private double physicalLatency(int lowerDeviceId, int upperDeviceId) {
		
		// communication between an edge device and the edge orchestrator 
		if(lowerDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID && upperDeviceId == SimSettings.EDGE_ORCHESTRATOR_ID) {
			return SimSettings.getInstance().getInternalLanDelay();
		}
		
		if(lowerDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID || lowerDeviceId == SimSettings.EDGE_ORCHESTRATOR_ID || lowerDeviceId == SimSettings.CLOUD_DATACENTER_ID) {
			SimLogger.printLine("ERROR (PhysicalDistanceModel.physicalLatency): lowerDeviceId " + lowerDeviceId + " is not a mobile device.");
			System.exit(0);
		}
		
		// communication between a mobile device and an edge device
		if(upperDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID) {
			return getWlanDelay(lowerDeviceId, upperDeviceId);
		}
		
		// communication between a mobile device and the edge orchestrator
		if(upperDeviceId == SimSettings.EDGE_ORCHESTRATOR_ID) {
			return getWlanDelay(lowerDeviceId, upperDeviceId) + SimSettings.getInstance().getInternalLanDelay();
		}
		
		// communication between a mobile device and the cloud
		if(upperDeviceId == SimSettings.CLOUD_DATACENTER_ID) {
			return SimSettings.getInstance().getWanPropogationDelay();
		}
		
		SimLogger.printLine("ERROR (PhysicalDistanceModel.physicalLatency): upperDeviceId " + upperDeviceId + " is a mobile device or an unknown device.");
		System.exit(0);
		
		return 0; // This is just to keep my IDE silent ;)
	}
	
	private double getWlanDelay(int mobileDeviceId, int accessPointId) {
		Location mobileDeviceLocation = SimManager.getInstance().getMobilityModel().getLocation(mobileDeviceId, CloudSim.clock());
		Location accessPointLocation = SimManager.getInstance().getMobilityModel().getLocation(accessPointId, CloudSim.clock());
		
		/* TODO: Remove code in comments if call to SimUtils below is fast enough.
		int x_device = mobileDeviceLocation.getXPos();
		int y_device = mobileDeviceLocation.getYPos();
		int x_ap = accessPointLocation.getXPos();
		int y_ap = accessPointLocation.getYPos();
		
		double distance =  Math.sqrt(Math.pow(x_ap - x_device, 2) + Math.pow(y_ap - y_device, 2));*/
		
		double distance = SimUtils.calculateDistance(mobileDeviceLocation, accessPointLocation);
		double latency = distance / wirelessSpeed; 
		
		return latency;
	}

	/**
	* Source device must always be a mobile device.
	*/
	@Override
	public double getUploadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		return physicalLatency(sourceDeviceId, destDeviceId);
	}

	/**
	* Destination device must always be a mobile device.
	*/
	@Override
	public double getDownloadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		return physicalLatency(destDeviceId, sourceDeviceId);
	}

	@Override
	public void uploadStarted(Location accessPointLocation, int destDeviceId) {
		// do nothing
		
	}

	@Override
	public void uploadFinished(Location accessPointLocation, int destDeviceId) {
		// do nothing
		
	}

	@Override
	public void downloadStarted(Location accessPointLocation, int sourceDeviceId) {
		// do nothing
		
	}

	@Override
	public void downloadFinished(Location accessPointLocation, int sourceDeviceId) {
		// do nothing
		
	}
}
