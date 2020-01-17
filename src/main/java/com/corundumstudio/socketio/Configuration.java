/**
 * Copyright (c) 2012-2019 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.corundumstudio.socketio;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.corundumstudio.socketio.handler.SuccessAuthorizationListener;
import com.corundumstudio.socketio.listener.DefaultExceptionListener;
import com.corundumstudio.socketio.listener.ExceptionListener;
import com.corundumstudio.socketio.protocol.JsonSupport;
import com.corundumstudio.socketio.store.MemoryStoreFactory;
import com.corundumstudio.socketio.store.StoreFactory;

import javax.net.ssl.KeyManagerFactory;

/**
 * 配置信息
 */
public class Configuration {

    /** 错误listener */
    private ExceptionListener exceptionListener = new DefaultExceptionListener();

    private String context = "/socket.io";

    /**
     * 把Transport枚举类组装成list
     */
    private List<Transport> transports = Arrays.asList(Transport.WEBSOCKET, Transport.POLLING);

    /** worker线程和boss线程的数量 */
    private int bossThreads = 0; // 0 = current_processors_amount * 2
    private int workerThreads = 0; // 0 = current_processors_amount * 2
    /** 是否使用linux的epoll */
    private boolean useLinuxNativeEpoll;

    /** 是否允许自定义请求 */
    private boolean allowCustomRequests = false;

    /** 升级超时时间 */
    private int upgradeTimeout = 10000;
    /** ping超时时间 */
    private int pingTimeout = 60000;
    /** ping时间间隔 */
    private int pingInterval = 25000;
    /** 第一个数据超时时间 */
    private int firstDataTimeout = 5000;
    /** 最大Http内容长度 */
    private int maxHttpContentLength = 64 * 1024;
    /** 最大帧长度 */
    private int maxFramePayloadLength = 64 * 1024;
    /** 包前缀 */
    private String packagePrefix;
    /** 主机名 */
    private String hostname;
    private int port = -1;
    /** ssl协议 */
    private String sslProtocol = "TLSv1";

    /** 密钥存储格式 */
    private String keyStoreFormat = "JKS";
    /** 秘钥输入流 */
    private InputStream keyStore;
    /** 秘钥文件密码 */
    private String keyStorePassword;
    /** 可信任的存储格式 */
    private String trustStoreFormat = "JKS";
    /** 可信任的存储输入流 */
    private InputStream trustStore;
    /** 可信任的存储密码 */
    private String trustStorePassword;

    /** 秘钥工厂算法 */
    private String keyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

    /** 是否使用直接缓冲区 */
    private boolean preferDirectBuffer = true;

    /** TCP套接字配置 */
    private SocketConfig socketConfig = new SocketConfig();
    /** 默认用内存来保存sessionId和namespace */
    private StoreFactory storeFactory = new MemoryStoreFactory();

    private JsonSupport jsonSupport;
    /** 授权listener，默认成功 */
    private AuthorizationListener authorizationListener = new SuccessAuthorizationListener();
    /** 默认成功后才应答 */
    private AckMode ackMode = AckMode.AUTO_SUCCESS_ONLY;
    /** 添加版本头 */
    private boolean addVersionHeader = true;

    private String origin;
    /** http压缩 */
    private boolean httpCompression = true;
    /** websocket 压缩 */
    private boolean websocketCompression = true;
    /** 随机session */
    private boolean randomSession = false;

    public Configuration() {
    }

