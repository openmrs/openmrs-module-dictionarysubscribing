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
package org.openmrs.module.dictionarysubscribing.web.controller;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.dictionarysubscribing.api.DictionarySubscribingService;
import org.openmrs.module.metadatasharing.ImportedPackage;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The main controller.
 */
@Controller
public class DictionarySubscribingController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public static final String MODULE_URL = "/module/dictionarysubscribing/";
	
	@RequestMapping(value = MODULE_URL + "subscribe", method = RequestMethod.GET)
	public String subscribe(ModelMap model) {
		ImportedPackage dictionary = getService().getSubscribedDictionary();
		if (dictionary == null) {
			model.addAttribute("user", Context.getAuthenticatedUser());
			model.addAttribute("conceptCount", Context.getConceptService().getAllConcepts().size());
			return null;
		} else {
			return "redirect:subscribed.form";
		}
	}
	
	@RequestMapping(value = MODULE_URL + "subscribe", method = RequestMethod.POST)
	public String subscribePOST(String url) {
		getService().subscribeToDictionary(url);
		return "redirect:subscribed.form";
	}
	
	@RequestMapping(MODULE_URL + "subscribed")
	public String subscribed(HttpSession httpSession, ModelMap model) {
		ImportedPackage importedPackage = getService().getSubscribedDictionary();
		if (importedPackage.hasSubscriptionErrors()) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
			    "Unable to subscribe to url: " + importedPackage.getSubscriptionStatus());
		}
		
		model.addAttribute("dictionary", getService().getSubscribedDictionary());
		model.addAttribute("url", importedPackage.getSubscriptionUrl());
		
		return null;
	}
	
	private DictionarySubscribingService getService() {
		return Context.getService(DictionarySubscribingService.class);
	}
	
	@RequestMapping(value = MODULE_URL + "updateToLatestVersion", method = RequestMethod.POST)
	public String updateToLatestVersion() {
		getService().importDictionaryUpdates();
		return "redirect:subscribed.form";
	}
}
