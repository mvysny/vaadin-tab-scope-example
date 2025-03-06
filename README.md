# Vaadin Tab-Scope Example App

A demo project demoing a proper way of having tab-scoped values and routes.
No Spring - pure Servlet project.

In Vaadin 8, things were simple: both `UI` and `UI.init()` was working predictably:
the UI was instantiated once per tab (when using `@PreserveOnRefresh`), and the init
listener ran exactly once, before everything else, and so it was the right place to perform initialization.

In Vaadin 23+, this is no longer the case. You don't use UI almost at all, the only
sane way to use `UI` is to store data to it via `ComponentUtil` ,
and there is a `UIInitListener`. However, even with `@PreserveOnRefresh`, the `UI`
won't survive reload and thus:

- The init listener is called multiple times per browser tab, once for every reload; and
- Route instances are preserved on page reload, but they're killed when navigating away to another view and back.

Please see the [Tab Scope blog post](https://mvysny.github.io/vaadin-ui-scope/)
and [issue #13468](https://github.com/vaadin/flow/issues/13468) for more details.

This project implements tab-scoped values and tab-scoped routes and fixes both of the abovementioned
issues. Moreover, the implementation works correctly even without the `@PreserveOnRefresh` annotation.

[Live demo at v-herd](https://v-herd.eu/vaadin-tab-scope-example).

# Documentation

Please see the [Vaadin Boot](https://github.com/mvysny/vaadin-boot#preparing-environment) documentation
on how you run, develop and package this Vaadin-Boot-based app.

The `TabScope` class is used to store tab-scoped values. First, it needs to be
initialized in the UI Init Listener:
```java
public class ApplicationServiceInitListener
        implements VaadinServiceInitListener {

    static final AtomicInteger counter = new AtomicInteger();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(TabScope.uiInitListener(ts -> {
            ts.getValues().setAttribute("hello", counter.incrementAndGet());
        }));
    }
}
```
You can now access/modify the tab-scoped values from your routes, layouts and components, or generally any other code which runs in
Vaadin UI thread. See the `MainView` and `MainViewNoAppLayout` views for example on a regular route (prototype-scoped:
new instance every time) accessing tab-scoped values.

## Tab-scoped routes

It's also possible to have tab-scoped routes - instances of those routes are preserved and reused
when they're navigated to repeatedly. A special `Instantiator` is implemented, to
cache `@TabScoped`-annotated views in the `TabScope` map - see `TabScopedRouteInstantiator` for more details.

See the `TabScopedView` and `TabScopedViewNoAppLayout` routes for more details.

## Cleaning up

This is the open topic. The way `TabScope` works is that it keeps a `Map` from `ExtendedClientDetails.windowName`
(which identifies the browser tab uniquely) to the instance of `TabScope`.
At the moment, even if you close the browser tab, the tab-scoped routes and values will
continue to be stored in the Vaadin Session, and they are never removed manually.
The values and routes are only removed and GC-ed when the whole session goes down.

This is exactly the way in which the official `vaadin-spring` plugin, the `VaadinRouteScope` `BeanStore` works,
so I'll just keep it like that for the time being.

Couple of considerations:

* I can't purge the tab scope when the UI is closed: when refreshing, old UI is killed and detached,
  before the new one springs to life. So, I can't purge the tab scope on UI detach, since that would cause tab-scoped
  values to not survive the page reload.
* I could perhaps mark down that the UI currently associated with the `TabScope` is detached, and perform
  some kind of GC after certain time - the same way Vaadin cleans up inactive UIs. There would have to be
  a configurable timeout though...
* or maybe not: on page reload, it is expected that the new UI springs to life
  fast. So maybe I can wait 60 seconds tops, then kill the tab scope.

Remains to be seen.
