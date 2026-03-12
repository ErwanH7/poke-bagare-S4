import com.montaury.pokebagarre.metier.Pokemon;
import com.montaury.pokebagarre.metier.Stats;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class PokemonTests {

    /**
     * Tests de la méthode estVainqueurContre(Pokemon)
     *
     * Scénarios testés :
     * - attaque supérieure → vainqueur
     * - attaque inférieure → perdant
     * - attaque égale + défense supérieure → vainqueur
     * - attaque égale + défense inférieure → perdant
     * - attaque égale + défense égale → priorité au premier
     */

    @Test
    void premier_serait_vainqueur_avec_meilleur_attaque() {
        // given
        Pokemon pikachu = new Pokemon("Pikachu", "url", new Stats(100,50));
        Pokemon carapuce = new Pokemon("Carapuce", "url", new Stats(80,200));

        // when
        boolean resultat = pikachu.estVainqueurContre(carapuce);

        // then
        assertThat(resultat).isTrue();
    }

    @Test
    void second_serait_vainqueur_avec_meilleur_attaque() {
        // given
        Pokemon pikachu = new Pokemon("Pikachu", "url", new Stats(70,200));
        Pokemon dracaufeu = new Pokemon("Dracaufeu", "url", new Stats(90, 10));

        // when
        boolean resultat = pikachu.estVainqueurContre(dracaufeu);

        // then
        assertThat(resultat).isFalse();
    }

    @Test
    void premier_serait_vainqueur_avec_meme_attaque_mais_meilleur_defense() {
        // given
        Pokemon pikachu = new Pokemon("Pikachu", "url", new Stats(80, 100));
        Pokemon bulbizarre = new Pokemon("Bulbizarre", "url", new Stats(80, 50));

        // when
        boolean resultat = pikachu.estVainqueurContre(bulbizarre);

        // then
        assertThat(resultat).isTrue();
    }

    @Test
    void second_serait_vainqueur_avec_meme_attaque_mais_meilleur_defense() {
        // given
        Pokemon pikachu = new Pokemon("Pikachu", "url", new Stats(80, 40));
        Pokemon bulbizarre = new Pokemon("Bulbizarre", "url", new Stats(80, 90));

        // when
        boolean resultat = pikachu.estVainqueurContre(bulbizarre);

        // then
        assertThat(resultat).isFalse();
    }

    @Test
    void premier_serait_vainqueur_avec_meme_attaque_et_meme_defense() {
        // given
        Pokemon pikachu = new Pokemon("Pikachu", "url", new Stats(100, 100));
        Pokemon mew = new Pokemon("Mew", "url", new Stats(100, 100));

        // when
        boolean resultat = pikachu.estVainqueurContre(mew);

        // then
        assertThat(resultat).isTrue();
    }
}