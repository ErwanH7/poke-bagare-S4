package com.montaury.pokebagarre.ui;

import java.util.concurrent.TimeUnit;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(ApplicationExtension.class)
class PokeBagarreAppTest {

    private static final String IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_1 = "#nomPokemon1";
    private static final String IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_2 = "#nomPokemon2";
    private static final String IDENTIFIANT_BOUTON_BAGARRE = ".button";

    @Start
    private void start(Stage stage) {
        new PokeBagarreApp().start(stage);
    }

    @Test
    void erreur_si_premier_pokemon_non_renseigne(FxRobot robot) {
        // Le champ du premier pokemon est vide, le second est rempli
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_2);
        robot.write("Pikachu");
        robot.clickOn(IDENTIFIANT_BOUTON_BAGARRE);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(getMessageErreur(robot))
                .isEqualTo("Erreur: Le premier pokemon n'est pas renseigne")
        );
    }

    @Test
    void erreur_si_second_pokemon_non_renseigne(FxRobot robot) {
        // Le champ du second pokemon est vide, le premier est rempli
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_1);
        robot.write("Pikachu");
        robot.clickOn(IDENTIFIANT_BOUTON_BAGARRE);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(getMessageErreur(robot))
                .isEqualTo("Erreur: Le second pokemon n'est pas renseigne")
        );
    }

    @Test
    void erreur_si_les_deux_champs_sont_vides(FxRobot robot) {
        // Les deux champs sont vides
        robot.clickOn(IDENTIFIANT_BOUTON_BAGARRE);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(getMessageErreur(robot))
                .isEqualTo("Erreur: Le premier pokemon n'est pas renseigne")
        );
    }

    @Test
    void erreur_si_meme_pokemon_dans_les_deux_champs(FxRobot robot) {
        // Les deux champs contiennent le même nom de pokemon
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_1);
        robot.write("Pikachu");
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_2);
        robot.write("Pikachu");
        robot.clickOn(IDENTIFIANT_BOUTON_BAGARRE);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(getMessageErreur(robot))
                .isEqualTo("Erreur: Impossible de faire se bagarrer un pokemon avec lui-meme")
        );
    }

    @Test
    void erreur_si_meme_pokemon_avec_casse_differente(FxRobot robot) {
        // Le même pokemon avec des majuscules différentes doit aussi être refusé
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_1);
        robot.write("pikachu");
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_2);
        robot.write("PIKACHU");
        robot.clickOn(IDENTIFIANT_BOUTON_BAGARRE);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(getMessageErreur(robot))
                .isEqualTo("Erreur: Impossible de faire se bagarrer un pokemon avec lui-meme")
        );
    }

    @Test
    void erreur_si_meme_pokemon_avec_espaces(FxRobot robot) {
        // Le même pokemon avec des espaces autour doit aussi être refusé
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_1);
        robot.write("Pikachu");
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_2);
        robot.write("  Pikachu  ");
        robot.clickOn(IDENTIFIANT_BOUTON_BAGARRE);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(getMessageErreur(robot))
                .isEqualTo("Erreur: Impossible de faire se bagarrer un pokemon avec lui-meme")
        );
    }

    @Test
    void affiche_le_vainqueur_apres_une_bagarre_valide(FxRobot robot) {
        // Scénario nominal : deux pokémons différents → l'API répond et le vainqueur s'affiche
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_1);
        robot.write("pikachu");
        robot.clickOn(IDENTIFIANT_CHAMP_DE_SAISIE_POKEMON_2);
        robot.write("bulbasaur");
        robot.clickOn(IDENTIFIANT_BOUTON_BAGARRE);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(getResultatBagarre(robot))
                .startsWith("Le vainqueur est: ")
        );
    }

    private static String getResultatBagarre(FxRobot robot) {
        return robot.lookup("#resultatBagarre").queryText().getText();
    }

    private static String getMessageErreur(FxRobot robot) {
        return robot.lookup("#resultatErreur").queryLabeled().getText();
    }
}
