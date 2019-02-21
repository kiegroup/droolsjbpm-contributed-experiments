package org.kiegroup.zenithr.rest;

import org.kiegroup.zenithr.drools.SessionFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationInitialization implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.setProperty("GET", System.getenv("GET"));
        SessionFactory.getInstance();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //Nothing to do
    }
}
