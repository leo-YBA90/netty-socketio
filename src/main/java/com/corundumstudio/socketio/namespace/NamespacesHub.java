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
package com.corundumstudio.socketio.namespace;

import io.netty.util.internal.PlatformDependent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.misc.CompositeIterable;

/**
 * namespace管理中心
 */
public class NamespacesHub {

    /** 通过key存储namespace的map */
    private final ConcurrentMap<String, SocketIONamespace> namespaces = PlatformDependent.newConcurrentHashMap();
    private final Configuration configuration;

    public NamespacesHub(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 根据name获取#{@link Namespace}
     * @param name
     * @return
     */
    public Namespace create(String name) {
        Namespace namespace = (Namespace) namespaces.get(name);
        if (namespace == null) {
            namespace = new Namespace(name, configuration);
            Namespace oldNamespace = (Namespace) namespaces.putIfAbsent(name, namespace);
            if (oldNamespace != null) {
                namespace = oldNamespace;
            }
        }
        return namespace;
    }

    /**
     * 根据room名称获取#{@link SocketIOClient} 迭代器
     * @param room
     * @return
     */
    public Iterable<SocketIOClient> getRoomClients(String room) {
        List<Iterable<SocketIOClient>> allClients = new ArrayList<Iterable<SocketIOClient>>();
        for (SocketIONamespace namespace : namespaces.values()) {
            Iterable<SocketIOClient> clients = ((Namespace)namespace).getRoomClients(room);
            allClients.add(clients);
        }
        return new CompositeIterable<SocketIOClient>(allClients);
    }

    /**
     * 根据name获取#{@link Namespace}
     * @param name
     * @return
     */
    public Namespace get(String name) {
        return (Namespace) namespaces.get(name);
    }

    /**
     * 移除#{@link SocketIONamespace} 并关闭连接
     * @param name
     */
    public void remove(String name) {
        SocketIONamespace namespace = namespaces.remove(name);
        if (namespace != null) {
            namespace.getBroadcastOperations().disconnect();
        }
    }

    /**
     * 获取所有注册的 #{@link SocketIONamespace}
     * @return
     */
    public Collection<SocketIONamespace> getAllNamespaces() {
        return namespaces.values();
    }

}
