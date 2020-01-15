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
package com.corundumstudio.socketio.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.handler.SocketIOException;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.namespace.Namespace;

/**
 * 断开连接时候调用，以及方法验证
 */
public class OnDisconnectScanner implements AnnotationScanner {

    @Override
    public Class<? extends Annotation> getScanAnnotation() {
        return OnDisconnect.class;
    }
    /**
     * 在命名空间中增加回调方法，当断开事件发生的时候反射此函数
     * @param namespace 命名空间
     * @param object    对象
     * @param method    调用对象的函数
     * @param annotation 注解
     */
    @Override
    public void addListener(Namespace namespace, final Object object, final Method method, Annotation annotation) {
        namespace.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                try {
                    method.invoke(object, client);
                } catch (InvocationTargetException e) {
                    throw new SocketIOException(e.getCause());
                } catch (Exception e) {
                    throw new SocketIOException(e);
                }
            }
        });
    }

    /**
     * 验证方法的参数中是否包含SocketIOClient
     * @param method
     * @param clazz
     */
    @Override
    public void validate(Method method, Class<?> clazz) {
        if (method.getParameterTypes().length != 1) {
            throw new IllegalArgumentException("Wrong OnDisconnect listener signature: " + clazz + "." + method.getName());
        }
        boolean valid = false;
        for (Class<?> eventType : method.getParameterTypes()) {
            if (eventType.equals(SocketIOClient.class)) {
                valid = true;
            }
        }
        if (!valid) {
            throw new IllegalArgumentException("Wrong OnDisconnect listener signature: " + clazz + "." + method.getName());
        }
    }

}
