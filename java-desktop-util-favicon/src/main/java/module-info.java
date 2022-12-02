
module nbbrd.desktop.favicon {

    requires static lombok;
    requires static org.checkerframework.checker.qual;
    requires static nbbrd.design;
    requires static nbbrd.service;

    requires transitive java.desktop;

    exports nbbrd.desktop.favicon;

    uses nbbrd.desktop.favicon.FaviconSupplier;
    provides nbbrd.desktop.favicon.FaviconSupplier with
            internal.desktop.favicon.FaviconkitSupplier,
            internal.desktop.favicon.GoogleSupplier;
}
