/*
 * Title:        EdgeCloudSim - Scenario Factory
 * 
 * Description:  Sample scenario factory providing the default
 *               instances of required abstract classes
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.RWPMobilityApp;

import org.apache.commons.math3.distribution.NormalDistribution;

import edu.boun.edgecloudsim.cloud_server.CloudServerManager;
import edu.boun.edgecloudsim.cloud_server.DefaultCloudServerManager;
import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.edge_orchestrator.BasicEdgeOrchestrator;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.DefaultEdgeServerManager;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.edge_client.DefaultMobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.DefaultMobileServerManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileServerManager;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.mobility.RWPMobility;
import edu.boun.edgecloudsim.task_generator.IdleActiveLoadGenerator;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.network.MM1Queue;
import edu.boun.edgecloudsim.network.NetworkModel;

public class SampleScenarioFactory implements ScenarioFactory {
	private int numOfMobileDevice;
	private double simulationTime;
	private String orchestratorPolicy;
	private String simScenario;
	private int areaXSize;
	private int areaYSize;
	
	SampleScenarioFactory(int _numOfMobileDevice,
			double _simulationTime,
			String _orchestratorPolicy,
			String _simScenario,
			int _areaXSize,
			int _areaYSize){
		numOfMobileDevice = _numOfMobileDevice;
		simulationTime = _simulationTime;
		orchestratorPolicy = _orchestratorPolicy;
		simScenario = _simScenario;
		areaXSize = _areaXSize;
		areaYSize = _areaYSize;
	}
	
	@Override
	public LoadGeneratorModel getLoadGeneratorModel() {
		return new IdleActiveLoadGenerator(numOfMobileDevice, simulationTime, simScenario);
	}

	@Override
	public EdgeOrchestrator getEdgeOrchestrator() {
		return new BasicEdgeOrchestrator(orchestratorPolicy, simScenario);
	}

	@Override
	public MobilityModel getMobilityModel() {
		NormalDistribution velocityDistr = new NormalDistribution(1, 0.5);
		NormalDistribution pauseTimeDistr = new NormalDistribution(8, 2);
		return new RWPMobility(numOfMobileDevice, simulationTime, areaXSize, areaYSize, velocityDistr, pauseTimeDistr);
	}

	@Override
	public NetworkModel getNetworkModel() {
		return new MM1Queue(numOfMobileDevice, simScenario);
	}

	@Override
	public EdgeServerManager getEdgeServerManager() {
		return new DefaultEdgeServerManager();
	}

	@Override
	public CloudServerManager getCloudServerManager() {
		return new DefaultCloudServerManager();
	}
	
	@Override
	public MobileDeviceManager getMobileDeviceManager() throws Exception {
		return new DefaultMobileDeviceManager();
	}

	@Override
	public MobileServerManager getMobileServerManager() {
		return new DefaultMobileServerManager();
	}
}
