package com.vaadin.starter.skeleton;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.Attributes;
import com.vaadin.flow.server.UIInitListener;
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
     * Register the UI init listener which initializes the tab scope.
     * @param tabInitListener invoked when the tab scope is ready to be used. Invoked exactly once for a browser tab,
     *                        before any route or layout is created or initialized. Store any init values to {@link #getValues()}.
     * @return the UI init listener
     */
    @NotNull
    public static UIInitListener uiInitListener(@NotNull SerializableConsumer<TabScope> tabInitListener) {
        return event -> init(tabInitListener);
    }

    private static void init(@NotNull SerializableConsumer<TabScope> tabInitListener) {
        final UI ui = Objects.requireNonNull(UI.getCurrent(), "Must be called from Vaadin UI thread");
        final ExtendedClientDetails extendedClientDetails = ui.getInternals().getExtendedClientDetails();
        if (extendedClientDetails != null) {
            throw new IllegalStateException("Called too late");
        }

        // The ECD weren't fetched, so we don't know the window name. Work around this:
        // 1. Create TabScope and temporarily store it under UI.getUIId().
        // 2. Fetch ECD.
        // 3. Move TabScope from the temporary map and store it correctly under the window name.

        final TabScope tabScope = ComponentUtil.getData(ui, TabScope.class);
        if (tabScope != null) {
            throw new IllegalStateException("Tab scope already exists for this UI, which means that init() has been called multiple times. " + ui.getUIId() + ": " + tabScope);
        }
        ui.getPage().retrieveExtendedClientDetails(ecd -> {
            TabScope tabScope1 = getInstances().get(ecd.getWindowName());
            if (tabScope1 == null) {
                tabScope1 = new TabScope();
                getInstances().put(ecd.getWindowName(), tabScope1);
                tabInitListener.accept(tabScope1);
            }
        });
    }

    @NotNull
    public static TabScope getCurrent() {
        // @todo how to clean up a tab scope? Not in UI destroy listener since a new UI can spring up right away. Steal from Vaadin Spring Plugin
        final UI ui = Objects.requireNonNull(UI.getCurrent(), "Must be called from Vaadin UI thread");

        final Map<String, TabScope> instances = getInstances();
        final ExtendedClientDetails extendedClientDetails = ui.getInternals().getExtendedClientDetails();
        if (extendedClientDetails != null) {
            final TabScope tabScope = instances.get(extendedClientDetails.getWindowName());
            if (tabScope == null) {
                throw new IllegalStateException("The TabScope instance is not available for this tab. That's an error since this shouldn't happen - the TabScope should have been created when ExtendedClientDetails were fetched.");
            }
            return tabScope;
        }
        throw new IllegalStateException("Trying to retrieve TabScope too early");
    }
}
