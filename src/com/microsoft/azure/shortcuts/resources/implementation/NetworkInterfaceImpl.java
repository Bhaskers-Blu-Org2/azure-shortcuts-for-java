/**
* Copyright (c) Microsoft Corporation
* 
* All rights reserved. 
* 
* MIT License
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
* (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
* ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
* THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.microsoft.azure.shortcuts.resources.implementation;

import java.util.Map;
import java.util.TreeMap;

import com.microsoft.azure.management.network.models.IpAllocationMethod;
import com.microsoft.azure.management.network.models.NetworkInterfaceIpConfiguration;
import com.microsoft.azure.management.network.models.ResourceId;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.PublicIpAddress;

class NetworkInterfaceImpl 
	extends 
		NetworkableGroupableResourceBaseImpl<
			NetworkInterface, 
			com.microsoft.azure.management.network.models.NetworkInterface,
			NetworkInterfaceImpl,
			NetworkInterfacesImpl>
	implements
		NetworkInterface,
		NetworkInterface.Definition {
	
	NetworkInterfaceImpl(
			com.microsoft.azure.management.network.models.NetworkInterface azureNetworkInterface, 
			NetworkInterfacesImpl collection) {
		super(azureNetworkInterface.getName(), azureNetworkInterface, collection);
	}
	
			
	/***********************************************************
	 * Helpers
	 ***********************************************************/
	private NetworkInterfaceIpConfiguration getPrimaryIpConfiguration() {
		// TODO: in the future, Azure will support multiple ipConfigs on a NIC, but currently it doesn't, so the first one can be assumed to be the primary
		return this.inner().getIpConfigurations().get(0); 
	}
	
	
	/***********************************************************
	 * Getters
	 ***********************************************************/
	
	@Override
	public Map<String, PublicIpAddress> publicIpAddresses() {
		TreeMap<String, PublicIpAddress> pips = new TreeMap<>();
		try {
			for(NetworkInterfaceIpConfiguration ipConfig : this.inner().getIpConfigurations()) {
				ResourceId pipId = ipConfig.getPublicIpAddress();
				if(pipId == null) {
					continue;
				} else {
					PublicIpAddress pip = this.collection.subscription().publicIpAddresses(pipId.getId());
					pips.put(pip.id(), pip);						
				}
			}
		} catch (Exception e) {
		}
		return pips;
	}
	
	
	/**************************************************************
	 * Setters (fluent interface)
	 **************************************************************/
	
	
	
	
	/************************************************************
	 * Verbs
	 ************************************************************/
	
	@Override
	public void delete() throws Exception {
		this.collection.subscription().networkInterfaces().delete(this.id());
	}
	
	@Override
	public NetworkInterface provision() throws Exception {
		// Create a group as needed
		ensureGroup();
	
		// Ensure virtual network
		Network network = ensureNetwork();
		
		// Ensure subnet
		Network.Subnet subnet = ensureSubnet(network);
		
		// Set the subnet on the primary (first) IP configuration
		NetworkInterfaceIpConfiguration ipConfig = getPrimaryIpConfiguration();
		ipConfig.setName(subnet.inner().getName());
		ipConfig.setSubnet(subnet.inner());
		
		// Set the private IP
		ipConfig.setPrivateIpAllocationMethod((this.privateIpAddress != null) ? IpAllocationMethod.STATIC : IpAllocationMethod.DYNAMIC);
		ipConfig.setPrivateIpAddress(this.privateIpAddress);
	
		// Ensure and set public IP 
		PublicIpAddress pip = ensurePublicIpAddress();
		if(pip != null) {
			ResourceId r = new ResourceId();
			r.setId(pip.id());
			ipConfig.setPublicIpAddress(r);
		}
		
		Subscription subscription = this.collection.subscription();
		subscription.networkManagementClient().getNetworkInterfacesOperations().createOrUpdate(this.groupName, this.name(), this.inner());
		return subscription.networkInterfaces().get(this.groupName, this.name());
	}
	
	@Override
	public NetworkInterfaceImpl refresh() throws Exception {
		this.setInner(this.collection.getNativeEntity(
			ResourcesImpl.groupFromResourceId(this.id()), 
			ResourcesImpl.nameFromResourceId(this.id())));
		return this;
	}
}