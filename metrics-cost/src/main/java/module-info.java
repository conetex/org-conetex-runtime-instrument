module org.conetex.runtime.instrument.metrics.cost {
    exports org.conetex.runtime.instrument.metrics.cost to org.conetex.runtime.instrument.test.jar.module;
    opens org.conetex.runtime.instrument.metrics.cost to org.conetex.runtime.instrument.agent;
    requires org.conetex.runtime.instrument.counter;
    requires java.instrument;
    requires org.conetex.runtime.instrument.interfaces;
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;

}