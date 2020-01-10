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

import com.corundumstudio.socketio.namespace.Namespace;

/**
 * 扫描注解功能类需要实现的接口
 */
public interface AnnotationScanner {

    /**
     * 获取注解类
     * @return
     */
    Class<? extends Annotation> getScanAnnotation();

    /**
     * 在命名空间中增加回调方法，当连接事件发生的时候调用此函数
     * @param namespace
     * @param object
     * @param method
     * @param annotation
     */
    void addListener(Namespace namespace, Object object, Method method, Annotation annotation);

    /**
     * 验证
     * @param method
     * @param clazz
     */
    void validate(Method method, Class<?> clazz);

}