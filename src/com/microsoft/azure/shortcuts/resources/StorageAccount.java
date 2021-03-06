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
package com.microsoft.azure.shortcuts.resources;

import java.net.URL;

import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.CustomDomain;
import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Creatable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Updatable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.GroupResourceBase;
import com.microsoft.azure.shortcuts.resources.common.Taggable;

public interface StorageAccount extends 
	GroupResourceBase,
	Refreshable<StorageAccount>,
	Wrapper<com.microsoft.azure.management.storage.models.StorageAccount>,
	Deletable {

	/**
	 * @return The URL of the primary blob endpoint
	 */
	public URL primaryBlobEndpoint();
	
	/**
	 * @return The type of the storage account
	 */
	public AccountType accountType();
	
	public interface Definition extends
		DefinitionBlank,
		DefinitionWithGroup,
		DefinitionCreatable {}
		
	/**
	 * A new blank storage account definition
	 */
	public interface DefinitionBlank extends 
		GroupResourceBase.DefinitionWithRegion<DefinitionWithGroup> { }
	
	/**
	 * A new storage account definition allowing to specifiy the resource group to put it in
	 */
	public interface DefinitionWithGroup extends
		GroupResourceBase.DefinitionWithResourceGroup<DefinitionCreatable> {}
	
	/**
	 * A new storage account definition with sufficient input parameters specified already to be provisioned in the cloud
	 */
	public interface DefinitionCreatable extends 
		GroupResourceBase.DefinitionWithTags<DefinitionCreatable>,
		Creatable<StorageAccount> {
		
		/**
		 * @param type The type of the storage account
		 * @return A storage account definition with sufficient required inputs to be provisioned in the cloud
		 */
	    DefinitionCreatable withAccountType(AccountType type);   
	}
	
	
	/**
	 * An existing storage account update request ready to be applied in the cloud.
	 */
	public interface Update extends 
		UpdateBlank, 
		Updatable<Update> {
	}
	
	
	/**
	 * A blank modification request for an existing storage account
	 */
	public interface UpdateBlank extends 
		Deletable, 
		Taggable<Update> {
	
		Update withRegion(String region);
		Update withAccountType(AccountType type);
		Update withCustomDomain(CustomDomain customDomain);
	}
}
