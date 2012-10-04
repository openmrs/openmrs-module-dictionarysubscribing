/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.dictionarysubscribing.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.metadatasharing.ImportedPackage;

/**
 * Contains public API methods related to subscribing to a remote concept dictionary and getting
 * periodic updates
 */
public interface DictionarySubscribingService extends OpenmrsService {
	
	/**
	 * Creates an {@link ImportedPackage} with the specified url and saves it to the database
	 * 
	 * @param subscriptionUrl
	 * @should subscribe to the dictionary at the specified url
	 * @should not create multiple subscriptions to the dictionary at the same url
	 */
	public void subscribeToDictionary(String subscriptionUrl);
	
	/**
	 * Unsubscribe from a dictionary at the specified url
	 * 
	 * @param subscriptionUrl
	 * @should unsubscribe from the dictionary at the specified url
	 * @should not unsubscribe from the dictionary it if has a different url
	 */
	public void unsubscribeFromDictionary(String subscriptionUrl);
	
}
