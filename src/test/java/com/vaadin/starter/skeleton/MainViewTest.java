package com.vaadin.starter.skeleton;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ.*;
import static com.github.mvysny.kaributesting.v10.NotificationsKt.expectNotifications;
import static org.junit.jupiter.api.Assertions.*;

public class MainViewTest {
    private static Routes routes;

    @BeforeAll
    public static void createRoutes() {
        routes = new Routes().autoDiscoverViews("com.vaadin.starter.skeleton");
    }

    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
        ApplicationServiceInitListener.counter.set(0); // start all tests with a known counter value
    }

    @AfterEach
    public void teardownVaadin() {
        MockVaadin.tearDown();
    }

    @Test
    public void smokeTest() {
        _assertOne(Span.class, spec -> spec.withText("Value: 1"));
    }

    @Test
    public void scopeSurvivesPageReload() {
        final TabScope current = TabScope.getCurrent();
        assertNotNull(current);
        UI.getCurrent().getPage().reload();
        assertSame(TabScope.getCurrent(), current);
    }

    @Test
    public void pageReloadShouldPreserveTheValue() {
        final MainView mainView = _get(MainView.class);
        _assertOne(Span.class, spec -> spec.withText("Value: 1"));
        UI.getCurrent().getPage().reload();

        // check that we have a new instance of main view
        assertNotSame(_get(MainView.class), mainView);
        _assertOne(Span.class, spec -> spec.withText("Value: 1"));
    }
}
