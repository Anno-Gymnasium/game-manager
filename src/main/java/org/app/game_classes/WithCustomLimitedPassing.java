package org.app.game_classes;

import java.util.List;
import org.app.game_classes.GenericMatchingGame.NotEvaluatedException;

/** Interface für Spiele, bei denen der Moderator die Anzahl der Teams, die weiterkommen, oder die
 * Mindestpunktzahl, die ein Team zum Weiterkommen braucht, selbst festlegen kann. */
public interface WithCustomLimitedPassing {
    /** Wird ausgelöst, wenn die Teams für die nächste Liga nicht eindeutig ausgewählt werden können (bei bereits ausgewerteten Matches) */
    class TeamSelectionException extends Exception {
        public TeamSelectionException(String message) {
            super(message);
        }
        public TeamSelectionException(int nPassingTeams) {
            super("Die " + nPassingTeams + " besten Teams, die in die nächste Liga kommen sollen, können nicht eindeutig ausgewählt werden.");
        }
    }

    /** Beschreibt, welche Anzahlen an Teams, die weiterkommen sollen, der Moderator auswählen
     * kann, ohne dass zwischen Teams mit gleicher Punktzahl entschieden werden muss. */
    List<Integer> getPossibleNPassingTeams();

    /** Erstellt eine neue Liga, in der alle Teams der aktuellen Liga weiterkommen, die mindestens minScore Punkte haben.
     * Gibt die Anzahl der Teams zurück, die nicht weitergekommen sind. */
    int createNextLeague(int minScore) throws NotEvaluatedException, TeamSelectionException;

    /** Erstellt eine neue Liga, in der die nPassingTeams besten Teams der aktuellen Liga weiterkommen.
     * Wenn in der nach Punktzahl absteigend sortierten Liste das Team am Index nPassingTeams - 1 nach sich noch andere
     * Teams mit gleicher Punktzahl hat, wird unterschieden:
     * - Ist decideOnDraw = true, so werden aus der unklaren letzten Gruppe einige Teams zufällig ausgewählt.
     * - Ist decideOnDraw = false, so wird eine TeamSelectionException geworfen. */
    void createNextLeague(int nPassingTeams, boolean decideOnDraw) throws NotEvaluatedException, TeamSelectionException;
}