    /**
     * Defend from further modifications by cloning
     *
     * @param conf - Configuration object to clone
     */
    Configuration(Configuration conf) {
        setBossThreads(conf.getBossThreads());
        setWorkerThreads(conf.getWorkerThreads());
        setUseLinuxNativeEpoll(conf.isUseLinuxNativeEpoll());

        setPingInterval(conf.getPingInterval());
        setPingTimeout(conf.getPingTimeout());

        setHostname(conf.getHostname());
        setPort(conf.getPort());

        if (conf.getJsonSupport() == null) {
            try {
                getClass().getClassLoader().loadClass("com.fasterxml.jackson.databind.ObjectMapper");
                try {
                    Class<?> jjs = getClass().getClassLoader().loadClass("com.corundumstudio.socketio.protocol.JacksonJsonSupport");
                    JsonSupport js = (JsonSupport) jjs.getConstructor().newInstance();
                    conf.setJsonSupport(js);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Can't find jackson lib in classpath", e);
            }
        }

        setJsonSupport(new JsonSupportWrapper(conf.getJsonSupport()));
        setContext(conf.getContext());
        setAllowCustomRequests(conf.isAllowCustomRequests());

        setKeyStorePassword(conf.getKeyStorePassword());
        setKeyStore(conf.getKeyStore());
        setKeyStoreFormat(conf.getKeyStoreFormat());
        setTrustStore(conf.getTrustStore());
        setTrustStoreFormat(conf.getTrustStoreFormat());
        setTrustStorePassword(conf.getTrustStorePassword());
        setKeyManagerFactoryAlgorithm(conf.getKeyManagerFactoryAlgorithm());

        setTransports(conf.getTransports().toArray(new Transport[conf.getTransports().size()]));
        setMaxHttpContentLength(conf.getMaxHttpContentLength());
        setPackagePrefix(conf.getPackagePrefix());

        setPreferDirectBuffer(conf.isPreferDirectBuffer());
        setStoreFactory(conf.getStoreFactory());
        setAuthorizationListener(conf.getAuthorizationListener());
        setExceptionListener(conf.getExceptionListener());
        setSocketConfig(conf.getSocketConfig());
        setAckMode(conf.getAckMode());
        setMaxFramePayloadLength(conf.getMaxFramePayloadLength());
        setUpgradeTimeout(conf.getUpgradeTimeout());

        setAddVersionHeader(conf.isAddVersionHeader());
        setOrigin(conf.getOrigin());
        setSSLProtocol(conf.getSSLProtocol());

        setHttpCompression(conf.isHttpCompression());
        setWebsocketCompression(conf.isWebsocketCompression());
        setRandomSession(conf.randomSession);
    }

    public JsonSupport getJsonSupport() {
        return jsonSupport;
    }

    /**
     * Allows to setup custom implementation of
     * JSON serialization/deserialization
     *
     * @param jsonSupport - json mapper
     *
     * @see JsonSupport
     */
    public void setJsonSupport(JsonSupport jsonSupport) {
        this.jsonSupport = jsonSupport;
    }

    public String getHostname() {
        return hostname;
    }

    /**
     * Optional parameter. If not set then bind address
     * will be 0.0.0.0 or ::0
     *
     * @param hostname - name of host
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public int getBossThreads() {
        return bossThreads;
    }
    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }
    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    /**
     * Ping interval
     *
     * @param heartbeatIntervalSecs - time in milliseconds
     */
    public void setPingInterval(int heartbeatIntervalSecs) {
        this.pingInterval = heartbeatIntervalSecs;
    }
    public int getPingInterval() {
        return pingInterval;
    }

    /**
     * Ping timeout
     * Use <code>0</code> to disable it
     *
     * @param heartbeatTimeoutSecs - time in milliseconds
     */
    public void setPingTimeout(int heartbeatTimeoutSecs) {
        this.pingTimeout = heartbeatTimeoutSecs;
    }
    public int getPingTimeout() {
        return pingTimeout;
    }
    public boolean isHeartbeatsEnabled() {
        return pingTimeout > 0;
    }

    public String getContext() {
        return context;
    }
    public void setContext(String context) {
        this.context = context;
    }

    public boolean isAllowCustomRequests() {
        return allowCustomRequests;
    }

    /**
     * 允许服务自定义请求不同于 socket.io 的协议。
     * 在这种情况下，需要添加自己的处理程序来处理它们，以避免连接挂起。
     * 默认为{@code false}
     *
     * Allow to service custom requests differs from socket.io protocol.
     * In this case it's necessary to add own handler which handle them
     * to avoid hang connections.
     * Default is {@code false}
     *
     * @param allowCustomRequests - {@code true} to allow
     */
    public void setAllowCustomRequests(boolean allowCustomRequests) {
        this.allowCustomRequests = allowCustomRequests;
    }

    /**
     * SSL key store password
     *
     * @param keyStorePassword - password of key store
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * SSL key store stream, maybe appointed to any source
     *
     * @param keyStore - key store input stream
     */
    public void setKeyStore(InputStream keyStore) {
        this.keyStore = keyStore;
    }
    public InputStream getKeyStore() {
        return keyStore;
    }

    /**
     * Key store format
     *
     * @param keyStoreFormat - key store format
     */
    public void setKeyStoreFormat(String keyStoreFormat) {
        this.keyStoreFormat = keyStoreFormat;
    }
    public String getKeyStoreFormat() {
        return keyStoreFormat;
    }

    /**
     * Set maximum http content length limit
     *
     * @param value
     *        the maximum length of the aggregated http content.
     */
    public void setMaxHttpContentLength(int value) {
        this.maxHttpContentLength = value;
    }
    public int getMaxHttpContentLength() {
        return maxHttpContentLength;
    }

    /**
     * Transports supported by server
     *
     * @param transports - list of transports
     */
    public void setTransports(Transport ... transports) {
        if (transports.length == 0) {
            throw new IllegalArgumentException("Transports list can't be empty");
        }
        this.transports = Arrays.asList(transports);
    }
    public List<Transport> getTransports() {
        return transports;
    }

    /**
     * 包前缀，用于从客户端发送不包含完整类名的json-object。
     *
     * Package prefix for sending json-object from client
     * without full class name.
     *
     * 使用已定义的包前缀socket.io客户端只需要在json对象中定义'@class: 'SomeType "而不是'@class: 'com.full.package.name.SomeType "
     *
     * With defined package prefix socket.io client
     * just need to define '@class: 'SomeType'' in json object
     * instead of '@class: 'com.full.package.name.SomeType''
     *
     * @param packagePrefix - prefix string
     *
     */
    public void setPackagePrefix(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }
    public String getPackagePrefix() {
        return packagePrefix;
    }

    /**
     * 包编码过程中使用的缓冲区分配方法。
     *
     * Buffer allocation method used during packet encoding.
     * Default is {@code true}
     *
     * @param preferDirectBuffer    {@code true} if a direct buffer should be tried to be used as target for
     *                              the encoded messages. If {@code false} is used it will allocate a heap
     *                              buffer, which is backed by an byte array.
     */
    public void setPreferDirectBuffer(boolean preferDirectBuffer) {
        this.preferDirectBuffer = preferDirectBuffer;
    }
    public boolean isPreferDirectBuffer() {
        return preferDirectBuffer;
    }

    /**
     * 用于存储会话数据并实现分布式pubsub。
     *
     * Data store - used to store session data and implements distributed pubsub.
     * Default is {@code MemoryStoreFactory}
     *
     * @param clientStoreFactory - implements StoreFactory
     *
     * @see com.corundumstudio.socketio.store.MemoryStoreFactory
     * @see com.corundumstudio.socketio.store.RedissonStoreFactory
     * @see com.corundumstudio.socketio.store.HazelcastStoreFactory
     */
    public void setStoreFactory(StoreFactory clientStoreFactory) {
        this.storeFactory = clientStoreFactory;
    }
    public StoreFactory getStoreFactory() {
        return storeFactory;
    }

    /**
     * 在每次握手时调用授权侦听器。
     * 通过{@code AuthorizationListener.isAuthorized}方法接受或拒绝客户。
     * 默认接受所有客户端。
     *
     * Authorization listener invoked on every handshake.
     * Accepts or denies a client by {@code AuthorizationListener.isAuthorized} method.
     * <b>Accepts</b> all clients by default.
     *
     * @param authorizationListener - authorization listener itself
     *
     * @see com.corundumstudio.socketio.AuthorizationListener
     */
    public void setAuthorizationListener(AuthorizationListener authorizationListener) {
        this.authorizationListener = authorizationListener;
    }
    public AuthorizationListener getAuthorizationListener() {
        return authorizationListener;
    }

    /**
     * Exception listener invoked on any exception in
     * SocketIO listener
     *
     * @param exceptionListener - listener
     *
     * @see com.corundumstudio.socketio.listener.ExceptionListener
     */
    public void setExceptionListener(ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
    }
    public ExceptionListener getExceptionListener() {
        return exceptionListener;
    }

    public SocketConfig getSocketConfig() {
        return socketConfig;
    }
    /**
     * TCP socket configuration
     *
     * @param socketConfig - config
     */
    public void setSocketConfig(SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
    }

    /**
     * Auto ack-response mode
     * Default is {@code AckMode.AUTO_SUCCESS_ONLY}
     *
     * @see AckMode
     *
     * @param ackMode - ack mode
     */
    public void setAckMode(AckMode ackMode) {
        this.ackMode = ackMode;
    }
    public AckMode getAckMode() {
        return ackMode;
    }


    public String getTrustStoreFormat() {
        return trustStoreFormat;
    }
    public void setTrustStoreFormat(String trustStoreFormat) {
        this.trustStoreFormat = trustStoreFormat;
    }

    public InputStream getTrustStore() {
        return trustStore;
    }
    public void setTrustStore(InputStream trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyManagerFactoryAlgorithm() {
        return keyManagerFactoryAlgorithm;
    }
    public void setKeyManagerFactoryAlgorithm(String keyManagerFactoryAlgorithm) {
        this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm;
    }


    /**
     * Set maximum websocket frame content length limit
     *
     * @param maxFramePayloadLength - length
     */
    public void setMaxFramePayloadLength(int maxFramePayloadLength) {
        this.maxFramePayloadLength = maxFramePayloadLength;
    }
    public int getMaxFramePayloadLength() {
        return maxFramePayloadLength;
    }

    /**
     * Transport upgrade timeout in milliseconds
     *
     * @param upgradeTimeout - upgrade timeout
     */
    public void setUpgradeTimeout(int upgradeTimeout) {
        this.upgradeTimeout = upgradeTimeout;
    }
    public int getUpgradeTimeout() {
        return upgradeTimeout;
    }

    /**
     * Adds <b>Server</b> header with lib version to http response.
     * <p>
     * Default is <code>true</code>
     *
     * @param addVersionHeader - <code>true</code> to add header
     */
    public void setAddVersionHeader(boolean addVersionHeader) {
        this.addVersionHeader = addVersionHeader;
    }
    public boolean isAddVersionHeader() {
        return addVersionHeader;
    }

    /**
     * Set <b>Access-Control-Allow-Origin</b> header value for http each
     * response.
     * Default is <code>null</code>
     *
     * If value is <code>null</code> then request <b>ORIGIN</b> header value used.
     *
     * @param origin - origin
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    public String getOrigin() {
        return origin;
    }

    public boolean isUseLinuxNativeEpoll() {
        return useLinuxNativeEpoll;
    }
    public void setUseLinuxNativeEpoll(boolean useLinuxNativeEpoll) {
        this.useLinuxNativeEpoll = useLinuxNativeEpoll;
    }

    /**
     * Set the name of the requested SSL protocol
     *
     * @param sslProtocol - name of protocol
     */
    public void setSSLProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
    public String getSSLProtocol() {
        return sslProtocol;
    }

    /**
     * 通道打开和首次数据传输之间的超时有助于避免“静默通道”攻击，并防止在这种情况下出现“打开太多文件”的问题
     *
     * Timeout between channel opening and first data transfer
     * Helps to avoid 'silent channel' attack and prevents
     * 'Too many open files' problem in this case
     *
     * @param firstDataTimeout - timeout value
     */
    public void setFirstDataTimeout(int firstDataTimeout) {
        this.firstDataTimeout = firstDataTimeout;
    }
    public int getFirstDataTimeout() {
        return firstDataTimeout;
    }

    /**
     * Activate http protocol compression. Uses {@code gzip} or
     * {@code deflate} encoding choice depends on the {@code "Accept-Encoding"} header value.
     * <p>
     * Default is <code>true</code>
     *
     * @param httpCompression - <code>true</code> to use http compression
     */
    public void setHttpCompression(boolean httpCompression) {
        this.httpCompression = httpCompression;
    }
    public boolean isHttpCompression() {
        return httpCompression;
    }

    /**
     * Activate websocket protocol compression.
     * Uses {@code permessage-deflate} encoding only.
     * <p>
     * Default is <code>true</code>
     *
     * @param websocketCompression - <code>true</code> to use websocket compression
     */
    public void setWebsocketCompression(boolean websocketCompression) {
        this.websocketCompression = websocketCompression;
    }
    public boolean isWebsocketCompression() {
        return websocketCompression;
    }

    public boolean isRandomSession() {
        return randomSession;
    }

    public void setRandomSession(boolean randomSession) {
        this.randomSession = randomSession;
    }
}
