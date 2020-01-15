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
import java.util.ArrayList;
import java.util.List;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.MultiTypeArgs;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.handler.SocketIOException;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.MultiTypeEventListener;
import com.corundumstudio.socketio.namespace.Namespace;

/**
 * OnEvent注解验证及回调逻辑
 */
public class OnEventScanner implements AnnotationScanner {

    @Override
    public Class<? extends Annotation> getScanAnnotation() {
        return OnEvent.class;
    }

    /**
     * 在命名空间中增加回调方法，当连接事件发生的时候反射此函数
     * @param namespace
     * @param object
     * @param method
     * @param annot
     */
    @Override
    @SuppressWarnings("unchecked")
    public void addListener(Namespace namespace, final Object object, final Method method, Annotation annot) {
        // 强制转换成OnEvent.class
        OnEvent annotation = (OnEvent) annot;
        // 内部的事件为空的时候，抛出异常
        if (annotation.value() == null || annotation.value().trim().length() == 0) {
            throw new IllegalArgumentException("OnEvent \"value\" parameter is required");
        }
        // SocketIOClient.class这个类在method中的第几个参数中
        final int socketIOClientIndex = paramIndex(method, SocketIOClient.class);
        // AckRequest.class这个类在method中的第几个参数中
        final int ackRequestIndex = paramIndex(method, AckRequest.class);
        // 检查参数里面不包含SocketIOClient.class和AckRequest.class的数量并把下标组装成list
        final List<Integer> dataIndexes = dataIndexes(method);

        // 如果需要传入的参数大于1
        if (dataIndexes.size() > 1) {
            // 把参数list转换成实体class list
            List<Class<?>> classes = new ArrayList<Class<?>>();
            for (int index : dataIndexes) {
                Class<?> param = method.getParameterTypes()[index];
                classes.add(param);
            }

            // 把事件监听器与时间放入namespace中，当发生对应的事件时，调用对应的事件监听器函数
            namespace.addMultiTypeEventListener(annotation.value(), new MultiTypeEventListener() {
                @Override
                public void onData(SocketIOClient client, MultiTypeArgs data, AckRequest ackSender) {
                    try {
                        // 把对应的参数组装成数组作为对象传入method中
                        Object[] args = new Object[method.getParameterTypes().length];
                        if (socketIOClientIndex != -1) {
                            args[socketIOClientIndex] = client;
                        }
                        if (ackRequestIndex != -1) {
                            args[ackRequestIndex] = ackSender;
                        }
                        int i = 0;
                        // warn 参数需要严格按照method的参数顺序，不然会出错
                        for (int index : dataIndexes) {
                            args[index] = data.get(i);
                            i++;
                        }
                        // 通过反射调用函数
                        method.invoke(object, args);
                    } catch (InvocationTargetException e) {
                        throw new SocketIOException(e.getCause());
                    } catch (Exception e) {
                        throw new SocketIOException(e);
                    }
                }
            }, classes.toArray(new Class[classes.size()]));
        } else {
            // 预先定义为void
            Class objectType = Void.class;
            // 获取第一个入参
            if (!dataIndexes.isEmpty()) {
                objectType = method.getParameterTypes()[dataIndexes.iterator().next()];
            }
            // 往命名空间中增加OnEvent的value事件，并把数据类型传入，当发生该事件时，调用该事件的listener
            namespace.addEventListener(annotation.value(), objectType, new DataListener<Object>() {
                // 事件发生时，调用此函数，并进行相应处理
                @Override
                public void onData(SocketIOClient client, Object data, AckRequest ackSender) {
                    try {
                        // 组装入参
                        Object[] args = new Object[method.getParameterTypes().length];
                        if (socketIOClientIndex != -1) {
                            args[socketIOClientIndex] = client;
                        }
                        if (ackRequestIndex != -1) {
                            args[ackRequestIndex] = ackSender;
                        }
                        if (!dataIndexes.isEmpty()) {
                            int dataIndex = dataIndexes.iterator().next();
                            args[dataIndex] = data;
                        }
                        // 反射调用此函数
                        method.invoke(object, args);
                    } catch (InvocationTargetException e) {
                        throw new SocketIOException(e.getCause());
                    } catch (Exception e) {
                        throw new SocketIOException(e);
                    }
                }
            });
        }
    }

    private List<Integer> dataIndexes(Method method) {
        List<Integer> result = new ArrayList<Integer>();
        int index = 0;
        for (Class<?> type : method.getParameterTypes()) {
            if (!type.equals(AckRequest.class) && !type.equals(SocketIOClient.class)) {
                result.add(index);
            }
            index++;
        }
        return result;
    }

    /**
     * 检查clazz在method参数中的第几位
     * @param method
     * @param clazz
     * @return
     */
    private int paramIndex(Method method, Class<?> clazz) {
        int index = 0;
        for (Class<?> type : method.getParameterTypes()) {
            if (type.equals(clazz)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * 验证方法的参数中是否包含clazz
     * @param method
     * @param clazz
     */
    @Override
    public void validate(Method method, Class<?> clazz) {
        int paramsCount = method.getParameterTypes().length;
        final int socketIOClientIndex = paramIndex(method, SocketIOClient.class);
        final int ackRequestIndex = paramIndex(method, AckRequest.class);
        List<Integer> dataIndexes = dataIndexes(method);
        paramsCount -= dataIndexes.size();
        if (socketIOClientIndex != -1) {
            paramsCount--;
        }
        if (ackRequestIndex != -1) {
            paramsCount--;
        }
        if (paramsCount != 0) {
            throw new IllegalArgumentException("Wrong OnEvent listener signature: " + clazz + "." + method.getName());
        }
    }

}
