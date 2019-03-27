#!/usr/bin/env python3

import math
import xml.etree.cElementTree as ET
import os


### configuration START
edge_devices_total = 100
area_sq_km = 10


datacenter_attributes = {'arch': 'x86', 'os': 'Linux', 'vmm': 'Xen'}

costPerBw      = "0.1"
costPerSec     = "3.0"
costPerMem     = "0.05"
costPerStorage = "0.1"

attractiveness = "0"

hosts_per_edge_device = 1

host_cores   = "0"
host_mips    = "0"
host_ram     = "0"
host_storage = "0"

vms_per_host = 1
vm_attributes = {'vmm': 'Xen'}

vm_cores   = "0"
vm_mips    = "0"
vm_ram     = "0"
vm_storage = "0"
### configuration END


size_km = math.sqrt(area_sq_km)
size = size_km * 1000

edge_devices_per_edge = round(math.sqrt(edge_devices_total))
if(edge_devices_per_edge * edge_devices_per_edge != edge_devices_total): print("WARNING: Generating slightly different amount of edge devices than specified!")
edge_devices_total = edge_devices_per_edge * edge_devices_per_edge

full_distance = size / edge_devices_per_edge
half_distance = full_distance / 2


print("Generating ", edge_devices_per_edge, "*", edge_devices_per_edge, "=", edge_devices_total, " edge devices in a square of ", round(size), "m*", round(size) , "m (~", area_sq_km, " square km)", sep='')
print("Distance between edge devices: ", full_distance)
print("Distance between edge devices and edge of the area: ", half_distance)


edge_devices = ET.Element("edge_devices")

edge_devices_counter = 0 # DEBUG

for x in range(edge_devices_per_edge):
	for y in range(edge_devices_per_edge):
		datacenter = ET.SubElement(edge_devices, "datacenter", datacenter_attributes)

		ET.SubElement(datacenter, "costPerBw").text = costPerBw
		ET.SubElement(datacenter, "costPerSec").text = costPerSec
		ET.SubElement(datacenter, "costPerMem").text = costPerMem
		ET.SubElement(datacenter, "costPerStorage").text = costPerStorage
		
		location = ET.SubElement(datacenter, "location")

		ET.SubElement(location, "x_pos").text = str(round((x * full_distance) + half_distance))
		ET.SubElement(location, "y_pos").text = str(round((y * full_distance) + half_distance))
		ET.SubElement(location, "wlan_id").text = str((x * edge_devices_per_edge) + y)
		ET.SubElement(location, "attractiveness").text = attractiveness

		#print("Device ", str((x * edge_devices_per_edge) + y), " at position (", str(round((x * full_distance) + half_distance)), ",", str(round((y * full_distance) + half_distance)), ")", sep='') # DEBUG

		hosts = ET.SubElement(datacenter, "hosts")

		for i in range(hosts_per_edge_device):
			host = ET.SubElement(hosts, "host")

			ET.SubElement(host, "core").text = host_cores
			ET.SubElement(host, "mips").text = host_mips
			ET.SubElement(host, "ram").text = host_ram
			ET.SubElement(host, "storage").text = host_storage

			vms = ET.SubElement(host, "VMs")
			
			for j in range(vms_per_host):
				vm = ET.SubElement(vms, "VM", vm_attributes)

				ET.SubElement(vm, "core").text = vm_cores
				ET.SubElement(vm, "mips").text = vm_mips
				ET.SubElement(vm, "ram").text = vm_ram
				ET.SubElement(vm, "storage").text = vm_storage

		if(edge_devices_counter != (x * edge_devices_per_edge) + y): print("WARNING: edge_devices_counter does not match!") # DEBUG
		edge_devices_counter+=1

if(edge_devices_counter != edge_devices_total): print("WARNING: edge_devices_total does not match!") # DEBUG

tree = ET.ElementTree(edge_devices)
tree.write("edge_devices.xml", xml_declaration=True)


print("Replacing area size values in default_config.properties")

os.system("sed -i 's/^area_x_size=.*$/area_x_size=" + str(round(size)) + "/g' default_config.properties")
os.system("sed -i 's/^area_y_size=.*$/area_y_size=" + str(round(size)) + "/g' default_config.properties")
