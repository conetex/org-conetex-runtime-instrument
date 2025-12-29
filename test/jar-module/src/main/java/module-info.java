module org.conetex.runtime.instrument.test.jar.module {
    requires org.conetex.runtime.instrument.counter;
    requires org.conetex.runtime.instrument.interfaces;
    requires org.conetex.runtime.instrument.metrics.cost;
    opens org.conetex.runtime.instrument.test.jar.module;
}