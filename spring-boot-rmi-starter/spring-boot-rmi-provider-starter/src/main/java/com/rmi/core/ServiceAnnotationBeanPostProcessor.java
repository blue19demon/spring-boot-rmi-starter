package com.rmi.core;
import java.rmi.RemoteException;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.remoting.rmi.RmiServiceExporter;

import com.rmi.anno.RemoteService;
import com.rmi.anno.RmiServiceProperty;


public class ServiceAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements PriorityOrdered {

    private int order = Ordered.LOWEST_PRECEDENCE - 1;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RemoteService service = AnnotationUtils.findAnnotation(bean.getClass(), RemoteService.class);
        Object resultBean = bean;
        if (null != service) {
                RmiServiceExporter rmiServiceExporter = new RmiServiceExporter();
                rmiServiceExporter.setServiceInterface(service.serviceInterface());
                rmiServiceExporter.setService(bean);
                RmiServiceProperty rmiServiceProperty = bean.getClass().getAnnotation(RmiServiceProperty.class);
                if (rmiServiceProperty != null) {
                    rmiServiceExporter.setRegistryPort(rmiServiceProperty.registryPort());
                }
                String serviceName = service.serviceInterface().getSimpleName();
                if (serviceName.startsWith("/")) {
                    serviceName = serviceName.substring(1);
                }
                rmiServiceExporter.setServiceName(serviceName);
                try {
                    rmiServiceExporter.afterPropertiesSet();
                } catch (RemoteException remoteException) {
                    throw new FatalBeanException("Exception initializing RmiServiceExporter", remoteException);
                }
                resultBean = rmiServiceExporter;
            }
        return resultBean;
    }

    @Override
    public int getOrder() {
        return order;
    }
}