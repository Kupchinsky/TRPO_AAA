package ru.killer666.trpo.aaa;

import org.apache.commons.cli.*;
import ru.killer666.trpo.aaa.models.Role;

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

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(ConsoleMain.makeOptionWithArgument("l", "login", "Login name", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("p", "password", "Password", true));
        options.addOption(ConsoleMain.makeOptionWithArgument("res", "resource", "Resource name", false));
        options.addOption(ConsoleMain.makeOptionWithArgument("r", "role", "Role", false));
        options.addOption(ConsoleMain.makeOptionWithArgument("sd", "start-date", "Start date", false));
        options.addOption(ConsoleMain.makeOptionWithArgument("ed", "end-date", "End date", false));
        options.addOption(ConsoleMain.makeOptionWithArgument("vol", "volume", "Volume", false));

        try {
            CommandLine commandLine = new DefaultParser().parse(options, args);
            UserController controller = new UserController();

            // Auth user
            controller.authUser(commandLine.getOptionValue("l"), commandLine.getOptionValue("p"));

            if (commandLine.hasOption("res")) {
                if (!commandLine.hasOption("r"))
                    throw new MissingOptionException("Option not found: -r,--role");

                // Get role and create accounting
                Role role = Role.valueOf(commandLine.getOptionValue("r"));

                if (role == null)
                    throw new Role.InvalidRoleException();

                controller.createAccounting(role);

                // Auth resource
                controller.authResource(commandLine.getOptionValue("res"));

                boolean hasSd = commandLine.hasOption("sd");
                boolean hasEd = commandLine.hasOption("ed");
                boolean hasVol = commandLine.hasOption("vol");

                if (hasSd || hasEd || hasVol) {

                    if (!hasSd)
                        throw new MissingOptionException("Option not found: -sd,--start-date");
                    else if (!hasEd)
                        throw new MissingOptionException("Option not found: -ed,--end-date");
                    else if (!hasVol)
                        throw new MissingOptionException("Option not found: -vol,--volume");

                    DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);

                    controller.getLogOnUserAccounting().setLoginDate(format.parse(commandLine.getOptionValue("sd")));
                    controller.getLogOnUserAccounting().setLogoutDate(format.parse(commandLine.getOptionValue("ed")));
                    controller.getLogOnUserAccounting().setVolume(Integer.parseInt(commandLine.getOptionValue("vol")));

                    if (controller.getLogOnUserAccounting().getVolume() <= 0)
                        throw new NumberFormatException("Invalid volume number!");
                }

                controller.saveAccounting();
            }

            controller.clearAll();

            System.exit(0);
        } catch (ParseException e) {
            ConsoleMain.printHelp(options);
            System.exit(255);
        } catch (UserController.UserNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (UserController.IncorrectPasswordException e) {
            e.printStackTrace();
            System.exit(2);
        } catch (Role.InvalidRoleException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (UserController.ResourceNotFoundException | UserController.ResourceDeniedException e) {
            e.printStackTrace();
            System.exit(4);
        } catch (java.text.ParseException | NumberFormatException e) {
            e.printStackTrace();
            System.exit(5);
        }
    }
}
