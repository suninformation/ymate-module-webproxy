### WebProxy

基于YMP框架实现的简单HTTP请求透传代理模块，用于将本地请求转发至远程服务器，并向浏览器返回远程服务的响应结果；

#### Maven包依赖

    <dependency>
        <groupId>net.ymate.module</groupId>
        <artifactId>ymate-module-webproxy</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

> **注**：
> 本项目依赖 `ymate-framework-v2` 库，[请前往下载最新代码](https://github.com/suninformation/ymate-framework-v2) 

#### 模块初始化

- 首先，你需要创建一个基于YMPv2框架的JavaWeb工程项目；（[如何快速搭建工程?](http://git.oschina.net/suninformation/ymate-platform-v2/wikis/Quickstart_New)）

- 将工程项目的 `web.xml` 中配置的 `filter` 过滤器类调整为 `net.ymate.module.webproxy.support.DispatchProxyFilter` 即可，完整配置如下：


    <?xml version="1.0" encoding="UTF-8"?>
    	<web-app id="WebApp_ID" version="2.5" xmlns="http://java.sun.com/xml/ns/javaee"
    	         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    	         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">
    	
    	    <listener>
    	        <listener-class>net.ymate.platform.webmvc.support.WebAppEventListener</listener-class>
    	    </listener>
    	
    	    <filter>
    	        <filter-name>DispatchFilter</filter-name>
    	        <filter-class>net.ymate.module.webproxy.support.DispatchProxyFilter</filter-class>
    	    </filter>
    	    <filter-mapping>
    	        <filter-name>DispatchFilter</filter-name>
    	        <url-pattern>/*</url-pattern>
    	        <dispatcher>REQUEST</dispatcher>
    	        <dispatcher>FORWARD</dispatcher>
    	    </filter-mapping>
    	    
    	    <welcome-file-list>
    	        <welcome-file>index.html</welcome-file>
    	        <welcome-file>index.jsp</welcome-file>
    	    </welcome-file-list>
    	</web-app>

### 模块配置参数说明

    #-------------------------------------
    # module.webproxy 模块初始化参数
    #-------------------------------------
    
    # 代理服务基准URL路径, 此项必填, 必须以'http://'或'https://'开始并以'/'结束, 如: http://www.ymate.net/proxies/
    ymp.configs.module.webproxy.service_base_url=
    
    # 请求路径前缀(仅透传转发以此为前缀的请求)，可选参数，默认值为空
    ymp.configs.module.webproxy.service_request_prefix=
    
    # 是否开启代理模式, 默认值: false
    ymp.configs.module.webproxy.use_proxy=
    
    # 代理类型, 取值范围[HTTP|DIRECT|SOCKS], 默认值: HTTP
    ymp.configs.module.webproxy.proxy_type=
    
    # 代理主机域名或IP地址, 开启代理模式时该项必填
    ymp.configs.module.webproxy.proxy_host=
    
    # 代理主机端口号, 默认值: 80
    ymp.configs.module.webproxy.proxy_port=
    
    # 连接超时时间(毫秒), 默认值: 0
    ymp.configs.module.webproxy.connect_timeout=
    
    # 数据读超时时间(毫秒), 默认值: 0
    ymp.configs.module.webproxy.read_timeout=

#### 启动成功日志

    2017/04/01 11:44:05:280 CST [INFO] YMP - 
    __   ____  __ ____          ____  
    \ \ / /  \/  |  _ \  __   _|___ \ 
     \ V /| |\/| | |_) | \ \ / / __) |
      | | | |  | |  __/   \ V / / __/ 
      |_| |_|  |_|_|       \_/ |_____|  Website: http://www.ymate.net/
    2017/04/01 11:44:05:287 CST [INFO] YMP - Initializing ymate-platform-core-2.0.0-Release build-20170316-0850 - debug:true
    2017/04/01 11:44:05:455 CST [INFO] Logs - Initializing ymate-platform-log-2.0.0-Release build-20170316-0850
    2017/04/01 11:44:05:456 CST [INFO] Cfgs - Initializing ymate-platform-configuration-2.0.0-Release build-20170316-0850
    2017/04/01 11:44:05:457 CST [INFO] Cfgs - -->  CONFIG_HOME: /Users/xxxx/projects/webproxy/target/webproxy/WEB-INF
    2017/04/01 11:44:05:457 CST [INFO] Cfgs - -->    USER_HOME: /Users/xxxx
    2017/04/01 11:44:05:457 CST [INFO] Cfgs - -->     USER_DIR: /Users/xxxx/projects/webproxy/target/webproxy/WEB-INF
    2017/04/01 11:44:05:946 CST [INFO] WebMVC - Initializing ymate-platform-webmvc-2.0.0-Release build-20170317-2348
    2017/04/01 11:44:05:954 CST [INFO] WebProxy - Initializing ymate-module-webproxy-1.0.0-Alphal build-20170401-1059
    2017/04/01 11:44:05:955 CST [INFO] WebProxy - -->          service_base_url: http://xxxx.xx/api
    2017/04/01 11:44:05:955 CST [INFO] WebProxy - -->            request_prefix: none
    2017/04/01 11:44:05:955 CST [INFO] WebProxy - -->                     proxy: none
    2017/04/01 11:44:05:955 CST [INFO] WebProxy - -->                use_caches: false
    2017/04/01 11:44:05:955 CST [INFO] WebProxy - --> instance_follow_redirects: false
    2017/04/01 11:44:05:956 CST [INFO] WebProxy - -->        connection_timeout: 0
    2017/04/01 11:44:05:956 CST [INFO] WebProxy - -->              read_timeout: 0
    2017/04/01 11:44:05:956 CST [INFO] Caches - Initializing ymate-platform-cache-2.0.0-Release build-20170316-0850
    2017/04/01 11:44:06:165 CST [INFO] Validations - Initializing ymate-platform-validation-2.0.0-Release build-20170316-0850
    2017/04/01 11:44:06:183 CST [INFO] YMP - Initialization completed, Total time: 896ms
    [2017-04-01 11:44:06,311] Artifact ymcms-webproxy:war exploded: Artifact is deployed successfully
    [2017-04-01 11:44:06,311] Artifact ymcms-webproxy:war exploded: Deploy took 3,728 milliseconds
    四月 01, 2017 11:44:12 上午 org.apache.catalina.startup.HostConfig deployDirectory
    信息: Deploying web application directory /Users/xxxx/Java/apache-tomcat-7.0.54/webapps/manager
    四月 01, 2017 11:44:12 上午 org.apache.catalina.startup.HostConfig deployDirectory
    信息: Deployment of web application directory /Users/xxxx/Java/apache-tomcat-7.0.54/webapps/manager has finished in 611 ms

接下来，使用浏览器访问你本地的服务（如：`http://localhost:8080/xxx/xxx/xxx`），请求将被转发至 `service_base_url` 配置的URL地址！