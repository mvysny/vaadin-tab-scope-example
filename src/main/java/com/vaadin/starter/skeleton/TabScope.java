package com.vaadin.starter.skeleton;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.server.Attributes;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TabScope implements Serializable {
    private TabScope() {
        // prevent instantiation by the app itself.
    }
    /**
     * Holds all tab-scoped values stored by the app.
     */
    @NotNull
    private final Attributes values = new Attributes();

    /**
     * Returns a map which holds all tab-scoped values stored by the app.
     * @return a map which holds all tab-scoped values stored by the app.
     */
    @NotNull
    public Attributes getValues() {
        return values;
    }

    /**
     * Returns a map holding all tab scopes in a session.
     * @return a map, mapping {@link ExtendedClientDetails#getWindowName()} (a unique ID of a browser tab)
     * to the TabScope instance, holding all tab-scoped values.
     */
    @NotNull
    private static Map<String, TabScope> getInstances() {
        Map<String, TabScope> instances = (Map<String, TabScope>) VaadinSession.getCurrent().getAttribute("tab-scopes");
        if (instances == null) {
            instances = new HashMap<>();
            VaadinSession.getCurrent().setAttribute("tab-scopes", instances);
        }
        return instances;
    }

    /**
     *
     * @return
     */
    @NotNull
    private static Map<Integer, TabScope> getTemporaryInstanceMap() {
        Map<Integer, TabScope> instances = (Map<Integer, TabScope>) VaadinSession.getCurrent().getAttribute("temporary-tab-scopes");
        if (instances == null) {
            instances = new HashMap<>();
            VaadinSession.getCurrent().setAttribute("temporary-tab-scopes", instances);
        }
        return instances;
    }

    @NotNull
    public static TabScope getCurrent() {
        // @todo how to kill a tab scope? Steal from Vaadin Spring Plugin
        final UI ui = Objects.requireNonNull(UI.getCurrent(), "Must be called from Vaadin UI thread");

        final Map<String, TabScope> instances = getInstances();
        final ExtendedClientDetails extendedClientDetails = ui.getInternals().getExtendedClientDetails();
        if (extendedClientDetails != null) {
            final TabScope tabScope = instances.get(extendedClientDetails.getWindowName());
            if (tabScope == null) {
                throw new IllegalStateException("The TabScope instance is not available for this tab. That's an error since this shouldn't happen - the TabScope should have been created when ExtendedClientDetails were fetched.");
            }
            System.out.println("TabScope " + tabScope + " available for window " + extendedClientDetails.getWindowName() + ", returning");
            return tabScope;
        }

        // The ECD weren't fetched, so we don't know the window name. Work around this:
        // 1. Create TabScope and temporarily store it under UI.getUIId().
        // 2. Fetch ECD.
        // 3. Move TabScope from the temporary map and store it correctly under the window name.

        final Map<Integer, TabScope> temporaryInstanceMap = getTemporaryInstanceMap();
        TabScope tabScope = temporaryInstanceMap.get(ui.getUIId());
        if (tabScope != null) {
            System.out.println("TabScope " + tabScope + " available for UI  " + ui.getUIId() + ", returning");
            return tabScope;
        }
        tabScope = new TabScope();
        System.out.println("TabScope " + tabScope + " created");
        temporaryInstanceMap.put(ui.getUIId(), tabScope);
        ui.getPage().retrieveExtendedClientDetails(ecd -> {
            final TabScope tabScope1 = temporaryInstanceMap.remove(ui.getUIId());
            System.out.println("TabScope " + tabScope1 + ": moving from " + ui.getUIId() + " to " + ecd.getWindowName());
            Objects.requireNonNull(tabScope1);
            final TabScope prev = instances.put(ecd.getWindowName(), tabScope1);
            if (prev != null) {
                throw new IllegalStateException("TabScope " + prev + " already exists for window " + ecd.getWindowName());
            }
        });
        return tabScope;
    }
}
