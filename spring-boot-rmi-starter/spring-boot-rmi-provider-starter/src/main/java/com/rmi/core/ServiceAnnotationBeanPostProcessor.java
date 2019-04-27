package com.rmi.core;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.remoting.rmi.RmiServiceExporter;

import com.rmi.anno.RemoteService;


public class ServiceAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements PriorityOrdered {

    private int order = Ordered.LOWEST_PRECEDENCE - 1;
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RemoteService remoteService = AnnotationUtils.findAnnotation(bean.getClass(), RemoteService.class);
        Object resultBean = bean;
        if (null != remoteService) {
                Class<?> service = bean.getClass();
                RmiServiceExporter rmiServiceExporter = new RmiServiceExporter();
                rmiServiceExporter.setServiceInterface(service.getInterfaces()[0]);
                rmiServiceExporter.setService(bean);
                Integer rmiPort=(Integer) PropertiesUtils.getCommonYml("rmi.port");
                System.out.println("---------rmiPort>>>>>>>>>>>>>>>>>"+rmiPort);
                if(rmiPort==null) {
                	rmiPort = Registry.REGISTRY_PORT;
                }
                rmiServiceExporter.setRegistryPort(rmiPort);
                String serviceName = service.getInterfaces()[0].getSimpleName();
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