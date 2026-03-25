package com.montaury.pokebagarre.webapi;

import com.google.gson.Gson;
import com.montaury.pokebagarre.erreurs.ErreurBagarre;
import com.montaury.pokebagarre.erreurs.ErreurRecuperationPokemon;
import com.montaury.pokebagarre.metier.Pokemon;
import com.montaury.pokebagarre.metier.Stats;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class PokeBuildApi {
  private static final String URL_BASE_API_PRODUCTION = "https://pokeapi.co/";
  public static final String SUB_DIR_POKEMON_DANS_URLS_BASE_API_PRODUCTION = "api/v2/pokemon/";
  private final String urlBaseApi;
  private final HttpClient httpClient;
  private final Gson gson;

  public PokeBuildApi() {
    this(URL_BASE_API_PRODUCTION);
  }

  public PokeBuildApi(String urlBaseApi) {
    this.urlBaseApi = urlBaseApi;
    this.httpClient = HttpClient.newHttpClient();
    this.gson = new Gson();
  }

  public CompletableFuture<Pokemon> recupererParNom(String nom) throws ErreurBagarre {
    String nomNettoye = nom.trim();

    return httpClient.sendAsync(
                    HttpRequest.newBuilder()
                            .uri(getWebApiUri(nomNettoye))
                            .build(),
                    HttpResponse.BodyHandlers.ofString())
            .thenApply(reponseHttp -> essayerDeConvertirReponse(nomNettoye, reponseHttp));
  }

  private URI getWebApiUri(String nom) {
    try {
      return new URI(
              urlBaseApi + SUB_DIR_POKEMON_DANS_URLS_BASE_API_PRODUCTION +
                      URLEncoder.encode(nom.toLowerCase(), StandardCharsets.UTF_8));
    } catch (URISyntaxException e) {
      throw new ErreurRecuperationPokemon(nom);
    }
  }

  private Pokemon essayerDeConvertirReponse(String nom, HttpResponse<String> reponse) {
    int statut = reponse.statusCode();
    if (statut < 200 || statut >= 300) {
      throw new ErreurRecuperationPokemon(nom);
    }

    PokeApiPokemonPayload pokemonPayload = gson.fromJson(reponse.body(), PokeApiPokemonPayload.class);
    if (pokemonPayload == null || pokemonPayload.name == null) {
      throw new ErreurRecuperationPokemon(nom);
    }

    String sprite = extraireSprite(pokemonPayload);
    int attaque = extraireStat(pokemonPayload, "attack");
    int defense = extraireStat(pokemonPayload, "defense");

    return new Pokemon(
            pokemonPayload.name,
            sprite,
            new Stats(attaque, defense)
    );
  }

  private String extraireSprite(PokeApiPokemonPayload pokemonPayload) {
    if (pokemonPayload.sprites != null && pokemonPayload.sprites.front_default != null) {
      return pokemonPayload.sprites.front_default;
    }
    return "";
  }

  private int extraireStat(PokeApiPokemonPayload pokemonPayload, String nomStat) {
    if (pokemonPayload.stats == null) {
      throw new ErreurRecuperationPokemon(pokemonPayload.name);
    }

    for (PokemonStatPayload statPayload : pokemonPayload.stats) {
      if (statPayload != null
              && statPayload.stat != null
              && nomStat.equals(statPayload.stat.name)) {
        return statPayload.base_stat;
      }
    }

    throw new ErreurRecuperationPokemon(pokemonPayload.name);
  }

  private static class PokeApiPokemonPayload {
    private String name;
    private SpritesPayload sprites;
    private PokemonStatPayload[] stats;
  }

  private static class SpritesPayload {
    private String front_default;
  }

  private static class PokemonStatPayload {
    private int base_stat;
    private NamedApiResource stat;
  }

  private static class NamedApiResource {
    private String name;
  }
}
