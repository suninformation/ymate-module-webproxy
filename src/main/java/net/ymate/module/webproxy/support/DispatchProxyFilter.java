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
package net.ymate.module.webproxy.support;

import net.ymate.module.webproxy.WebProxy;
import net.ymate.module.webproxy.WebProxyEvent;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.event.IEvent;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.WebMVC;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.impl.DefaultRequestContext;
import net.ymate.platform.webmvc.support.GenericDispatcher;
import net.ymate.platform.webmvc.support.GenericResponseWrapper;
import net.ymate.platform.webmvc.support.RequestMethodWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author 刘镇 (suninformation@163.com) on 17/3/29 下午3:21
 * @version 1.0
 */
public class DispatchProxyFilter implements Filter {

    private static final Log _LOG = LogFactory.getLog(DispatchProxyFilter.class);

    private Pattern __ignorePatern;

    private FilterConfig __filterConfig;

    private String __prefix;

    private String __charsetEncoding;
    private String __requestMethodParam;
    private String __requestPrefix;

    public void init(FilterConfig filterConfig) throws ServletException {
        __filterConfig = filterConfig;
        String _regex = WebMVC.get().getModuleCfg().getRequestIgnoreRegex();
        if (!"false".equalsIgnoreCase(_regex)) {
            __ignorePatern = Pattern.compile(_regex, Pattern.CASE_INSENSITIVE);
        }
        __prefix = WebProxy.get().getModuleCfg().getServiceRequestPrefix();
        //
        __charsetEncoding = WebMVC.get().getModuleCfg().getDefaultCharsetEncoding();
        __requestMethodParam = WebMVC.get().getModuleCfg().getRequestMethodParam();
        __requestPrefix = WebMVC.get().getModuleCfg().getRequestPrefix();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding(__charsetEncoding);
        response.setCharacterEncoding(__charsetEncoding);
        //
        response.setContentType(Type.ContentType.HTML.getContentType().concat("; charset=").concat(__charsetEncoding));
        //
        HttpServletRequest _request = new RequestMethodWrapper((HttpServletRequest) request, __requestMethodParam);
        HttpServletResponse _response = (HttpServletResponse) response;
        IRequestContext _requestContext = new DefaultRequestContext(_request, __requestPrefix);
        if (null == __ignorePatern || !__ignorePatern.matcher(_requestContext.getOriginalUrl()).find()) {
            if (StringUtils.isNotBlank(__prefix) && !StringUtils.startsWith(_requestContext.getRequestMapping(), __prefix)) {
                _response = new GenericResponseWrapper(_response);
                GenericDispatcher.create(WebMVC.get()).execute(_requestContext, __filterConfig.getServletContext(), _request, _response);
            } else {
                try {
                    YMP.get().getEvents().fireEvent(new WebProxyEvent(WebProxy.get(), WebProxyEvent.EVENT.REQUEST_RECEIVED).addParamExtend(IEvent.EVENT_SOURCE, _requestContext));
                    //
                    String _requestMapping = _requestContext.getRequestMapping();
                    if (StringUtils.isNotBlank(__prefix)) {
                        _requestMapping = StringUtils.substringAfter(_requestMapping, __prefix);
                    }
                    StringBuilder _url = new StringBuilder(WebProxy.get().getModuleCfg().getServiceBaseUrl()).append(_requestMapping);
                    if (Type.HttpMethod.GET.equals(_requestContext.getHttpMethod())) {
                        if (StringUtils.isNotBlank(_request.getQueryString())) {
                            _url.append("?").append(_request.getQueryString());
                        }
                    }
                    WebProxy.get().transmission(_request, _response, _url.toString(), _requestContext.getHttpMethod());
                } catch (Throwable e) {
                    _LOG.warn("An exception occurred: ", RuntimeUtils.unwrapThrow(e));
                } finally {
                    YMP.get().getEvents().fireEvent(new WebProxyEvent(WebProxy.get(), WebProxyEvent.EVENT.REQUEST_COMPLETED).addParamExtend(IEvent.EVENT_SOURCE, _requestContext));
                }
            }
        } else {
            chain.doFilter(_request, _response);
        }
    }

    public void destroy() {
    }
}
