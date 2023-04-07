module spiele.manager {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires org.jetbrains.annotations;

    requires jakarta.mail;
    requires spring.security.crypto;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires jakarta.transaction;

    opens org.app.fx_application to javafx.graphics, javafx.fxml;
    opens org.app.comp_key_classes to org.hibernate.orm.core;
    opens org.app.game_classes to org.hibernate.orm.core;

    exports org.app.fx_application;
    exports org.app.game_classes;
    exports org.app.comp_key_classes;
}