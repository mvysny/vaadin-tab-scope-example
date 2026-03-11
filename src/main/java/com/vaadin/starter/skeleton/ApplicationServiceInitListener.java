package com.vaadin.starter.skeleton;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationServiceInitListener
        implements VaadinServiceInitListener {

    static final AtomicInteger counter = new AtomicInteger();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        TabScope.setup(ts -> {
            Objects.requireNonNull(TabScope.getCurrent()); // this should work as well.
            if (ts.getValues().getAttribute("hello") != null) {
                throw new IllegalStateException("This is unexpected - we're already initialized but we shouldn't be!");
            }
            ts.getValues().setAttribute("hello", counter.incrementAndGet());
            System.out.println("TabScope created: " + ts);
            ts.addDestroyListener(e -> System.out.println("TabScope destroyed: " + e));
        });
    }
}
