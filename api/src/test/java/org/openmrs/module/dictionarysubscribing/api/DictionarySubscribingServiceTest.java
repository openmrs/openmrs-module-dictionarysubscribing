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

import static org.junit.Assert.assertNotNull;
import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.dictionarysubscribing.DictionarySubscribingConstants;
import org.openmrs.module.metadatasharing.ImportedPackage;
import org.openmrs.module.metadatasharing.SubscriptionStatus;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests {@link DictionarySubscribingService}.
 */
public class DictionarySubscribingServiceTest extends BaseModuleContextSensitiveTest {
	
	private static final String TEST_URL = "http://demo.org";
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(DictionarySubscribingService.class));
	}
	
	/**
	 * @see {@link DictionarySubscribingService#subscribeToDictionary(String)}
	 */
	@Test
	@Verifies(value = "should subscribe to the dictionary at the specified url", method = "subscribeToDictionary(String)")
	public void subscribeToDictionary_shouldSubscribeToTheDictionaryAtTheSpecifiedUrl() throws Exception {
		AdministrationService as = Context.getAdministrationService();
		String groupUuid = as.getGlobalProperty(DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		Assert.assertNull(groupUuid);
		
		Context.getService(DictionarySubscribingService.class).subscribeToDictionary(TEST_URL);
		groupUuid = as.getGlobalProperty(DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		Assert.assertNotNull(groupUuid);
		ImportedPackage importedPackage = Context.getService(MetadataSharingService.class).getImportedPackageByGroup(
		    groupUuid);
		Assert.assertEquals(TEST_URL, importedPackage.getSubscriptionUrl());
		Assert.assertFalse(SubscriptionStatus.DISABLED == importedPackage.getSubscriptionStatus());
	}
	
	/**
	 * @see {@link DictionarySubscribingService#subscribeToDictionary(String)}
	 */
	@Test
	@Verifies(value = "should not create multiple subscriptions to the dictionary at the same url", method = "subscribeToDictionary(String)")
	public void subscribeToDictionary_shouldNotCreateMultipleSubscriptionsToTheDictionaryAtTheSameUrl() throws Exception {
		MetadataSharingService mss = Context.getService(MetadataSharingService.class);
		int importPackagesCount = mss.getAllImportedPackages().size();
		
		DictionarySubscribingService dss = Context.getService(DictionarySubscribingService.class);
		dss.subscribeToDictionary(TEST_URL);
		AdministrationService as = Context.getAdministrationService();
		String groupUuid = as.getGlobalProperty(DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		Assert.assertNotNull(groupUuid);
		importPackagesCount++;
		Assert.assertEquals(importPackagesCount, mss.getAllImportedPackages().size());
		
		dss.subscribeToDictionary(TEST_URL);
		//The uuid should have remained the same and no new import package created
		Assert.assertTrue(groupUuid.equals(as
		        .getGlobalProperty(DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID)));
		Assert.assertEquals(importPackagesCount, mss.getAllImportedPackages().size());
	}
}
