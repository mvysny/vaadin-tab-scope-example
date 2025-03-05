package com.vaadin.starter.skeleton;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationServiceInitListener
        implements VaadinServiceInitListener {

    private static final AtomicInteger counter = new AtomicInteger();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(TabScope.uiInitListener(ts -> {
            ts.getValues().setAttribute("hello", counter.incrementAndGet());
        }));
    }
}
