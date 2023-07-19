module com.ping.ide {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.jfxtras.styles.jmetro;
    requires java.desktop;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires java.scripting;
    requires org.graalvm.sdk;

    opens com.ping.ide to javafx.fxml;
    exports com.ping.ide;
}