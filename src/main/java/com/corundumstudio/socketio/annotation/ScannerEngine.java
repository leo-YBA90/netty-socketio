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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.namespace.Namespace;

/**
 * 注解扫描类，与把函数加入到listener中
 */
public class ScannerEngine {

    private static final Logger log = LoggerFactory.getLogger(ScannerEngine.class);

    /**
     * 主要扫描OnConnectScanner,OnDisconnectScanner,OnEventScanner这三个类
     */
    private static final List<? extends AnnotationScanner> annotations =
            Arrays.asList(new OnConnectScanner(), new OnDisconnectScanner(), new OnEventScanner());

    /**
     * 找到class中对应method的方法
     * @param objectClazz
     * @param method
     * @return
     */
    private Method findSimilarMethod(Class<?> objectClazz, Method method) {
        Method[] methods = objectClazz.getDeclaredMethods();
        for (Method m : methods) {
            if (isEquals(m, method)) {
                return m;
            }
        }
        return null;
    }

    public void scan(Namespace namespace, Object object, Class<?> clazz) throws IllegalArgumentException {
        Method[] methods = clazz.getDeclaredMethods();

        // 判定此 Class 对象所表示的类或接口与指定的 Class 参数所表示的类或接口是否相同，或是否是其超类或超接口。如果是则返回 true；否则返回 false。
        // 检查clazz是否为object的父类或者接口
        if (!clazz.isAssignableFrom(object.getClass())) {
            // 遍历每个method
            for (Method method : methods) {
                // 遍历每个method的注解是否是list中的注解，如果是这些注解中的一种的话，加入到namespace中，并把方法加入到监听方法中
                for (AnnotationScanner annotationScanner : annotations) {
                    Annotation ann = method.getAnnotation(annotationScanner.getScanAnnotation());
                    // 如果是注解中的一种
                    if (ann != null) {
                        // 验证method中是否包含SocketIOClient.class如果是OnEvent的话，还需要验证是否包含AckRequest.class
                        annotationScanner.validate(method, clazz);
                        // 在object中查找method
                        Method m = findSimilarMethod(object.getClass(), method);
                        if (m != null) {
                            // 如果包含的话，则加入到namespace的listener中
                            annotationScanner.addListener(namespace, object, m, ann);
                        } else {
                            log.warn("Method similar to " + method.getName() + " can't be found in " + object.getClass());
                        }
                    }
                }
            }
        } else {
            // 如果不是父类或接口的话，直接扫描类的方法，检验其中的注解
            for (Method method : methods) {
                // 遍历是否包含注解
                for (AnnotationScanner annotationScanner : annotations) {
                    Annotation ann = method.getAnnotation(annotationScanner.getScanAnnotation());
                    if (ann != null) {
                        // 验证
                        annotationScanner.validate(method, clazz);
                        makeAccessible(method);
                        // 把函数加入到listener中
                        annotationScanner.addListener(namespace, object, method, ann);
                    }
                }
            }
            // 如果有父类的话，扫描父类
            if (clazz.getSuperclass() != null) {
                scan(namespace, object, clazz.getSuperclass());
            } else if (clazz.isInterface()) {
                // 如果有接口，扫描接口
                for (Class<?> superIfc : clazz.getInterfaces()) {
                    scan(namespace, object, superIfc);
                }
            }
        }

    }

    /**
     * 比较两个method是否相等，需要比较名字，返回值，以及参数
     * @param method1
     * @param method2
     * @return
     */
    private boolean isEquals(Method method1, Method method2) {
        if (!method1.getName().equals(method2.getName())
                || !method1.getReturnType().equals(method2.getReturnType())) {
            return false;
        }

        return Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes());
    }

    /**
     * true 则指示反射的对象在使用时应该取消 Java 语言访问检查。值为 false 则指示反射的对象应该实施 Java 语言访问检查。
     * @param method
     */
    private void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

}
