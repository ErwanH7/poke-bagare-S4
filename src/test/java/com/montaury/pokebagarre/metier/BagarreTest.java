package com.montaury.pokebagarre.metier;

import com.montaury.pokebagarre.erreurs.ErreurMemePokemon;
import com.montaury.pokebagarre.erreurs.ErreurPokemonNonRenseigne;
import com.montaury.pokebagarre.erreurs.ErreurRecuperationPokemon;
import com.montaury.pokebagarre.webapi.PokeBuildApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class BagarreTest {

    private PokeBuildApi fausseApi;
    private Bagarre bagarre;

    @BeforeEach
    void setUp() {
        fausseApi = Mockito.mock(PokeBuildApi.class);
        bagarre = new Bagarre(fausseApi);
    }

    // --- Cas 1 : premier pokemon non renseigné ---

    @Test
    void doit_lever_erreur_si_premier_pokemon_est_null() {
        Throwable thrown = catchThrowable(() -> bagarre.demarrer(null, "pikachu"));
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");
    }

    @Test
    void doit_lever_erreur_si_premier_pokemon_est_vide() {
        Throwable thrown = catchThrowable(() -> bagarre.demarrer("", "pikachu"));
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");
    }

    @Test
    void doit_lever_erreur_si_premier_pokemon_est_compose_despaces() {
        Throwable thrown = catchThrowable(() -> bagarre.demarrer("   ", "pikachu"));
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");
    }

    // --- Cas 2 : second pokemon non renseigné ---

    @Test
    void doit_lever_erreur_si_second_pokemon_est_null() {
        Throwable thrown = catchThrowable(() -> bagarre.demarrer("pikachu", null));
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le second pokemon n'est pas renseigne");
    }

    @Test
    void doit_lever_erreur_si_second_pokemon_est_vide() {
        Throwable thrown = catchThrowable(() -> bagarre.demarrer("pikachu", ""));
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le second pokemon n'est pas renseigne");
    }

    // --- Cas 3 : même pokemon ---

    @Test
    void doit_lever_erreur_si_les_deux_pokemons_sont_identiques() {
        Throwable thrown = catchThrowable(() -> bagarre.demarrer("pikachu", "pikachu"));
        assertThat(thrown)
                .isInstanceOf(ErreurMemePokemon.class)
                .hasMessage("Impossible de faire se bagarrer un pokemon avec lui-meme");
    }

    @Test
    void doit_lever_erreur_si_les_deux_pokemons_sont_identiques_casse_differente() {
        Throwable thrown = catchThrowable(() -> bagarre.demarrer("pikachu", "PIKACHU"));
        assertThat(thrown).isInstanceOf(ErreurMemePokemon.class);
    }

    @Test
    void doit_lever_erreur_si_les_deux_pokemons_sont_identiques_avec_espaces() {
        Throwable thrown = catchThrowable(() -> bagarre.demarrer(" pikachu ", "pikachu"));
        assertThat(thrown).isInstanceOf(ErreurMemePokemon.class);
    }

    // --- Cas 4 : échec récupération API ---

    @Test
    void doit_echouer_si_lapi_ne_trouve_pas_le_premier_pokemon() {
        Mockito.when(fausseApi.recupererParNom("inconnu"))
                .thenReturn(CompletableFuture.failedFuture(new ErreurRecuperationPokemon("inconnu")));
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url", new Stats(50, 30))));

        var futur = bagarre.demarrer("inconnu", "pikachu");

        assertThat(futur)
                .failsWithin(Duration.ofSeconds(2))
                .withThrowableOfType(ExecutionException.class)
                .havingCause()
                .isInstanceOf(ErreurRecuperationPokemon.class)
                .withMessage("Impossible de recuperer les details sur 'inconnu'");
    }

    @Test
    void doit_echouer_si_lapi_ne_trouve_pas_le_second_pokemon() {
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url", new Stats(50, 30))));
        Mockito.when(fausseApi.recupererParNom("inconnu"))
                .thenReturn(CompletableFuture.failedFuture(new ErreurRecuperationPokemon("inconnu")));

        var futur = bagarre.demarrer("pikachu", "inconnu");

        assertThat(futur)
                .failsWithin(Duration.ofSeconds(2))
                .withThrowableOfType(ExecutionException.class)
                .havingCause()
                .isInstanceOf(ErreurRecuperationPokemon.class)
                .withMessage("Impossible de recuperer les details sur 'inconnu'");
    }

    // --- Cas 5 : premier pokemon a plus d'attaque ---

    @Test
    void doit_retourner_le_premier_pokemon_si_son_attaque_est_superieure() {
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url1", new Stats(100, 30))));
        Mockito.when(fausseApi.recupererParNom("bulbizarre"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("bulbizarre", "url2", new Stats(50, 30))));

        var futur = bagarre.demarrer("pikachu", "bulbizarre");

        assertThat(futur)
                .succeedsWithin(Duration.ofSeconds(2))
                .satisfies(pokemon -> assertThat(pokemon.getNom()).isEqualTo("pikachu"));
    }

    // --- Cas 6 : second pokemon a plus d'attaque ---

    @Test
    void doit_retourner_le_second_pokemon_si_son_attaque_est_superieure() {
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url1", new Stats(30, 30))));
        Mockito.when(fausseApi.recupererParNom("bulbizarre"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("bulbizarre", "url2", new Stats(80, 30))));

        var futur = bagarre.demarrer("pikachu", "bulbizarre");

        assertThat(futur)
                .succeedsWithin(Duration.ofSeconds(2))
                .satisfies(pokemon -> assertThat(pokemon.getNom()).isEqualTo("bulbizarre"));
    }

    // --- Cas 7 : attaque égale, défense du premier >= second ---

    @Test
    void doit_retourner_le_premier_pokemon_si_attaque_egale_et_defense_superieure_ou_egale() {
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url1", new Stats(50, 40))));
        Mockito.when(fausseApi.recupererParNom("bulbizarre"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("bulbizarre", "url2", new Stats(50, 20))));

        var futur = bagarre.demarrer("pikachu", "bulbizarre");

        assertThat(futur)
                .succeedsWithin(Duration.ofSeconds(2))
                .satisfies(pokemon -> assertThat(pokemon.getNom()).isEqualTo("pikachu"));
    }

    // --- Cas 8 : attaque égale, défense du premier < second ---

    @Test
    void doit_retourner_le_second_pokemon_si_attaque_egale_et_defense_inferieure() {
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url1", new Stats(50, 10))));
        Mockito.when(fausseApi.recupererParNom("bulbizarre"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("bulbizarre", "url2", new Stats(50, 50))));

        var futur = bagarre.demarrer("pikachu", "bulbizarre");

        assertThat(futur)
                .succeedsWithin(Duration.ofSeconds(2))
                .satisfies(pokemon -> assertThat(pokemon.getNom()).isEqualTo("bulbizarre"));
    }
}
