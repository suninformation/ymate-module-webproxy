/*
 * Copyright 2007-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.module.webproxy.impl;

import net.ymate.module.webproxy.IWebProxy;
import net.ymate.module.webproxy.IWebProxyModuleCfg;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.lang.BlurObject;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/03/29 上午 09:14
 * @version 1.0
 */
public class DefaultModuleCfg implements IWebProxyModuleCfg {

    private boolean __useProxy;

    private Proxy __proxy;

    private boolean __useCaches;

    private boolean __instanceFollowRedirects;

    private int __connectTimeout;

    private int __readTimeout;

    private String __serviceBaseUrl;

    private String __serviceRequestPrefix;

    private boolean __transferHeaderEnabled;

    private List<String> __transferHeaderWhiteList;

    private List<String> __transferHeaderBlackList;

    private List<String> __responseHeaderWhiteList;

    public DefaultModuleCfg(YMP owner) {
        Map<String, String> _moduleCfgs = owner.getConfig().getModuleConfigs(IWebProxy.MODULE_NAME);
        //
        __serviceBaseUrl = _moduleCfgs.get("service_base_url");
        if (StringUtils.isBlank(__serviceBaseUrl)) {
            throw new NullArgumentException("service_base_url");
        }
        if (!StringUtils.startsWithIgnoreCase(__serviceBaseUrl, "http://") && !StringUtils.startsWithIgnoreCase(__serviceBaseUrl, "https://")) {
            throw new IllegalArgumentException("Argument service_base_url must be start with http or https");
        } else if (StringUtils.endsWith(__serviceBaseUrl, "/")) {
            __serviceBaseUrl = StringUtils.substringBeforeLast(__serviceBaseUrl, "/");
        }
        //
        __serviceRequestPrefix = StringUtils.trimToEmpty(_moduleCfgs.get("service_request_prefix"));
        if (StringUtils.isNotBlank(__serviceRequestPrefix) && !StringUtils.startsWith(__serviceRequestPrefix, "/")) {
            __serviceRequestPrefix = "/" + __serviceRequestPrefix;
        }
        //
        __useProxy = BlurObject.bind(_moduleCfgs.get("use_proxy")).toBooleanValue();
        if (__useProxy) {
            Proxy.Type _proxyType = Proxy.Type.valueOf(StringUtils.defaultIfBlank(_moduleCfgs.get("proxy_type"), "HTTP").toUpperCase());
            int _proxyPrort = BlurObject.bind(StringUtils.defaultIfBlank(_moduleCfgs.get("proxy_port"), "80")).toIntValue();
            String _proxyHost = _moduleCfgs.get("proxy_host");
            if (StringUtils.isBlank(_proxyHost)) {
                throw new NullArgumentException("proxy_host");
            }
            __proxy = new Proxy(_proxyType, new InetSocketAddress(_proxyHost, _proxyPrort));
        }
        //
        __useCaches = BlurObject.bind(_moduleCfgs.get("use_caches")).toBooleanValue();
        __instanceFollowRedirects = BlurObject.bind(_moduleCfgs.get("instance_follow_redirects")).toBooleanValue();
        //
        __connectTimeout = BlurObject.bind(_moduleCfgs.get("connect_timeout")).toIntValue();
        __readTimeout = BlurObject.bind(_moduleCfgs.get("read_timeout")).toIntValue();
        //
        __transferHeaderEnabled = BlurObject.bind(_moduleCfgs.get("transfer_header_enabled")).toBooleanValue();
        //
        if (__transferHeaderEnabled) {
            String[] _filters = StringUtils.split(_moduleCfgs.get("transfer_header_whitelist"), "|");
            if (_filters != null && _filters.length > 0) {
                __transferHeaderWhiteList = Arrays.asList(_filters);
            } else {
                __transferHeaderWhiteList = Collections.emptyList();
            }
            //
            _filters = StringUtils.split(_moduleCfgs.get("transfer_header_blacklist"), "|");
            if (_filters != null && _filters.length > 0) {
                __transferHeaderBlackList = Arrays.asList(_filters);
            } else {
                __transferHeaderBlackList = Collections.emptyList();
            }
            //
            _filters = StringUtils.split(_moduleCfgs.get("response_header_whitelist"), "|");
            if (_filters != null && _filters.length > 0) {
                __responseHeaderWhiteList = Arrays.asList(_filters);
            } else {
                __responseHeaderWhiteList = Collections.emptyList();
            }
        } else {
            __transferHeaderWhiteList = Collections.emptyList();
            __transferHeaderBlackList = Collections.emptyList();
            //
            __responseHeaderWhiteList = Collections.emptyList();
        }
    }

    public boolean isUseProxy() {
        return __useProxy;
    }

    public Proxy getProxy() {
        return __proxy;
    }

    public boolean isUseCaches() {
        return __useCaches;
    }

    public boolean isInstanceFollowRedirects() {
        return __instanceFollowRedirects;
    }

    public int getConnectTimeout() {
        return __connectTimeout;
    }

    public int getReadTimeout() {
        return __readTimeout;
    }

    public String getServiceBaseUrl() {
        return __serviceBaseUrl;
    }

    public String getServiceRequestPrefix() {
        return __serviceRequestPrefix;
    }

    public boolean isTransferHeaderEnabled() {
        return __transferHeaderEnabled;
    }

    public List<String> getTransferHeaderBlackList() {
        return __transferHeaderBlackList;
    }

    public List<String> getTransferHeaderWhiteList() {
        return __transferHeaderWhiteList;
    }

    public List<String> getResponseHeaderWhileList() {
        return __responseHeaderWhiteList;
    }
}