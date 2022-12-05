
module nbbrd.desktop.favicon {

    requires static lombok;
    requires static org.checkerframework.checker.qual;
    requires static nbbrd.design;
    requires static nbbrd.service;

    requires transitive java.desktop;

    exports nbbrd.desktop.favicon;
    exports nbbrd.desktop.favicon.spi;

    uses nbbrd.desktop.favicon.spi.FaviconSupplier;
    provides nbbrd.desktop.favicon.spi.FaviconSupplier with
            internal.desktop.favicon.spi.FaviconkitSupplier,
            internal.desktop.favicon.spi.GoogleSupplier;
}
