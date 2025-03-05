package com.vaadin.starter.skeleton;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demoes a prototype-scoped route (new instance created on page reload), fetching a tab-scoped value.
 */
@Route(value = "tab-scoped-route", layout = MainLayout.class)
@TabScoped
public class TabScopedView extends VerticalLayout {

    static final AtomicInteger counter = new AtomicInteger();

    public TabScopedView() {
        add(new H3("Tab-Scoped Route"));
        add(new Span("This route is tab-scoped (new instance created per tab). The value below not change when the page is reloaded or navigated elsewhere, but should change when the route is opened in another browser tab."));
        add(new Span("Value: " + counter.incrementAndGet()));
    }
}
