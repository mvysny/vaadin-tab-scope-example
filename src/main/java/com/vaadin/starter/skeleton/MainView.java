package com.vaadin.starter.skeleton;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Attributes;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        final Attributes values = TabScope.getCurrent().getValues();
        Integer value = (Integer) values.getAttribute("hello");
        add(new Span("Value: " + value));
    }
}
