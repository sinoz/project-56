package database;

import play.db.Database;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class JavaJdbcConnection {
    @Inject private Database db;

    public void tryIt() {
        db.withConnection(connection -> {
            // maar dit is de basics nu, alleen dtabase opzetten, ik denk dat je de rest wel weet
            // ik kan nu uiteraard de tabels toevoegen. Ik vraag me alleen af hoe dit later op de server komt te staan.
            // fix ik wel lol veel klote met configureren
            // Aah oki, dan maak ik vanaf hier wel gewoon de queries en bedenk misschien wat game account logins ofzo
            // Is het dan goed dat de URLs van images in de db komen? yh
            // en nog iets, nu je het zegt over game account logins
            // play support evolutions, met evolutions kun je default data plaatsen in de db voor test purposes

            // nice. Maar die ups waren dus voor creation and insertion en die downs voor dropping? ja maar stel dat je de gehele database opnieuw wilt bouwen
            // doe je DROP TABLE IF EXISTS voooor de creations
            // yeah, dan geen drop doen in down en juist met IF EXISTS boven create? huh hoe bedoel je lol
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM actor ORDER BY actor_id ASC");
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                System.out.println(results.getString("first_name"));
            }
        });
    }
}