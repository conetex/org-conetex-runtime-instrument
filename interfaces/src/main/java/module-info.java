module org.conetex.runtime.instrument.interfaces {
    requires java.instrument;
    exports org.conetex.runtime.instrument.interfaces.arithmetic;
    exports org.conetex.runtime.instrument.interfaces.counter;
    opens org.conetex.runtime.instrument.interfaces.counter;
    exports org.conetex.runtime.instrument.interfaces;
}