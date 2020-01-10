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


/**
 * ack回调基础类。
 *
 * Base ack callback class.
 *
 * 通过回调方法通知从客户端接收到的确认信息。
 *
 * Notifies about acknowledgement received from client
 * via {@link #onSuccess} callback method.
 *
 * 默认情况下，当SocketIOClient是活动的时候，它可以等待来自客户机的确认。可以将timeout定义为构造函数参数。
 *
 * By default it may wait acknowledgement from client
 * while {@link SocketIOClient} is alive. Timeout can be
 * defined {@link #timeout} as constructor argument.
 *
 * 如果执行了onSuccess或onTimeout，则此对象不再是实际对象。
 *
 * This object is NOT actual anymore if {@link #onSuccess} or
 * {@link #onTimeout} was executed.
 *
 * @param <T> - any serializable type
 *
 * @see com.corundumstudio.socketio.VoidAckCallback
 * @see com.corundumstudio.socketio.MultiTypeAckCallback
 *
 */
public abstract class AckCallback<T> {

    protected final Class<T> resultClass;
    protected final int timeout;

    /**
     * Create AckCallback
     *
     * @param resultClass - result class
     */
    public AckCallback(Class<T> resultClass) {
        this(resultClass, -1);
    }

    /**
     * Creates AckCallback with timeout
     *
     * @param resultClass - result class
     * @param timeout - callback timeout in seconds
     */
    public AckCallback(Class<T> resultClass, int timeout) {
        this.resultClass = resultClass;
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    /**
     * Executes only once when acknowledgement received from client.
     *
     * @param result - object sended by client
     */
    public abstract void onSuccess(T result);

    /**
     * Invoked only once then <code>timeout</code> defined
     *
     */
    public void onTimeout() {

    }

    /**
     * Returns class of argument in {@link #onSuccess} method
     *
     * @return - result class
     */
    public Class<T> getResultClass() {
        return resultClass;
    }

}
