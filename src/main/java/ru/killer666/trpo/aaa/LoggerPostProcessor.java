package ru.killer666.trpo.aaa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

@Component
public class LoggerPostProcessor implements BeanPostProcessor {
    private static Logger logger = LoggerFactory.getLogger(LoggerPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        List<Field> fields = Arrays.asList(bean.getClass().getDeclaredFields());

        for (Field field : fields) {
            if (Logger.class.isAssignableFrom(field.getType()) && field.getAnnotation(InjectLogger.class) != null) {
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    field.setAccessible(true);

                    try {
                        field.set(bean, LoggerFactory.getLogger(bean.getClass()));
                        logger.debug("Successfully injected a SLF4J logger on bean: " + bean.getClass());
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        logger.warn("Could not inject logger for class: " + bean.getClass(), e);
                    }
                }
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}