package com.xxl.job.executor.service.jobhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 启动
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    static ClassPathXmlApplicationContext context;

    public static void main(String[] args) {

        context = new ClassPathXmlApplicationContext("applicationcontext-xxl-job.xml");
        logger.debug("start-------------------------");

        try {
            context.start();
            synchronized (Application.class)
            {
                while (true)
                {
                    Application.class.wait();
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
