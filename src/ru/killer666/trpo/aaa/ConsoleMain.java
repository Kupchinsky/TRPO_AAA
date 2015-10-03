package ru.killer666.trpo.aaa;

import org.apache.commons.cli.*;

public class ConsoleMain {

    private static Option makeOptionWithArgument(String shortName, String longName, String description, boolean isRequired) {
        Option result = new Option(shortName, longName, true, description);
        result.setArgs(1);
        result.setRequired(isRequired);

        return result;
    }

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(ConsoleMain.makeOptionWithArgument("l", "login", "Login name", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("p", "password", "Password", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("res", "resource", "Resource name", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("r", "role", "Role", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("sd", "start-date", "Start date", false));
        options.addOption(ConsoleMain.makeOptionWithArgument("ed", "end-date", "End date", false));
        options.addOption(ConsoleMain.makeOptionWithArgument("vol", "volume", "Volume", false));

        CommandLine commandLine = new DefaultParser().parse(options, args);
    }
}
