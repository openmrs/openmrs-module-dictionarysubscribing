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
package org.openmrs.module.dictionarysubscribing.api.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dictionarysubscribing.DictionarySubscribingConstants;
import org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService;
import org.openmrs.module.dictionarysubscribing.api.db.DictionarySubscribingDAO;
import org.openmrs.module.metadatasharing.ImportedPackage;
import org.openmrs.module.metadatasharing.SubscriptionStatus;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link DictionarySubscribingService}.
 */
@Transactional
public class DictionarySubscribingServiceImpl extends BaseOpenmrsService implements DictionarySubscribingService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private DictionarySubscribingDAO dao;
	
	/**
	 * @param dao the dao to set
	 */
	public void setDao(DictionarySubscribingDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @return the dao
	 */
	public DictionarySubscribingDAO getDao() {
		return dao;
	}
	
	/**
	 * @see org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService#subscribeToDictionary(java.lang.String)
	 */
	@Override
	public void subscribeToDictionary(String subscriptionUrl) {
		GlobalProperty groupUuid = Context.getAdministrationService().getGlobalPropertyObject(
		    DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		MetadataSharingService mss = Context.getService(MetadataSharingService.class);
		if (groupUuid != null) {
			ImportedPackage importedPackage = mss.getImportedPackageByGroup(groupUuid.getPropertyValue());
			if (importedPackage != null) {
				if (importedPackage.getSubscriptionUrl().equals(subscriptionUrl)) {
					return;
				} else {
					throw new APIException("Cannot subcribe to multiple dictionaries");
				}
			}
		}
		
		ImportedPackage pack = new ImportedPackage();
		pack.setName("Package");
		pack.setDescription("Contains concepts");
		pack.setSubscriptionUrl(subscriptionUrl);
		mss.saveImportedPackage(pack);
		
		AdministrationService as = Context.getAdministrationService();
		GlobalProperty groupUuidGP = as
		        .getGlobalPropertyObject(DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		if (groupUuidGP == null) {
			groupUuidGP = new GlobalProperty(DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID,
			        pack.getGroupUuid(), "The group UUID of a dictionary package which you are currently subscribing");
		} else {
			groupUuidGP.setPropertyValue(pack.getGroupUuid());
		}
		
		as.saveGlobalProperty(groupUuidGP);
		if (pack.getSubscriptionStatus() != SubscriptionStatus.NEVER_CHECKED)
			checkForUpdates();
	}
	
	/**
	 * @see org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService#checkForUpdates()
	 */
	@Override
	public void checkForUpdates() {
		String groupUuid = Context.getAdministrationService().getGlobalProperty(
		    DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		if (StringUtils.isBlank(groupUuid)) {
			log.warn("There is no concept dictionary that is currently subscribed to");
			return;
		}
		
		MetadataSharingService mss = Context.getService(MetadataSharingService.class);
		ImportedPackage importedPackage = mss.getImportedPackageByGroup(groupUuid);
		if (importedPackage != null)
			mss.getSubscriptionUpdater().checkForUpdates(importedPackage);
	}
	
	@Override
	public ImportedPackage getSubscribedDictionary(){
		String groupUuid = Context.getAdministrationService().getGlobalProperty(
			    DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
			if (StringUtils.isBlank(groupUuid)) {
				log.warn("There is no concept dictionary that is currently subscribed to");
				return null;
			}
			
			MetadataSharingService mss = Context.getService(MetadataSharingService.class);
			ImportedPackage importedPackage = mss.getImportedPackageByGroup(groupUuid);
			
			return importedPackage;
	}
	
	/**
	 * @see org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService#unsubscribeFromDictionary(java.lang.String)
	 */
	@Override
	public void unsubscribeFromDictionary(String subscriptionUrl) {
		GlobalProperty groupUuid = Context.getAdministrationService().getGlobalPropertyObject(
		    DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		if (groupUuid != null) {
			MetadataSharingService mss = Context.getService(MetadataSharingService.class);
			ImportedPackage importedPackage = mss.getImportedPackageByGroup(groupUuid.getPropertyValue());
			if (importedPackage != null && importedPackage.getSubscriptionUrl().equals(subscriptionUrl)) {
				importedPackage.setSubscriptionStatus(SubscriptionStatus.DISABLED);
				mss.saveImportedPackage(importedPackage);
				
				groupUuid.setPropertyValue("");
				Context.getAdministrationService().saveGlobalProperty(groupUuid);
			}
		}
	}
}
