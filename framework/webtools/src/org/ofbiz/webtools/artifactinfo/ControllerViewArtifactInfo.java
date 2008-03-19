/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ofbiz.webtools.artifactinfo;

import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilObject;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.webapp.control.ConfigXMLReader;

/**
 *
 */
public class ControllerViewArtifactInfo extends ArtifactInfoBase {
    public static final String module = ControllerViewArtifactInfo.class.getName();

    protected URL controllerXmlUrl;
    protected String viewUri;
    
    protected Map<String, String> viewInfoMap;
    
    protected ScreenWidgetArtifactInfo screenCalledByThisView = null;
    
    public ControllerViewArtifactInfo(URL controllerXmlUrl, String viewUri, ArtifactInfoFactory aif) throws GeneralException {
        super(aif);
        this.controllerXmlUrl = controllerXmlUrl;
        this.viewUri = viewUri;
        
        this.viewInfoMap = aif.getControllerViewInfoMap(controllerXmlUrl, viewUri);
        
        if (this.viewInfoMap == null) {
            throw new GeneralException("Could not find Controller View [" + viewUri + "] at URL [" + controllerXmlUrl.toExternalForm() + "]");
        }

        if (this.viewInfoMap == null) {
            throw new GeneralException("Controller view with name [" + viewUri + "] is not defined in controller file [" + controllerXmlUrl + "].");
        }
        // populate screenCalledByThisView and reverse in aif.allViewInfosReferringToScreen
        if ("screen".equals(this.viewInfoMap.get(ConfigXMLReader.VIEW_TYPE))) {
            String fullScreenName = this.viewInfoMap.get(ConfigXMLReader.VIEW_PAGE);
            if (UtilValidate.isNotEmpty(fullScreenName)) {
                int poundIndex = fullScreenName.indexOf('#');
                try {
                    this.screenCalledByThisView = this.aif.getScreenWidgetArtifactInfo(fullScreenName.substring(poundIndex+1), fullScreenName.substring(0, poundIndex));
                    if (this.screenCalledByThisView != null) {
                        // add the reverse association
                        UtilMisc.addToSetInMap(this, aif.allViewInfosReferringToScreen, this.screenCalledByThisView.getUniqueId());
                    }
                } catch (GeneralException e) {
                    Debug.logWarning(e.toString(), module);
                }
            }
        }
    }
    
    public URL getControllerXmlUrl() {
        return this.controllerXmlUrl;
    }
    
    public String getViewUri() {
        return this.viewUri;
    }
    
    public String getDisplayName() {
        return this.getUniqueId();
    }
    
    public String getDisplayType() {
        return "Controller View";
    }
    
    public String getType() {
        return ArtifactInfoFactory.ControllerViewInfoTypeId;
    }
    
    public String getUniqueId() {
        return this.controllerXmlUrl.toExternalForm() + "#" + this.viewUri;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof ControllerViewArtifactInfo) {
            ControllerViewArtifactInfo that = (ControllerViewArtifactInfo) obj;
            return UtilObject.equalsHelper(this.controllerXmlUrl, that.controllerXmlUrl) &&
                UtilObject.equalsHelper(this.viewUri, that.viewUri);
        } else {
            return false;
        }
    }
    
    public Set<ControllerRequestArtifactInfo> getRequestsThatThisViewIsResponseTo() {
        return this.aif.allRequestInfosReferringToView.get(this.getUniqueId());
    }
    
    public ScreenWidgetArtifactInfo getScreenCalledByThisView() {
        return screenCalledByThisView;
    }
}
