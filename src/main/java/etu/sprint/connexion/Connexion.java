package connexion;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connexion {
    public static Connection connect() throws Exception {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connect = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pg11", "postgres",
                    "postgres");

            return connect;
        } catch (Exception e) {
            throw e;
        }
    }
}