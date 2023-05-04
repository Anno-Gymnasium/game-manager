package org.app.fx_application;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.Properties;

// Singleton-Klasse, die eine JDBI-Instanz zur Verfügung stellt
public final class JdbiProvider {
    private static JdbiProvider instance = null;
    private final Jdbi jdbi;

    private JdbiProvider() throws IOException {
        // Lade db-properties.properties
        Properties dbProperties = new java.util.Properties();
        dbProperties.load(getClass().getClassLoader().getResourceAsStream("db-properties.properties"));
        String server = dbProperties.getProperty("jdbi.server");
        int port = Integer.parseInt(dbProperties.getProperty("jdbi.port"));
        String database = dbProperties.getProperty("jdbi.database");
        String user = dbProperties.getProperty("jdbi.user");
        String password = dbProperties.getProperty("jdbi.password");

        // Erstelle DataSource
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName(server);
        ds.setPortNumber(port);
        ds.setDatabaseName(database);
        ds.setUser(user);
        ds.setPassword(password);

        // JDBI-Setup
        jdbi = Jdbi.create(ds).installPlugin(new org.jdbi.v3.sqlobject.SqlObjectPlugin());
    }

    public static JdbiProvider getInstance() {
        if (instance == null) {
            try {
                instance = new JdbiProvider();
            } catch (IOException e) {
                throw new RuntimeException("Properties zur Datenbank-Verbindung konnten nicht geladen werden", e);
            }
        }
        return instance;
    }
    public Jdbi getJdbi() {
        return jdbi;
    }
}