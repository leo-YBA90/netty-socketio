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

import java.net.SocketAddress;
import java.util.Set;
import java.util.UUID;

import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.Store;


/**
 * Fully thread-safe.
 *
 */
public interface SocketIOClient extends ClientOperations, Store {

    /**
     * 客户端连接期间使用的握手数据#{@link HandshakeData}
     *
     * Handshake data used during client connection
     *
     * @return HandshakeData
     */
    HandshakeData getHandshakeData();

    /**
     * 当前客户端传输协议
     *
     * Current client transport protocol
     *
     * @return transport protocol
     */
    Transport getTransport();

    /**
     * 发送事件与ack回调
     *
     * Send event with ack callback
     *
     * @param name - event name
     * @param data - event data
     * @param ackCallback - ack callback
     */
    void sendEvent(String name, AckCallback<?> ackCallback, Object ... data);

    /**
     * 发送packet与ack回调
     *
     * Send packet with ack callback
     *
     * @param packet - packet to send
     * @param ackCallback - ack callback
     */
    void send(Packet packet, AckCallback<?> ackCallback);

    /**
     *
     * client的namespace
     *
     * Client namespace
     *
     * @return - namespace
     */
    SocketIONamespace getNamespace();

    /**
     * Client session id, uses {@link UUID} object
     *
     * @return - session id
     */
    UUID getSessionId();

    /**
     * Get client remote address
     *
     * @return remote address
     */
    SocketAddress getRemoteAddress();

    /**
     * Check is underlying channel open
     *
     * @return <code>true</code> if channel open, otherwise <code>false</code>
     */
    boolean isChannelOpen();

    /**
     * Join client to room
     *
     * @param room - name of room
     */
    void joinRoom(String room);

    /**
     * Join client to room
     *
     * @param room - name of room
     */
    void leaveRoom(String room);

    /**
     * 获取client加入的room
     *
     * Get all rooms a client is joined in.
     *
     * @return name of rooms
     */
    Set<String> getAllRooms();

}
