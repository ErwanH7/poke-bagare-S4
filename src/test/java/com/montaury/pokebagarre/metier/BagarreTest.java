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
        // Given
        String premierPokemon = null;
        String secondPokemon = "pikachu";

        // When
        Throwable thrown = catchThrowable(() -> bagarre.demarrer(premierPokemon, secondPokemon));

        // Then
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");
    }

    @Test
    void doit_lever_erreur_si_premier_pokemon_est_vide() {
        // Given
        String premierPokemon = "";
        String secondPokemon = "pikachu";

        // When
        Throwable thrown = catchThrowable(() -> bagarre.demarrer(premierPokemon, secondPokemon));

        // Then
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");
    }

    // --- Cas 2 : même pokemon ---

    @Test
    void doit_lever_erreur_si_les_deux_pokemons_sont_identiques() {
        // Given
        String premierPokemon = "pikachu";
        String secondPokemon = "pikachu";

        // When
        Throwable thrown = catchThrowable(() -> bagarre.demarrer(premierPokemon, secondPokemon));

        // Then
        assertThat(thrown)
                .isInstanceOf(ErreurMemePokemon.class)
                .hasMessage("Impossible de faire se bagarrer un pokemon avec lui-meme");
    }

    // --- Cas 3 : second pokemon non renseigné ---

    @Test
    void doit_lever_erreur_si_second_pokemon_est_vide() {
        // Given
        String premierPokemon = "pikachu";
        String secondPokemon = "";

        // When
        Throwable thrown = catchThrowable(() -> bagarre.demarrer(premierPokemon, secondPokemon));

        // Then
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le second pokemon n'est pas renseigne");
    }

    @Test
    void doit_lever_erreur_si_second_pokemon_est_null() {
        // Given
        String premierPokemon = "pikachu";
        String secondPokemon = null;

        // When
        Throwable thrown = catchThrowable(() -> bagarre.demarrer(premierPokemon, secondPokemon));

        // Then
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le second pokemon n'est pas renseigne");
    }

    // --- Cas 4 : échec récupération API ---

    @Test
    void doit_echouer_si_lapi_ne_trouve_pas_le_premier_pokemon() {
        // Given
        Mockito.when(fausseApi.recupererParNom("inconnu"))
                    .thenReturn(CompletableFuture.failedFuture(new ErreurRecuperationPokemon("inconnu")));
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url", new Stats(50, 30))));

        // When
        var futur = bagarre.demarrer("inconnu", "pikachu");

        // Then
        assertThat(futur)
                .failsWithin(Duration.ofSeconds(2))
                .withThrowableOfType(ExecutionException.class)
                .havingCause()
                .isInstanceOf(ErreurRecuperationPokemon.class)
                .withMessage("Impossible de recuperer les details sur 'inconnu'");
    }

    @Test
    void doit_echouer_si_lapi_ne_trouve_pas_le_second_pokemon() {
        // Given
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url", new Stats(50, 30))));
        Mockito.when(fausseApi.recupererParNom("inconnu"))
                .thenReturn(CompletableFuture.failedFuture(new ErreurRecuperationPokemon("inconnu")));

        // When
        var futur = bagarre.demarrer("pikachu", "inconnu");

        // Then
        assertThat(futur)
                .failsWithin(Duration.ofSeconds(2))
                .withThrowableOfType(ExecutionException.class)
                .havingCause()
                .isInstanceOf(ErreurRecuperationPokemon.class)
                .withMessage("Impossible de recuperer les details sur 'inconnu'");
    }

    // --- Cas 5 : retour du pokémon gagnant ---

    @Test
    void doit_retourner_le_premier_pokemon_quand_il_gagne() {
        // Given
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url1", new Stats(100, 30))));
        Mockito.when(fausseApi.recupererParNom("bulbizarre"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("bulbizarre", "url2", new Stats(50, 30))));

        // When
        var futur = bagarre.demarrer("pikachu", "bulbizarre");

        // Then
        assertThat(futur)
                .succeedsWithin(Duration.ofSeconds(2))
                .satisfies(pokemon -> assertThat(pokemon.getNom()).isEqualTo("pikachu"));
    }

    @Test
    void doit_retourner_le_second_pokemon_quand_il_gagne() {
        // Given
        Mockito.when(fausseApi.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url1", new Stats(30, 30))));
        Mockito.when(fausseApi.recupererParNom("bulbizarre"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("bulbizarre", "url2", new Stats(80, 30))));

        // When
        var futur = bagarre.demarrer("pikachu", "bulbizarre");

        // Then
        assertThat(futur)
                .succeedsWithin(Duration.ofSeconds(2))
                .satisfies(pokemon -> assertThat(pokemon.getNom()).isEqualTo("bulbizarre"));
    }
}
