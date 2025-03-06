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

/**
 * Stores values in a browser tab scope - all values inserted into {@link #getValues()} are preserved per browser tab.
 * The tab scope survives page reloads and navigation.
 * <br/>
 * To use this you need to:
 * <ul>
 *     <li>Call {@link #init(SerializableConsumer)} from {@link com.vaadin.flow.server.VaadinService#addUIInitListener(UIInitListener) UI Init Listener}</li>
 *     <li>Call {@link #getCurrent()} from everywhere else from your app: from your routes and layouts etc</li>
 * </ul>
 * <h3>Vaadin 8</h3>
 * This is how the Vaadin 8 UI scope used to work. When migrating, just store your values to
 * {@link #getValues()} instead to Vaadin 8 UI; perform any initialization in the <code>tab init listener</code>,
 * passed to the {@link #uiInitListener(SerializableConsumer)}.
 */
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
     *                        before any route or layout is created or initialized. In the listener,
     *                        You can store any init values to {@link #getValues()}, or perform any
     *                        kind of initialization that only needs to be done once per browser tab.
     * @return the UI init listener
     */
    @NotNull
    public static UIInitListener uiInitListener(@NotNull SerializableConsumer<TabScope> tabInitListener) {
        return event -> init(tabInitListener);
    }

    /**
     * Initializes the tab scope.
     * @param tabInitListener when the tab scope has been initialized, this listener is called. Invoked exactly once for a browser tab,
     *                         before any route or layout is created or initialized.
     */
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
            // tabScope1 may not be null. This can happen on page reload, when there's a new UI instance
            // (which doesn't carry over ExtendedClientDetails from the old UI instance:
            // ui.getInternals().getExtendedClientDetails() returns null),
            // but the TabScope is already created for this browser tab by the previous UI.
            if (tabScope1 == null) {
                tabScope1 = new TabScope();
                getInstances().put(ecd.getWindowName(), tabScope1);
                tabInitListener.accept(tabScope1);
            }
        });

        // Important note regarding the "before any route or layout is created or initialized"
        // This requirement is not actually implemented anywhere in this code, but
        // instead relies on the way internal Vaadin machinery works.
        //
        // The way this works is that Vaadin (when @PreserveOnRefresh is used) defers navigation
        // until the ECD is fetched (since @PreserveOnRefresh needs to know the ECD.windowName too).
        // Hopefully, our ExtendedClientDetailsReceiver is invoked first, initializing the TabScope;
        // the deferred navigation hopefully happens afterwards.
        //
        // The usage of word "hopefully" above hints that this solution is a bit fragile, but
        // unfortunately that's the only way. Vote for https://github.com/vaadin/flow/issues/13468
        // to be implemented, so that a better solution can be found.
    }

    /**
     * Returns the current tab scope.
     * Can be called from your routes, layouts and components, or generally any other code which runs in
     * Vaadin UI thread.
     * <br/>
     * Can not be called from the UI init listener itself, or before the UI init listener has been run.
     * @return the tab scope, not null.
     */
    @NotNull
    public static TabScope getCurrent() {
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
