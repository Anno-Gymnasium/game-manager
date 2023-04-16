module spiele.manager {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires org.jetbrains.annotations;

    requires jakarta.mail;
    requires spring.security.crypto;
    requires org.jdbi.v3.core;
    requires org.jdbi.v3.sqlobject;
    requires org.slf4j;

    requires com.microsoft.sqlserver.jdbc;
    requires java.naming;

    opens org.app.fx_application to javafx.graphics, javafx.fxml, org.jdbi.v3.core, org.jdbi.v3.sqlobject;
    opens org.app.game_classes to org.jdbi.v3.core, org.jdbi.v3.sqlobject;
    opens org.app.fx_application.dialogs to javafx.fxml, javafx.graphics, org.jdbi.v3.core, org.jdbi.v3.sqlobject;
    opens org.app.fx_application.selectables to javafx.fxml, javafx.graphics, org.jdbi.v3.core, org.jdbi.v3.sqlobject;
    opens org.app.fx_application.controllers to javafx.fxml, javafx.graphics, org.jdbi.v3.core, org.jdbi.v3.sqlobject;
    opens org.app.fx_application.daos to javafx.fxml, javafx.graphics, org.jdbi.v3.core, org.jdbi.v3.sqlobject;
    opens org.app to javafx.fxml, javafx.graphics, org.jdbi.v3.core, org.jdbi.v3.sqlobject;

    exports org.app.fx_application;
    exports org.app.game_classes;
    exports org.app;
    exports org.app.fx_application.dialogs;
    exports org.app.fx_application.selectables;
    exports org.app.fx_application.controllers;
    exports org.app.fx_application.daos;
}