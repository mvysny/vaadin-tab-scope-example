package com.vaadin.starter.skeleton;

import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.InstantiatorFactory;
import com.vaadin.flow.server.VaadinService;
import org.jetbrains.annotations.NotNull;

/**
 * A custom instantiator which adds support for tab-scoped routes (annotated with
 * {@link TabScoped}).
 */
public class TabScopedRouteInstantiator extends DefaultInstantiator {
    /**
     * Creates a new instantiator for the given service.
     *
     * @param service the service to use
     */
    public TabScopedRouteInstantiator(VaadinService service) {
        super(service);
    }

    @Override
    @NotNull
    public <T> T getOrCreate(@NotNull Class<T> type) {
        if (type.getAnnotation(TabScoped.class) != null) {
            T instance = TabScope.getCurrent().getValues().getAttribute(type);
            if (instance == null) {
                instance = super.getOrCreate(type);
                TabScope.getCurrent().getValues().setAttribute(type, instance);
            }
            return instance;
        }
        return super.getOrCreate(type);
    }

    public static class Factory implements InstantiatorFactory {
        @Override
        public Instantiator createInstantitor(VaadinService service) {
            return new TabScopedRouteInstantiator(service);
        }
    }
}
