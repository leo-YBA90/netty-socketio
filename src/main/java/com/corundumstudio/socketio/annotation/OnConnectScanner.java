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
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.namespace.Namespace;

/**
 * 验证onConnect注解函数是否符合要求，及onConnect发生时回调此方法
 */
public class OnConnectScanner implements AnnotationScanner  {

    /**
     * 获取OnConnect.class
     * @return  OnConnect.class
     */
    @Override
    public Class<? extends Annotation> getScanAnnotation() {
        return OnConnect.class;
    }

    /**
     * 在命名空间中增加回调方法，当连接事件发生的时候反射此函数
     * @param namespace 命名空间
     * @param object    对象
     * @param method    调用对象的函数
     * @param annotation 注解
     */
    @Override
    public void addListener(Namespace namespace, final Object object, final Method method, Annotation annotation) {
        namespace.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
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
     * @param method    回调方法
     * @param clazz     回调class
     */
    @Override
    public void validate(Method method, Class<?> clazz) {
        // 方法必须包含参数
        if (method.getParameterTypes().length != 1) {
            throw new IllegalArgumentException("Wrong OnConnect listener signature: " + clazz + "." + method.getName());
        }
        boolean valid = false;
        // 只要参数中不包含SocketIOClient则抛出异常
        for (Class<?> eventType : method.getParameterTypes()) {
            if (eventType.equals(SocketIOClient.class)) {
                valid = true;
            }
        }
        if (!valid) {
            throw new IllegalArgumentException("Wrong OnConnect listener signature: " + clazz + "." + method.getName());
        }
    }

}
