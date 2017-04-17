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

import java.net.Proxy;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/03/29 上午 09:14
 * @version 1.0
 */
public interface IWebProxyModuleCfg {

    /**
     * @return 是否开启代理模式, 默认值: false
     */
    boolean isUseProxy();

    /**
     * @return 代理对象，若未开启代理模式则返回空
     */
    Proxy getProxy();

    /**
     * @return 是否使用缓存, 默认值: false
     */
    boolean isUseCaches();

    /**
     * @return 连接是否自动处理重定向, 默认值: false
     */
    boolean isInstanceFollowRedirects();

    /**
     * @return 连接超时时间(毫秒), 默认值: 0
     */
    int getConnectTimeout();

    /**
     * @return 数据读超时时间(毫秒), 默认值: 0
     */
    int getReadTimeout();

    /**
     * @return 代理服务基准URL路径, 此项必填), 必须以'http://'或'https://'开始并以'/'结束, 如: http://www.ymate.net/proxies/
     */
    String getServiceBaseUrl();

    /**
     * @return 请求路径前缀(仅透传转发以此为前缀的请求)，可选参数，默认值为空
     */
    String getServiceRequestPrefix();

    /**
     * @return 是否开启请求头传输, 默认值: false
     */
    boolean isTransferHeaderEnabled();

    /**
     * @return 传输请求头名称黑名单, 默认值为空(表示不启用), 存在于列表中的请求头将不被发送, 多个名称间用'|'分隔
     */
    List<String> getTransferHeaderBlackList();

    /**
     * @return 传输请求头名称白名单, 默认值为空(表示不启用), 存在于列表中的请求头将被发送, 多个名称间用'|'分隔
     */
    List<String> getTransferHeaderWhiteList();

    /**
     * @return 响应头名称白名单, 默认值为空(表示不启用), 存在于列表中的响应头将被回传至客户端, 多个名称间用'|'分隔
     */
    List<String> getResponseHeaderWhileList();
}