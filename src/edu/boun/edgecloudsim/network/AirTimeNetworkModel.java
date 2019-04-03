package edu.boun.edgecloudsim.network;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

public class AirTimeNetworkModel extends NetworkModel {
	private enum AIRTIME_STATUS {FREE, BLOCKED}
	private ArrayList<TreeMap<Double, AIRTIME_STATUS>> treeMapArray;

	public AirTimeNetworkModel(int _numberOfMobileDevices, String _simScenario) {
		super(_numberOfMobileDevices, _simScenario);
	}

	@Override
	public void initialize() {
		int numberOfEdgeDevices = SimSettings.getInstance().getNumOfEdgeDatacenters();
		treeMapArray = new ArrayList<TreeMap<Double, AIRTIME_STATUS>>();

		for(int i=0; i<numberOfEdgeDevices; i++) {
			// create a TreeMap for each edge device to keep track of the used airtime
			treeMapArray.add(i, new TreeMap<Double, AIRTIME_STATUS>());
			// mark the airtime as free from the beginning of the simulation
			treeMapArray.get(i).put(0.0, AIRTIME_STATUS.FREE);
			// put marker at the end to avoid null pointers
			treeMapArray.get(i).put(Double.MAX_VALUE, AIRTIME_STATUS.BLOCKED);
		}

	}

	@Override
	public double getUploadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		if(destDeviceId != SimSettings.CLOUD_DATACENTER_ID) {
			SimLogger.printLine("ERROR: destDeviceId is not the cloud!");
			System.exit(0);
		}
		long bytesToTransmit = task.getCloudletFileSize(); // InputFileSize
		return findAirTimeSlot(sourceDeviceId, bytesToTransmit);
	}

	@Override
	public double getDownloadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		if(sourceDeviceId != SimSettings.CLOUD_DATACENTER_ID) {
			SimLogger.printLine("ERROR: sourceDeviceId is not the cloud!");
			System.exit(0);
		}
		long bytesToTransmit = task.getCloudletOutputSize(); // OutputFileSize
		return findAirTimeSlot(destDeviceId, bytesToTransmit);
	}

	private double findAirTimeSlot(int sourceDeviceId, long bytesToTransmit) {
		if(bytesToTransmit <= 0) {
			SimLogger.printLine("ERROR: Nothing to transmit!");
			System.exit(0);
		}
		
		double bandwidthKbps = SimSettings.getInstance().getWlanBandwidth();
		double bytesPerSecond = bandwidthKbps * 1000 / 8;
		double transmissionTime = bytesToTransmit / bytesPerSecond;
		
		if(transmissionTime <= 0) {
			SimLogger.printLine("ERROR: Transmission time less or equal zero!");
			System.exit(0);
		}
		
		Location accessPointLocation = SimManager.getInstance().getMobilityModel().getLocation(sourceDeviceId, CloudSim.clock());
		int wlan_id = accessPointLocation.getServingWlanId();
		TreeMap<Double, AIRTIME_STATUS> airtime = treeMapArray.get(wlan_id);
		
		/* The AirTimeNetworkModel is used together with the StreamLoadGeneratorModel.
		 * There are a bunch of new tasks every time unit. The transmission needs to
		 * be scheduled before the expected new tasks. The variable 'maximumDelay'
		 * marks the interval in which new tasks are expected.
		 */
		double maximumDelay = SimSettings.getInstance().getTimeResolution();
		double threshold = CloudSim.clock() + maximumDelay;
		double timeToCheck = CloudSim.clock();
		
		while(timeToCheck < threshold) {
			Entry<Double, AIRTIME_STATUS> StartFloorEntry = airtime.floorEntry(timeToCheck);
			Entry<Double, AIRTIME_STATUS> StartCeilingEntry = airtime.higherEntry(timeToCheck);
			Entry<Double, AIRTIME_STATUS> EndFloorEntry = airtime.lowerEntry(timeToCheck+transmissionTime);
			Entry<Double, AIRTIME_STATUS> EndCeilingEntry = airtime.ceilingEntry(timeToCheck+transmissionTime);
			
			if(StartFloorEntry == null || EndFloorEntry == null) {
				SimLogger.printLine("ERROR: No floor Entry!");
				System.exit(0);
			}
			
			if(StartFloorEntry.equals(EndFloorEntry) && StartCeilingEntry.equals(EndCeilingEntry) && StartFloorEntry.getValue() == AIRTIME_STATUS.FREE) {
				// There are no other entries between start and end. The start marker indicates free. The transmission of the task can be scheduled.
				// Place a marker at the beginning of the transmission to mark the airtime as blocked.
				airtime.put(timeToCheck, AIRTIME_STATUS.BLOCKED);
				// If there is no key at the end of the transmission, place a free marker.
				if(!airtime.containsKey(timeToCheck+transmissionTime))
					airtime.put(timeToCheck+transmissionTime, AIRTIME_STATUS.FREE);
				// The total delay consists of the time to wait until airtime is available and the transmission delay.
				return timeToCheck - CloudSim.clock() + transmissionTime;
			}
			
			timeToCheck = StartCeilingEntry.getKey();
			
		}
		
		// It is not possible to schedule the transmission. Return a delay of 0 which indicates transmission failure.
		return 0;
	}

	@Override
	public void uploadStarted(Location accessPointLocation, int destDeviceId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void uploadFinished(Location accessPointLocation, int destDeviceId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void downloadStarted(Location accessPointLocation, int sourceDeviceId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void downloadFinished(Location accessPointLocation, int sourceDeviceId) {
		// TODO Auto-generated method stub
	}
}
