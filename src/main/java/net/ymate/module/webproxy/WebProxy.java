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
package net.ymate.module.webproxy;

import net.ymate.framework.commons.ParamUtils;
import net.ymate.module.webproxy.impl.DefaultModuleCfg;
import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.WebMVC;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 17/3/29 下午3:21
 * @version 1.0
 */
@Module
public class WebProxy implements IModule, IWebProxy {

    private static final Log _LOG = LogFactory.getLog(WebProxy.class);

    public static final Version VERSION = new Version(1, 0, 0, WebProxy.class.getPackage().getImplementationVersion(), Version.VersionType.Alphal);

    private static volatile IWebProxy __instance;

    private YMP __owner;

    private IWebProxyModuleCfg __moduleCfg;

    private boolean __inited;

    public static IWebProxy get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(WebProxy.class);
                }
            }
        }
        return __instance;
    }

    public String getName() {
        return IWebProxy.MODULE_NAME;
    }

    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-module-webproxy-" + VERSION);
            //
            __owner = owner;
            __moduleCfg = new DefaultModuleCfg(owner);
            //
            _LOG.info("-->          service_base_url: " + __moduleCfg.getServiceBaseUrl());
            _LOG.info("-->            request_prefix: " + StringUtils.defaultIfBlank(__moduleCfg.getServiceRequestPrefix(), "none"));
            _LOG.info("-->                     proxy: " + (__moduleCfg.getProxy() != null ? __moduleCfg.getProxy().toString() : "none"));
            _LOG.info("-->   transfer_header_enabled: " + __moduleCfg.isTransferHeaderEnabled());
            if (__moduleCfg.isTransferHeaderEnabled()) {
                _LOG.info("-->   transfer_header_filters: " + __moduleCfg.getTransferHeaderFilters());
            }
            _LOG.info("-->                use_caches: " + __moduleCfg.isUseCaches());
            _LOG.info("--> instance_follow_redirects: " + __moduleCfg.isInstanceFollowRedirects());
            _LOG.info("-->        connection_timeout: " + __moduleCfg.getConnectTimeout());
            _LOG.info("-->              read_timeout: " + __moduleCfg.getReadTimeout());
            //
            __inited = true;
        }
    }

    public boolean isInited() {
        return __inited;
    }

    private String __doParseContentBody(HttpURLConnection _conn, byte[] _content, String charset) throws Exception {
        if (StringUtils.contains(_conn.getHeaderField("Content-Encoding"), "gzip")) {
            ByteArrayInputStream _input = null;
            GZIPInputStream _gzip = null;
            ByteArrayOutputStream _output = null;
            try {
                _input = new ByteArrayInputStream(_content);
                _gzip = new GZIPInputStream(_input);
                _output = new ByteArrayOutputStream();
                //
                IOUtils.copyLarge(_gzip, _output);
                return new String(_output.toByteArray(), charset);
            } finally {
                IOUtils.closeQuietly(_output);
                IOUtils.closeQuietly(_gzip);
                IOUtils.closeQuietly(_input);
            }
        }
        return new String(_content, charset);
    }

    @SuppressWarnings("unchecked")
    public void transmission(HttpServletRequest request, HttpServletResponse response, String url, Type.HttpMethod method) throws Exception {
        StopWatch _consumeTime = null;
        long _threadId = 0;
        if (_LOG.isDebugEnabled()) {
            _consumeTime = new StopWatch();
            _consumeTime.start();
            _threadId = Thread.currentThread().getId();
            _LOG.debug("-------------------------------------------------");
            _LOG.debug("--> [" + _threadId + "] URL: " + url);
        }
        //
        HttpURLConnection _conn = null;
        try {
            if (__moduleCfg.isUseProxy()) {
                _conn = (HttpURLConnection) new URL(url).openConnection(__moduleCfg.getProxy());
            } else {
                _conn = (HttpURLConnection) new URL(url).openConnection();
            }
            _conn.setUseCaches(__moduleCfg.isUseCaches());
            _conn.setInstanceFollowRedirects(__moduleCfg.isInstanceFollowRedirects());
            if (Type.HttpMethod.POST.equals(method)) {
                _conn.setDoOutput(true);
                _conn.setDoInput(true);
                _conn.setRequestMethod(method.name());
            }
            if (__moduleCfg.getConnectTimeout() > 0) {
                _conn.setConnectTimeout(__moduleCfg.getConnectTimeout());
            }
            if (__moduleCfg.getReadTimeout() > 0) {
                _conn.setReadTimeout(__moduleCfg.getReadTimeout());
            }
            //
            if (_LOG.isDebugEnabled()) {
                _LOG.debug("--> [" + _threadId + "] Method: " + method.name());
                _LOG.debug("--> [" + _threadId + "] Request Headers: ");
            }
            //
            Enumeration _header = request.getHeaderNames();
            while (_header.hasMoreElements()) {
                String _name = (String) _header.nextElement();
                String _value = request.getHeader(_name);
                if (__moduleCfg.isTransferHeaderEnabled() && __moduleCfg.getTransferHeaderFilters().contains(_name)) {
                    _conn.setRequestProperty(_name, _value);
                }
                //
                if (_LOG.isDebugEnabled()) {
                    _LOG.debug("--> [" + _threadId + "] \t - " + _name + ": " + _value);
                }
            }
            _conn.connect();
            //
            boolean _multipartFlag = false;
            //
            if (Type.HttpMethod.POST.equals(method)) {
                DataOutputStream _output = new DataOutputStream(_conn.getOutputStream());
                try {
                    if (StringUtils.contains(request.getContentType(), "multipart/")) {
                        if (_LOG.isDebugEnabled()) {
                            _LOG.debug("--> [" + _threadId + "] Multipart: TRUE");
                        }
                        //
                        _multipartFlag = true;
                        IOUtils.copyLarge(request.getInputStream(), _output);
                    } else {
                        String _charset = request.getCharacterEncoding();
                        String _queryStr = ParamUtils.buildQueryParamStr(request.getParameterMap(), true, _charset);
                        IOUtils.write(_queryStr, _output, _charset);
                        //
                        if (_LOG.isDebugEnabled()) {
                            _LOG.debug("--> [" + _threadId + "] Request Parameters: ");
                            Map<String, String> _paramsMap = ParamUtils.parseQueryParamStr(_queryStr, true, _charset);
                            for (Map.Entry<String, String> _param : _paramsMap.entrySet()) {
                                _LOG.debug("--> [" + _threadId + "] \t - " + _param.getKey() + ": " + _param.getValue());
                            }
                        }
                    }
                    _output.flush();
                } finally {
                    IOUtils.closeQuietly(_output);
                }
            }
            //
            int _code = _conn.getResponseCode();
            response.setStatus(_code);
            //
            if (_LOG.isDebugEnabled()) {
                _LOG.debug("--> [" + _threadId + "] Response Code: " + _code);
                _LOG.debug("--> [" + _threadId + "] Response Headers: ");
            }
            //
            Map<String, List<String>> _headers = _conn.getHeaderFields();
            for (Map.Entry<String, List<String>> _entry : _headers.entrySet()) {
                if (_entry.getKey() != null) {
                    String _values = StringUtils.join(_entry.getValue(), ",");
                    response.setHeader(_entry.getKey(), _values);
                    if (_LOG.isDebugEnabled()) {
                        _LOG.debug("--> [" + _threadId + "] \t - " + _entry.getKey() + ": " + _values);
                    }
                }
            }
            if (HttpURLConnection.HTTP_OK == _code) {
                InputStream _inputStream = _conn.getInputStream();
                if (_inputStream != null) {
                    if (!_multipartFlag) {
                        byte[] _content = IOUtils.toByteArray(_inputStream);
                        IOUtils.write(_content, response.getOutputStream());
                        //
                        if (_LOG.isDebugEnabled()) {
                            _LOG.debug("--> [" + _threadId + "] Response Content: " + __doParseContentBody(_conn, _content, WebMVC.get().getModuleCfg().getDefaultCharsetEncoding()));
                        }
                    } else {
                        IOUtils.copyLarge(_conn.getInputStream(), response.getOutputStream());
                        //
                        if (_LOG.isDebugEnabled()) {
                            _LOG.debug("--> [" + _threadId + "] Response Content: MultipartBody");
                        }
                    }
                } else if (_LOG.isDebugEnabled()) {
                    _LOG.debug("--> [" + _threadId + "] Response Content: NULL");
                }
            } else {
                InputStream _inputStream = _conn.getInputStream();
                if (_inputStream != null) {
                    byte[] _content = IOUtils.toByteArray(_inputStream);
                    IOUtils.write(_content, response.getOutputStream());
                    //
                    if (_LOG.isDebugEnabled()) {
                        _LOG.debug("--> [" + _threadId + "] Response Content: " + __doParseContentBody(_conn, _content, WebMVC.get().getModuleCfg().getDefaultCharsetEncoding()));
                    }
                } else if (_LOG.isDebugEnabled()) {
                    _LOG.debug("--> [" + _threadId + "] Response Content: NULL");
                }
            }
            response.flushBuffer();
        } catch (Throwable e) {
            _LOG.warn("An exception occurred while processing request mapping '" + url + "': ", RuntimeUtils.unwrapThrow(e));
        } finally {
            IOUtils.close(_conn);
            //
            if (_LOG.isDebugEnabled()) {
                if (_consumeTime != null) {
                    _consumeTime.stop();
                    _LOG.debug("--> [" + _threadId + "] Total execution time: " + _consumeTime.getTime() + "ms");
                }
                _LOG.debug("-------------------------------------------------");
            }
        }
    }

    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            __moduleCfg = null;
            __owner = null;
        }
    }

    public YMP getOwner() {
        return __owner;
    }

    public IWebProxyModuleCfg getModuleCfg() {
        return __moduleCfg;
    }
}
