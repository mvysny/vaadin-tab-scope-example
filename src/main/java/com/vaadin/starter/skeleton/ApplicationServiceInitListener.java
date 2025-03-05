package com.vaadin.starter.skeleton;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationServiceInitListener
        implements VaadinServiceInitListener {

    static final AtomicInteger counter = new AtomicInteger();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        // this tab init listener is called exactly once per browser tab
        event.getSource().addUIInitListener(TabScope.uiInitListener(ts -> {
            if (ts.getValues().getAttribute("hello") != null) {
                throw new IllegalStateException("This is unexpected - we're already initialized but we shouldn't be!");
            }
            ts.getValues().setAttribute("hello", counter.incrementAndGet());
        }));
    }
}
