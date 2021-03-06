package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class PokeCommand extends InteractionCommand {

    public PokeCommand(Orion orion) {
        super(orion, "pokes");
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
                "https://i.imgur.com/BWG6gvH.gif",
                "https://i.imgur.com/xGlwJ3P.gif",
                "https://i.imgur.com/UD2EUBE.gif",
                "https://i.imgur.com/rFkrrdg.gif",
                "https://i.imgur.com/JIWK6Ht.gif",
                "https://i.imgur.com/rd9p6DQ.gif"
        );
    }

    @Override
    public String getName() {
        return "Poke Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("poke", "pokes");
    }
}
