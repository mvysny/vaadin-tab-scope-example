package com.vaadin.starter.skeleton;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.jetbrains.annotations.NotNull;

/**
 * The main layout of the app, adds navigation menu.
 */
public class MainLayout extends AppLayout {
    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Tab Scope Demo App");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        VerticalLayout nav = getSideNav();

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
        addToNavbar(toggle, title);
    }

    @NotNull
    private VerticalLayout getSideNav() {
        VerticalLayout sideNav = new VerticalLayout();
        sideNav.add(
                new RouterLink("Main View", MainView.class),
                new RouterLink("Main View (No App Layout)", MainViewNoAppLayout.class),
                new RouterLink("Tab Scoped View", TabScopedView.class),
                new RouterLink("Tab Scoped View (No App Layout)", TabScopedViewNoAppLayout.class)
        );
        return sideNav;
    }
}
