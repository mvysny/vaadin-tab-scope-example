package com.vaadin.starter.skeleton;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Attributes;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {

    private static final AtomicInteger counter = new AtomicInteger();

    public MainView() {
        final Attributes values = TabScope.getCurrent().getValues();
        Integer value = (Integer) values.getAttribute("hello");
        if (value == null) {
            value = counter.incrementAndGet();
            values.setAttribute("hello", value);
        }
        add(new Span("Value: " + value));
    }
}
