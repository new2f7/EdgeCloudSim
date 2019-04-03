/*
 * Title:        EdgeCloudSim - Simulation Settings class
 * 
 * Description: 
 * SimSettings provides system wide simulation settings. It is a
 * singleton class and provides all necessary information to other modules.
 * If you need to use another simulation setting variable in your
 * config file, add related getter methods in this class.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.AppProperties;

public class SimSettings {
	private static SimSettings instance = null;
	private Document edgeDevicesDoc = null;

	public static final double CLIENT_ACTIVITY_START_TIME = 10;

	//enumarations for the VM types
	public static enum VM_TYPES { MOBILE_VM, EDGE_VM, CLOUD_VM }

	//enumarations for the VM types
	public static enum NETWORK_DELAY_TYPES { WLAN_DELAY, MAN_DELAY, WAN_DELAY }

	//predifined IDs for the components.
	public static final int CLOUD_DATACENTER_ID = 1000;
	public static final int MOBILE_DATACENTER_ID = 1001;
	public static final int EDGE_ORCHESTRATOR_ID = 1002;
	public static final int GENERIC_EDGE_DEVICE_ID = 1003;

	//delimiter for output file.
	public static final String DELIMITER = ";";

	private double SIMULATION_TIME; // unit in properties file: minutes
	private double WARM_UP_PERIOD; // unit in properties file: minutes
	private double INTERVAL_TO_GET_VM_LOAD_LOG; // unit in properties file: minutes
	private double INTERVAL_TO_GET_VM_LOCATION_LOG; // unit in properties file: minutes
	private boolean FILE_LOG_ENABLED;
	private boolean DEEP_FILE_LOG_ENABLED;

	private int MIN_NUM_OF_MOBILE_DEVICES;
	private int MAX_NUM_OF_MOBILE_DEVICES;
	private int MOBILE_DEVICE_COUNTER_SIZE;

	private int NUM_OF_EDGE_DATACENTERS;
	private int NUM_OF_EDGE_HOSTS;
	private int NUM_OF_EDGE_VMS;
	private int NUM_OF_PLACE_TYPES;

	private double WAN_PROPOGATION_DELAY; // unit in properties file: seconds
	private double LAN_INTERNAL_DELAY; // unit in properties file: seconds
	private int BANDWITH_WLAN; // unit in properties file: Mbps
	private int BANDWITH_WAN; // unit in properties file: Mbps
	private int BANDWITH_GSM; // unit in properties file: Mbps

	private int AREA_X_SIZE;
	private int AREA_Y_SIZE;
	private double TIME_RESOLUTION;
	private double VELOCITY_MEAN;
	private double VELOCITY_SD;
	private double PAUSE_TIME_MEAN;
	private double PAUSE_TIME_SD;

	private int NUM_OF_HOST_ON_CLOUD_DATACENTER;
	private int NUM_OF_VM_ON_CLOUD_HOST;
	private int CORE_FOR_CLOUD_VM;
	private int MIPS_FOR_CLOUD_VM; // MIPS
	private int RAM_FOR_CLOUD_VM; // MB
	private int STORAGE_FOR_CLOUD_VM; // Byte

	private int CORE_FOR_VM;
	private int MIPS_FOR_VM; // MIPS
	private int RAM_FOR_VM; // MB
	private int STORAGE_FOR_VM; // Byte

	private String[] SIMULATION_SCENARIOS;
	private String[] ORCHESTRATOR_POLICIES;

	// mean waiting time (minute) is stored for each place types
	private double[] mobilityLookUpTable;

	// stores values for each applications defined in applications.xml
	private ArrayList<AppProperties> taskLookUpTable = null;

	private SimSettings() {
		NUM_OF_PLACE_TYPES = 0;
	}

	public static SimSettings getInstance() {
		if(instance == null) {
			instance = new SimSettings();
		}
		return instance;
	}

	/**
	 * Reads configuration file and stores information to local variables
	 * @param propertiesFile
	 * @return
	 */
	public boolean initialize(String propertiesFile, String edgeDevicesFile, String applicationsFile) {
		boolean result = false;
		InputStream input = null;
		try {
			input = new FileInputStream(propertiesFile);

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			SIMULATION_TIME = 60 * Double.parseDouble(prop.getProperty("simulation_time")); // convert to seconds
			WARM_UP_PERIOD = 60 * Double.parseDouble(prop.getProperty("warm_up_period")); // convert to seconds
			INTERVAL_TO_GET_VM_LOAD_LOG = 60 * Double.parseDouble(prop.getProperty("vm_load_check_interval")); // convert to seconds
			INTERVAL_TO_GET_VM_LOCATION_LOG = 60 * Double.parseDouble(prop.getProperty("vm_location_check_interval")); // convert to seconds
			FILE_LOG_ENABLED = Boolean.parseBoolean(prop.getProperty("file_log_enabled"));
			DEEP_FILE_LOG_ENABLED = Boolean.parseBoolean(prop.getProperty("deep_file_log_enabled"));

			MIN_NUM_OF_MOBILE_DEVICES = Integer.parseInt(prop.getProperty("min_number_of_mobile_devices"));
			MAX_NUM_OF_MOBILE_DEVICES = Integer.parseInt(prop.getProperty("max_number_of_mobile_devices"));
			MOBILE_DEVICE_COUNTER_SIZE = Integer.parseInt(prop.getProperty("mobile_device_counter_size"));

			WAN_PROPOGATION_DELAY = Double.parseDouble(prop.getProperty("wan_propogation_delay"));
			LAN_INTERNAL_DELAY = Double.parseDouble(prop.getProperty("lan_internal_delay"));
			BANDWITH_WLAN = 1000 * Integer.parseInt(prop.getProperty("wlan_bandwidth")); // convert to Kbps
			BANDWITH_WAN = 1000 * Integer.parseInt(prop.getProperty("wan_bandwidth")); // convert to Kbps
			BANDWITH_GSM = 1000 * Integer.parseInt(prop.getProperty("gsm_bandwidth")); // convert to Kbps

			AREA_X_SIZE = Integer.parseInt(prop.getProperty("area_x_size"));
			AREA_Y_SIZE = Integer.parseInt(prop.getProperty("area_y_size"));
			TIME_RESOLUTION = Double.parseDouble(prop.getProperty("time_resolution"));
			VELOCITY_MEAN = Double.parseDouble(prop.getProperty("velocity_mean"));
			VELOCITY_SD = Double.parseDouble(prop.getProperty("velocity_sd"));
			PAUSE_TIME_MEAN = Double.parseDouble(prop.getProperty("pause_time_mean"));
			PAUSE_TIME_SD = Double.parseDouble(prop.getProperty("pause_time_sd"));

			NUM_OF_HOST_ON_CLOUD_DATACENTER = Integer.parseInt(prop.getProperty("number_of_host_on_cloud_datacenter"));
			NUM_OF_VM_ON_CLOUD_HOST = Integer.parseInt(prop.getProperty("number_of_vm_on_cloud_host"));
			CORE_FOR_CLOUD_VM = Integer.parseInt(prop.getProperty("core_for_cloud_vm"));
			MIPS_FOR_CLOUD_VM = Integer.parseInt(prop.getProperty("mips_for_cloud_vm"));
			RAM_FOR_CLOUD_VM = Integer.parseInt(prop.getProperty("ram_for_cloud_vm"));
			STORAGE_FOR_CLOUD_VM = Integer.parseInt(prop.getProperty("storage_for_cloud_vm"));

			RAM_FOR_VM = Integer.parseInt(prop.getProperty("ram_for_mobile_vm"));
			CORE_FOR_VM = Integer.parseInt(prop.getProperty("core_for_mobile_vm"));
			MIPS_FOR_VM = Integer.parseInt(prop.getProperty("mips_for_mobile_vm"));
			STORAGE_FOR_VM = Integer.parseInt(prop.getProperty("storage_for_mobile_vm"));

			ORCHESTRATOR_POLICIES = prop.getProperty("orchestrator_policies").split(",");

			SIMULATION_SCENARIOS = prop.getProperty("simulation_scenarios").split(",");

			//avg waiting time in a place (min)
			double place1_mean_waiting_time = Double.parseDouble(prop.getProperty("attractiveness_L1_mean_waiting_time"));
			double place2_mean_waiting_time = Double.parseDouble(prop.getProperty("attractiveness_L2_mean_waiting_time"));
			double place3_mean_waiting_time = Double.parseDouble(prop.getProperty("attractiveness_L3_mean_waiting_time"));

			//mean waiting time (minute)
			mobilityLookUpTable = new double[]{
				place1_mean_waiting_time, //ATTRACTIVENESS_L1
				place2_mean_waiting_time, //ATTRACTIVENESS_L2
				place3_mean_waiting_time  //ATTRACTIVENESS_L3
			};

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					result = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		parseApplicatinosXML(applicationsFile);
		parseEdgeDevicesXML(edgeDevicesFile);

		return result;
	}

	private void isAttribtuePresent(Element element, String key) {
		String value = element.getAttribute(key);
		if (value.isEmpty() || value == null){
			throw new IllegalArgumentException("Attribure '" + key + "' is not found in '" + element.getNodeName() +"'");
		}
	}

	private void isElementPresent(Element element, String key) {
		try {
			String value = element.getElementsByTagName(key).item(0).getTextContent();
			if (value.isEmpty() || value == null){
				throw new IllegalArgumentException("Element '" + key + "' is not found in '" + element.getNodeName() +"'");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Element '" + key + "' is not found in '" + element.getNodeName() +"'");
		}
	}

	private void parseApplicatinosXML(String filePath) {
		Document doc = null;
		try {
			File devicesFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(devicesFile);
			doc.getDocumentElement().normalize();

			NodeList appList = doc.getElementsByTagName("application");
			taskLookUpTable = new ArrayList<AppProperties>();
			for (int i = 0; i < appList.getLength(); i++) {
				Node appNode = appList.item(i);

				Element appElement = (Element) appNode;
				isAttribtuePresent(appElement, "name");
				isElementPresent(appElement, "video_x_size");
				isElementPresent(appElement, "video_y_size");
				isElementPresent(appElement, "bits_per_pixel");
				isElementPresent(appElement, "frames_per_second");
				isElementPresent(appElement, "compression_factor");
				isElementPresent(appElement, "instructions_per_pixel");
				isElementPresent(appElement, "reply_size");

				String taskName = appElement.getAttribute("name");
				int video_x_size = Integer.parseInt(appElement.getElementsByTagName("video_x_size").item(0).getTextContent());
				int video_y_size = Integer.parseInt(appElement.getElementsByTagName("video_y_size").item(0).getTextContent());
				int bits_per_pixel = Integer.parseInt(appElement.getElementsByTagName("bits_per_pixel").item(0).getTextContent());
				double fps = Double.parseDouble(appElement.getElementsByTagName("frames_per_second").item(0).getTextContent());
				double compression_factor = Double.parseDouble(appElement.getElementsByTagName("compression_factor").item(0).getTextContent());
				int instructions_per_pixel = Integer.parseInt(appElement.getElementsByTagName("instructions_per_pixel").item(0).getTextContent());
				int reply_size = Integer.parseInt(appElement.getElementsByTagName("reply_size").item(0).getTextContent());

				taskLookUpTable.add(new AppProperties(taskName, video_x_size, video_y_size, bits_per_pixel, fps, compression_factor, instructions_per_pixel, reply_size));
			}

		} catch (Exception e) {
			SimLogger.printLine("Edge Devices XML cannot be parsed! Terminating simulation...");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void parseEdgeDevicesXML(String filePath) {
		try {
			File devicesFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			edgeDevicesDoc = dBuilder.parse(devicesFile);
			edgeDevicesDoc.getDocumentElement().normalize();

			NodeList datacenterList = edgeDevicesDoc.getElementsByTagName("datacenter");
			for (int i = 0; i < datacenterList.getLength(); i++) {
				NUM_OF_EDGE_DATACENTERS++;
				Node datacenterNode = datacenterList.item(i);

				Element datacenterElement = (Element) datacenterNode;
				isAttribtuePresent(datacenterElement, "arch");
				isAttribtuePresent(datacenterElement, "os");
				isAttribtuePresent(datacenterElement, "vmm");
				isElementPresent(datacenterElement, "costPerBw");
				isElementPresent(datacenterElement, "costPerSec");
				isElementPresent(datacenterElement, "costPerMem");
				isElementPresent(datacenterElement, "costPerStorage");

				Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
				isElementPresent(location, "attractiveness");
				isElementPresent(location, "wlan_id");
				isElementPresent(location, "x_pos");
				isElementPresent(location, "y_pos");

				String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
				int placeTypeIndex = Integer.parseInt(attractiveness);
				if(NUM_OF_PLACE_TYPES < placeTypeIndex+1)
					NUM_OF_PLACE_TYPES = placeTypeIndex+1;

				NodeList hostList = datacenterElement.getElementsByTagName("host");
				for (int j = 0; j < hostList.getLength(); j++) {
					NUM_OF_EDGE_HOSTS++;
					Node hostNode = hostList.item(j);

					Element hostElement = (Element) hostNode;
					isElementPresent(hostElement, "core");
					isElementPresent(hostElement, "mips");
					isElementPresent(hostElement, "ram");
					isElementPresent(hostElement, "storage");

					NodeList vmList = hostElement.getElementsByTagName("VM");
					for (int k = 0; k < vmList.getLength(); k++) {
						NUM_OF_EDGE_VMS++;
						Node vmNode = vmList.item(k);

						Element vmElement = (Element) vmNode;
						isAttribtuePresent(vmElement, "vmm");
						isElementPresent(vmElement, "core");
						isElementPresent(vmElement, "mips");
						isElementPresent(vmElement, "ram");
						isElementPresent(vmElement, "storage");
					}
				}
			}

		} catch (Exception e) {
			SimLogger.printLine("Edge Devices XML cannot be parsed! Terminating simulation...");
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * returns the parsed XML document for edge_devices.xml
	 */
	public Document getEdgeDevicesDocument() {
		return edgeDevicesDoc;
	}

	/**
	 * returns simulation time (in seconds unit) from properties file
	 */
	public double getSimulationTime() {
		return SIMULATION_TIME;
	}

	/**
	 * returns warm up period (in seconds unit) from properties file
	 */
	public double getWarmUpPeriod() {
		return WARM_UP_PERIOD; 
	}

	/**
	 * returns VM utilization log collection interval (in seconds unit) from properties file
	 */
	public double getVmLoadLogInterval() {
		return INTERVAL_TO_GET_VM_LOAD_LOG; 
	}

	/**
	 * returns VM location log collection interval (in seconds unit) from properties file
	 */
	public double getVmLocationLogInterval() {
		return INTERVAL_TO_GET_VM_LOCATION_LOG; 
	}

	/**
	 * returns deep statistics logging status from properties file
	 */
	public boolean getDeepFileLoggingEnabled() {
		return DEEP_FILE_LOG_ENABLED; 
	}

	/**
	 * returns deep statistics logging status from properties file
	 */
	public boolean getFileLoggingEnabled() {
		return FILE_LOG_ENABLED; 
	}

	/**
	 * returns WAN propogation delay (in second unit) from properties file
	 */
	public double getWanPropogationDelay() {
		return WAN_PROPOGATION_DELAY;
	}

	/**
	 * returns internal LAN propogation delay (in second unit) from properties file
	 */
	public double getInternalLanDelay() {
		return LAN_INTERNAL_DELAY;
	}

	/**
	 * returns WLAN bandwidth (in Kbps unit) from properties file
	 */
	public int getWlanBandwidth() {
		return BANDWITH_WLAN;
	}

	/**
	 * returns WAN bandwidth (in Kbps unit) from properties file
	 */
	public int getWanBandwidth() {
		return BANDWITH_WAN; 
	}

	/**
	 * returns GSM bandwidth (in Kbps unit) from properties file
	 */
	public int getGsmBandwidth() {
		return BANDWITH_GSM;
	}

	/**
	 * returns x size of the area (in meters unit) from properties file
	 */
	public int getAreaXSize() {
		return AREA_X_SIZE;
	}

	/**
	 * returns y size of the area (in meters unit) from properties file
	 */
	public int getAreaYSize() {
		return AREA_Y_SIZE;
	}

	/**
	 * returns time resolution (in seconds unit) from properties file
	 */
	public double getTimeResolution() {
		return TIME_RESOLUTION;
	}

	/**
	 * returns mean velocity for the velocity normal distribution distribution (in meters per second unit) from properties file
	 */
	public double getVelocityMean() {
		return VELOCITY_MEAN;
	}

	/**
	 * returns standard deviation for the velocity normal distribution (in meters per second unit) from properties file
	 */
	public double getVelocitySd() {
		return VELOCITY_SD;
	}

	/**
	 * returns mean pause time for the pause time normal distribution (in seconds unit) from properties file
	 */
	public double getPauseTimeMean() {
		return PAUSE_TIME_MEAN;
	}

	/**
	 * returns standard deviation for the pause time normal distribution (in seconds unit) from properties file
	 */
	public double getPauseTimeSd() {
		return PAUSE_TIME_SD;
	}

	/**
	 * returns the minimum number of the mobile devices used in the simulation
	 */
	public int getMinNumOfMobileDev() {
		return MIN_NUM_OF_MOBILE_DEVICES;
	}

	/**
	 * returns the maximunm number of the mobile devices used in the simulation
	 */
	public int getMaxNumOfMobileDev() {
		return MAX_NUM_OF_MOBILE_DEVICES;
	}

	/**
	 * returns the number of increase on mobile devices
	 * while iterating from min to max mobile device
	 */
	public int getMobileDevCounterSize() {
		return MOBILE_DEVICE_COUNTER_SIZE;
	}

	/**
	 * returns the number of edge datacenters
	 */
	public int getNumOfEdgeDatacenters() {
		return NUM_OF_EDGE_DATACENTERS;
	}

	/**
	 * returns the number of edge hosts running on the datacenters
	 */
	public int getNumOfEdgeHosts() {
		return NUM_OF_EDGE_HOSTS;
	}

	/**
	 * returns the number of edge VMs running on the hosts
	 */
	public int getNumOfEdgeVMs() {
		return NUM_OF_EDGE_VMS;
	}

	/**
	 * returns the number of different place types
	 */
	public int getNumOfPlaceTypes() {
		return NUM_OF_PLACE_TYPES;
	}

	/**
	 * returns the number of cloud datacenters
	 */
	public int getNumOfCoudHost() {
		return NUM_OF_HOST_ON_CLOUD_DATACENTER;
	}

	/**
	 * returns the number of cloud VMs per Host
	 */
	public int getNumOfCloudVMsPerHost() {
		return NUM_OF_VM_ON_CLOUD_HOST;
	}

	/**
	 * returns the total number of cloud VMs
	 */
	public int getNumOfCloudVMs() {
		return NUM_OF_VM_ON_CLOUD_HOST * NUM_OF_HOST_ON_CLOUD_DATACENTER;
	}

	/**
	 * returns the number of cores for cloud VMs
	 */
	public int getCoreForCloudVM() {
		return CORE_FOR_CLOUD_VM;
	}

	/**
	 * returns MIPS of the central cloud VMs
	 */
	public int getMipsForCloudVM() {
		return MIPS_FOR_CLOUD_VM;
	}

	/**
	 * returns RAM of the central cloud VMs
	 */
	public int getRamForCloudVM() {
		return RAM_FOR_CLOUD_VM;
	}

	/**
	 * returns Storage of the central cloud VMs
	 */
	public int getStorageForCloudVM() {
		return STORAGE_FOR_CLOUD_VM;
	}

	/**
	 * returns RAM of the mobile (processing unit) VMs
	 */
	public int getRamForMobileVM() {
		return RAM_FOR_VM;
	}

	/**
	 * returns the number of cores for mobile VMs
	 */
	public int getCoreForMobileVM() {
		return CORE_FOR_VM;
	}

	/**
	 * returns MIPS of the mobile (processing unit) VMs
	 */
	public int getMipsForMobileVM() {
		return MIPS_FOR_VM;
	}

	/**
	 * returns Storage of the mobile (processing unit) VMs
	 */
	public int getStorageForMobileVM() {
		return STORAGE_FOR_VM;
	}

	/**
	 * returns simulation screnarios as string
	 */
	public String[] getSimulationScenarios() {
		return SIMULATION_SCENARIOS;
	}

	/**
	 * returns orchestrator policies as string
	 */
	public String[] getOrchestratorPolicies() {
		return ORCHESTRATOR_POLICIES;
	}

	/**
	 * returns mobility characteristic within an array
	 * the result includes mean waiting time (minute) or each place type
	 */ 
	public double[] getMobilityLookUpTable() {
		return mobilityLookUpTable;
	}

	public ArrayList<AppProperties> getTaskLookUpTable() {
		return taskLookUpTable;
	}

	public String getTaskName(int taskType) {
		return taskLookUpTable.get(taskType).getName();
	}
}