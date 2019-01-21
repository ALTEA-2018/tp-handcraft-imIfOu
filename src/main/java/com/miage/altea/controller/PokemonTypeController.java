package com.miage.altea.controller;

import com.miage.altea.Controller;
import com.miage.altea.PostMapping;
import com.miage.altea.RequestMapping;
import com.miage.altea.bo.PokemonType;
import com.miage.altea.repository.PokemonTypeRepository;

import java.util.Map;

@Controller
public class PokemonTypeController {

    private PokemonTypeRepository repository = new PokemonTypeRepository();

    @RequestMapping(uri="/pokemon")
    public PokemonType getPokemon(Map<String,String[]> parameters){
        if(parameters == null){
            throw new IllegalArgumentException("parameters should not be empty");
        }

        if(parameters.get("id")==null && parameters.get("name") == null){
            throw new IllegalArgumentException("unknown parameter");
        }

        if(parameters.get("id") != null){
            return repository.findPokemonById(Integer.valueOf(parameters.get("id")[0]));
        }else if(parameters.get("name") != null){
            return repository.findPokemonByName(parameters.get("name")[0]);
        }

        return  null;
    }

    @PostMapping(uri="/pokemon")
    public void addPokemon(PokemonType pokemonType) {
        repository.addPokemon(pokemonType);
    }
}
