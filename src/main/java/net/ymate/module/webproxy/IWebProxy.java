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

import net.ymate.platform.core.YMP;
import net.ymate.platform.webmvc.base.Type;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/03/29 上午 09:14
 * @version 1.0
 */
public interface IWebProxy {

    String MODULE_NAME = "module.webproxy";

    /**
     * @return 返回所属YMP框架管理器实例
     */
    YMP getOwner();

    /**
     * @return 返回模块配置对象
     */
    IWebProxyModuleCfg getModuleCfg();

    /**
     * @return 返回模块是否已初始化
     */
    boolean isInited();

    /**
     * 传送请求
     *
     * @param request  请求对象
     * @param response 回应对象
     * @param url      请求目标URL地址
     * @param method   请求方法类型
     * @throws Exception 可能产生的任何异常
     */
    void transmission(HttpServletRequest request, HttpServletResponse response, String url, Type.HttpMethod method) throws Exception;
}