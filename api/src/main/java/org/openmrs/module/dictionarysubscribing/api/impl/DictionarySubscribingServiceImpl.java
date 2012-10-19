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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

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
import org.openmrs.module.metadatasharing.ImportConfig;
import org.openmrs.module.metadatasharing.ImportMode;
import org.openmrs.module.metadatasharing.ImportedPackage;
import org.openmrs.module.metadatasharing.MetadataSharing;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.openmrs.module.metadatasharing.downloader.Downloader;
import org.openmrs.module.metadatasharing.downloader.DownloaderFactory;
import org.openmrs.module.metadatasharing.subscription.SubscriptionHeader;
import org.openmrs.module.metadatasharing.updater.SubscriptionUpdater;
import org.openmrs.module.metadatasharing.wrapper.PackageImporter;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link DictionarySubscribingService}.
 */
public class DictionarySubscribingServiceImpl extends BaseOpenmrsService implements DictionarySubscribingService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private DictionarySubscribingDAO dao;
	
	@Autowired
	private SubscriptionUpdater updater;
	
	@Autowired
	private DownloaderFactory downloaderFactory;
	
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
	
	public AdministrationService getAS() {
		return Context.getAdministrationService();
	}
	
	public MetadataSharingService getMSS() {
		return Context.getService(MetadataSharingService.class);
	}
	
	/**
	 * @see org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService#subscribeToDictionary(java.lang.String, int)
	 */
	@Transactional
	@Override
	public void subscribeToDictionary(String subscriptionUrl, int version) {
		GlobalProperty groupUuid = getAS().getGlobalPropertyObject(
		    DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		
		if (groupUuid != null) {
			ImportedPackage importedPackage = getMSS().getImportedPackageByGroup(groupUuid.getPropertyValue());
			
			if (importedPackage != null) {
				if (importedPackage.getSubscriptionUrl().equals(subscriptionUrl)) {
					return;
				} else {
					throw new APIException("Cannot subcribe to multiple dictionaries");
				}
			}
		}
		
		//Start from version
		version--;
		
		//Check for updates
		ImportedPackage pack = new ImportedPackage();
		pack.setSubscriptionUrl(subscriptionUrl);
		if (version == 0) {
			pack.setVersion(null);
		} else {
			pack.setVersion(version);
		}
		pack.setGroupUuid(null);
		updater.checkForUpdates(pack);
		
		if (pack.getGroupUuid() == null) {
			pack.setGroupUuid(UUID.randomUUID().toString());
		}
		
		getMSS().saveImportedPackage(pack);
		
		//Save dictionary groupUuid
		GlobalProperty groupUuidGP = getAS().getGlobalPropertyObject(
		    DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		if (groupUuidGP == null) {
			groupUuidGP = new GlobalProperty(DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID,
			        pack.getGroupUuid(), "The group UUID of a dictionary package which you are currently subscribing");
		} else {
			groupUuidGP.setPropertyValue(pack.getGroupUuid());
		}
		
		getAS().saveGlobalProperty(groupUuidGP);
		
		lockDictionary();
	}
	
	/**
	 * @see org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService#checkForUpdates()
	 */
	@Transactional
	@Override
	public void checkForUpdates() {
		String groupUuid = Context.getAdministrationService().getGlobalProperty(
		    DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		if (StringUtils.isBlank(groupUuid)) {
			log.warn("There is no concept dictionary that you is currently subscribed");
			return;
		}
		
		ImportedPackage importedPackage = getMSS().getImportedPackageByGroup(groupUuid);
		if (importedPackage != null)
			getMSS().getSubscriptionUpdater().checkForUpdates(importedPackage);
	}
	
	@Transactional(readOnly = true)
	@Override
	public ImportedPackage getSubscribedDictionary() {
		String groupUuid = Context.getAdministrationService().getGlobalProperty(
		    DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		if (StringUtils.isBlank(groupUuid)) {
			log.warn("There is no concept dictionary that is currently subscribed");
			return null;
		}
		
		ImportedPackage importedPackage = getMSS().getImportedPackageByGroup(groupUuid);
		
		return importedPackage;
	}
	
	/**
	 * @see org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService#unsubscribeFromDictionary(java.lang.String)
	 */
	@Transactional
	@Override
	public void unsubscribeFromDictionary(String subscriptionUrl) {
		GlobalProperty groupUuid = Context.getAdministrationService().getGlobalPropertyObject(
		    DictionarySubscribingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		
		if (groupUuid != null) {
			ImportedPackage importedPackage = getMSS().getImportedPackageByGroup(groupUuid.getPropertyValue());
			
			if (importedPackage != null && importedPackage.getSubscriptionUrl().equals(subscriptionUrl)) {
				getMSS().deleteImportedPackage(importedPackage);
				
				groupUuid.setPropertyValue("");
				Context.getAdministrationService().saveGlobalProperty(groupUuid);
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService#importDictionaryUpdates()
	 */
	@Override
	public void importDictionaryUpdates() throws APIException {
		ImportedPackage dictionary = getSubscribedDictionary();
		SubscriptionHeader header = updater.getSubscriptionHeader(dictionary);
		
		if (!dictionary.hasSubscriptionErrors()) {
			if (!dictionary.isImported() || dictionary.getRemoteVersion().compareTo(dictionary.getVersion()) > 0) {
				int version = (dictionary.getVersion() != null) ? dictionary.getVersion() : 0;
				version++;
				
				for (; version <= dictionary.getRemoteVersion(); version++) {
					URL packageContentUrl = getContentUrl(dictionary, header, version);
					
					getThisService().importPackage(dictionary, packageContentUrl);
					
					dictionary = getMSS().getImportedPackageByGroup(dictionary.getGroupUuid());
					updater.checkForUpdates(dictionary);
				}
			}
		}
	}
	
	@Override
    @Transactional
	public void importPackage(ImportedPackage dictionary, URL packageContentUrl) {
		//Preserve subscription URL
		String subscriptionUrl = dictionary.getSubscriptionUrl();
		
		PackageImporter importer = MetadataSharing.getInstance().newPackageImporter();
		importer.getImportedPackage().setId(dictionary.getId());
		Context.evictFromSession(dictionary); //evict so that it can be overwritten
		try {
			Downloader downloader = downloaderFactory.getDownloader(packageContentUrl);
			byte[] zippedPackage = downloader.downloadAsByteArray();
			importer.loadSerializedPackageStream(new ByteArrayInputStream(zippedPackage));
		}
		catch (IOException e) {
			throw new APIException(e);
		}
		
		importer.setImportConfig(ImportConfig.valueOf(ImportMode.MIRROR));
		
		unlockDictionary();
		
		importer.importPackage();
		
		importer.getImportedPackage().setSubscriptionUrl(subscriptionUrl);
		
		getMSS().saveImportedPackage(importer.getImportedPackage());
		
		lockDictionary();
	}
	
	private URL getContentUrl(ImportedPackage dictionary, SubscriptionHeader header, Integer version) {
		try {
			URL url = new URL(new URL(dictionary.getSubscriptionUrl()), "ws/rest/metadatasharing/package/"
			        + header.getContentUri().toString().substring(3));
			URL downloadURL = new URL(url, "../" + version + "/download.form");
			
			return downloadURL;
		}
		catch (MalformedURLException e) {
			throw new APIException();
		}
	}
	
	/**
	 * @see org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService#isDictionaryLocked()
	 */
	@Transactional(readOnly = true)
	@Override
	public boolean isDictionaryLocked() {
		String locked = getAS().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_CONCEPTS_LOCKED, "false");
		return Boolean.valueOf(locked);
	}
	
	private void unlockDictionary() {
		GlobalProperty conceptsLocked = getAS().getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_CONCEPTS_LOCKED);
		if (conceptsLocked == null) {
			conceptsLocked = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_CONCEPTS_LOCKED, "false",
			        "Disables modyfications to concept dictionary");
		} else {
			conceptsLocked.setPropertyValue("false");
		}
		
		getAS().saveGlobalProperty(conceptsLocked);
	}
	
	private void lockDictionary() {
		GlobalProperty conceptsLocked = getAS().getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_CONCEPTS_LOCKED);
		if (conceptsLocked == null) {
			conceptsLocked = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_CONCEPTS_LOCKED, "true",
			        "Disables modyfications to concept dictionary");
		} else {
			conceptsLocked.setPropertyValue("true");
		}
		
		getAS().saveGlobalProperty(conceptsLocked);
	}

	/**
     * @see org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService#getConceptsCount()
     */
	@Transactional(readOnly = true)
    @Override
    public Long getConceptsCount() {
	    return dao.getConceptsCount();
    }
	
	/**
	 * A work-around which allows for transactional calls within this service.
	 * 
	 * @return this service proxy
	 */
	private DictionarySubscribingService getThisService() {
		return Context.getService(DictionarySubscribingService.class);
	}
}
