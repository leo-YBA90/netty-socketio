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

public enum AckMode {

    /**
     * 在包处理过程中，对每个ack-request跳过异常自动发送ack-response
     *
     * Send ack-response automatically on each ack-request
     * <b>skip</b> exceptions during packet handling
     */
    AUTO,

    /**
     *
     * 只有在成功后才自动发送response
     *
     * Send ack-response automatically on each ack-request
     * only after <b>success</b> packet handling
     */
    AUTO_SUCCESS_ONLY,

    /**
     * 关闭自动应答ack-response。
     * 每次使用AckRequest.sendAckData发送ack-response。
     *
     * Turn off auto ack-response sending.
     * Use AckRequest.sendAckData to send ack-response each time.
     *
     */
    MANUAL

}
