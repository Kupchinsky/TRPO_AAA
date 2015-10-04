package ru.killer666.trpo.aaa;

import org.apache.commons.cli.*;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ConsoleMain {

    private static void printHelp(final Options options) {
        final PrintWriter writer = new PrintWriter(System.out);
        final HelpFormatter helpFormatter = new HelpFormatter();

        helpFormatter.printHelp(
                writer,
                80,
                "[program]",
                "Options:",
                options,
                3,
                5,
                "-- HELP --",
                true);

        writer.flush();
    }

    private static Option makeOptionWithArgument(String shortName, String longName, String description, boolean isRequired) {
        Option result = new Option(shortName, longName, true, description);
        result.setArgs(1);
        result.setRequired(isRequired);

        return result;
    }

    public static void main(String[] args) throws java.text.ParseException {
        Options options = new Options();
        options.addOption(ConsoleMain.makeOptionWithArgument("l", "login", "Login name", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("p", "password", "Password", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("res", "resource", "Resource name", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("r", "role", "Role", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("sd", "start-date", "Start date", false));
        options.addOption(ConsoleMain.makeOptionWithArgument("ed", "end-date", "End date", false));
        options.addOption(ConsoleMain.makeOptionWithArgument("vol", "volume", "Volume", false));

        CommandLine commandLine = null;
        try {
            commandLine = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            ConsoleMain.printHelp(options);
            System.exit(255);
        }

        UserController controller = new UserController();

        try {
            controller.logIn(commandLine.getOptionValue("l"), commandLine.getOptionValue("p"), commandLine.getOptionValue("res"), Integer.valueOf(commandLine.getOptionValue("r")));
        } catch (UserController.UserNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (UserController.IncorrectPasswordException e) {
            e.printStackTrace();
            System.exit(2);
        } catch (UserController.ResourceDeniedException | UserController.ResourceNotFoundException e) {
            e.printStackTrace();
            System.exit(3);
        }

        if (commandLine.hasOption("sd")) {
            DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
            controller.getLogOnUserAccounting().setLoginDate(format.parse(commandLine.getOptionValue("sd")));
        }

        if (commandLine.hasOption("ed")) {
            DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
            controller.getLogOnUserAccounting().setLogoutDate(format.parse(commandLine.getOptionValue("ed")));
        }

        if (commandLine.hasOption("vol")) {
            controller.getLogOnUserAccounting().setVolume(Integer.valueOf(commandLine.getOptionValue("vol")));
        }

        controller.logOut();
        System.exit(0);
    }
}
