package com.avairebot.orion.commands;

import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.utilities.RandomUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Category {

    ADMINISTRATION("Administration", "."),
    FUN("Fun", ">"),
    HELP("Help", "."),
    INTERACTION("Interaction", ">"),
    MUSIC("Music", "!"),
    SYSTEM("System", ";"),
    UTILITY("Utility", "!");

    private static final List<Category> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

    private final String name;
    private final String prefix;

    Category(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public static Category fromCommand(Command command) {
        String commandPackage = command.getClass().getName().split("\\.")[4];

        for (Category category : Category.values()) {
            if (category.toString().equalsIgnoreCase(commandPackage)) {
                return category;
            }
        }
        return null;
    }

    public static Category fromLazyName(String name) {
        name = name.toLowerCase();

        for (Category category : values()) {
            if (category.getName().toLowerCase().startsWith(name)) {
                return category;
            }
        }
        return null;
    }

    public static Category random() {
        return VALUES.get(RandomUtil.getInteger(VALUES.size()));
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }
}
