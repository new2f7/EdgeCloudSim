#!/usr/bin/env python3

import math
import xml.etree.cElementTree as ET
import os

### configuration START
x_size_km = 5
y_size_km = 2

edge_devices_per_km = 3


datacenter_attributes = {'arch': 'x86', 'os': 'Linux', 'vmm': 'Xen'}

costPerBw      = "0.1"
costPerSec     = "3.0"
costPerMem     = "0.05"
costPerStorage = "0.1"

attractiveness = "0"

hosts_per_edge_device = 1

host_cores   =      "8"
host_mips    =   "4000"
host_ram     =   "8000"
host_storage = "200000"

vms_per_host = 2
vm_attributes = {'vmm': 'Xen'}

vm_cores   =     "2"
vm_mips    =  "1000"
vm_ram     =  "2000"
vm_storage = "50000"
### configuration END


x_size = x_size_km * 1000
y_size = y_size_km * 1000

full_distance = 1000 / edge_devices_per_km
half_distance = full_distance / 2

edge_devices_x = x_size_km * edge_devices_per_km
edge_devices_y = y_size_km * edge_devices_per_km

area_sq_km = x_size_km * y_size_km
edge_devices_total = edge_devices_x * edge_devices_y


print("Generating ", edge_devices_x, "*", edge_devices_y, "=", edge_devices_total, " edge devices in a rectangle of ", x_size, "m*", y_size , "m (=", area_sq_km, " square km)", sep='')
print("Distance between edge devices: ", full_distance)
print("Distance between edge devices and edge of the area: ", half_distance)


edge_devices = ET.Element("edge_devices")

edge_devices_counter = 0 # DEBUG

for x in range(edge_devices_x):
	for y in range(edge_devices_y):
		datacenter = ET.SubElement(edge_devices, "datacenter", datacenter_attributes)

		ET.SubElement(datacenter, "costPerBw").text = costPerBw
		ET.SubElement(datacenter, "costPerSec").text = costPerSec
		ET.SubElement(datacenter, "costPerMem").text = costPerMem
		ET.SubElement(datacenter, "costPerStorage").text = costPerStorage
		
		location = ET.SubElement(datacenter, "location")

		ET.SubElement(location, "x_pos").text = str(round((x * full_distance) + half_distance))
		ET.SubElement(location, "y_pos").text = str(round((y * full_distance) + half_distance))
		ET.SubElement(location, "wlan_id").text = str((x * edge_devices_y) + y)
		ET.SubElement(location, "attractiveness").text = attractiveness

		#print("Device ", str((x * edge_devices_y) + y), " at position (", str(round((x * full_distance) + half_distance)), ",", str(round((y * full_distance) + half_distance)), ")", sep='') # DEBUG

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

		if(edge_devices_counter != (x * edge_devices_y) + y): print("WARNING: edge_devices_counter does not match!") # DEBUG
		edge_devices_counter+=1

if(edge_devices_counter != edge_devices_total): print("WARNING: edge_devices_total does not match!") # DEBUG

tree = ET.ElementTree(edge_devices)
tree.write("edge_devices.xml", xml_declaration=True)


print("Replacing area size values in default_config.properties")

os.system("sed -i 's/^area_x_size=.*$/area_x_size=" + str(x_size) + "/g' default_config.properties")
os.system("sed -i 's/^area_y_size=.*$/area_y_size=" + str(y_size) + "/g' default_config.properties")
