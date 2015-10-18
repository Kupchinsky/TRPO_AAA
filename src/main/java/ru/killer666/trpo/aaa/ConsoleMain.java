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

    private Option makeOptionWithArgument(String shortName, String description, boolean isRequired) {
        Option result = new Option(shortName, true, description);
        result.setArgs(1);
        result.setRequired(isRequired);

        return result;
    }

    public ResultCode work(String[] args) {
        Options options = new Options()
                .addOption(this.makeOptionWithArgument("login", "Login name", true))
                .addOption(this.makeOptionWithArgument("pass", "Password", true))
                .addOption(this.makeOptionWithArgument("res", "Resource name", false))
                .addOption(this.makeOptionWithArgument("role", "Role", false))
                .addOption(this.makeOptionWithArgument("ds", "Start date", false))
                .addOption(this.makeOptionWithArgument("de", "End date", false))
                .addOption(this.makeOptionWithArgument("vol", "Volume", false));

        try {
            CommandLine commandLine = new DefaultParser().parse(options, args);
            UserController controller = new UserController();

            // Auth user
            controller.authUser(commandLine.getOptionValue("login"), commandLine.getOptionValue("pass"));

            if (commandLine.hasOption("res")) {
                if (!commandLine.hasOption("role"))
                    throw new MissingOptionException("Option not found: -role,--role");

                // Get role and create accounting
                Role role = Role.valueOf(commandLine.getOptionValue("role"));

                if (role == null)
                    throw new Role.InvalidRoleException();

                controller.createAccounting(role);

                // Auth resource
                controller.authResource(commandLine.getOptionValue("res"));

                boolean hasSd = commandLine.hasOption("ds");
                boolean hasEd = commandLine.hasOption("de");
                boolean hasVol = commandLine.hasOption("vol");

                if (hasSd || hasEd || hasVol) {

                    if (!hasSd)
                        throw new MissingOptionException("Option not found: -ds,--start-date");
                    else if (!hasEd)
                        throw new MissingOptionException("Option not found: -de,--end-date");
                    else if (!hasVol)
                        throw new MissingOptionException("Option not found: -vol,--volume");

                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

                    controller.getLogOnUserAccounting().setLoginDate(format.parse(commandLine.getOptionValue("ds")));
                    controller.getLogOnUserAccounting().setLogoutDate(format.parse(commandLine.getOptionValue("de")));
                    controller.getLogOnUserAccounting().setVolume(Integer.parseInt(commandLine.getOptionValue("vol")));

                    if (!controller.getLogOnUserAccounting().getLoginDate().before(controller.getLogOnUserAccounting().getLogoutDate()))
                        throw new NumberFormatException("Incorrect login or logout dates!");

                    if (controller.getLogOnUserAccounting().getVolume() <= 0)
                        throw new NumberFormatException("Invalid volume number!");
                }

                controller.saveAccounting();
            }

            controller.clearAll();
        } catch (ParseException e) {
            ConsoleMain.printHelp(options);
            return ResultCode.INVALIDINPUT;
        } catch (UserController.UserNotFoundException e) {
            e.printStackTrace();
            return ResultCode.USERNOTFOUND;
        } catch (UserController.IncorrectPasswordException e) {
            e.printStackTrace();
            return ResultCode.INCORRECTPASSWORD;
        } catch (Role.InvalidRoleException e) {
            e.printStackTrace();
            return ResultCode.INVALIDROLE;
        } catch (UserController.ResourceNotFoundException | UserController.ResourceDeniedException e) {
            e.printStackTrace();
            return ResultCode.RESOURCEDENIED;
        } catch (java.text.ParseException | NumberFormatException e) {
            e.printStackTrace();
            return ResultCode.INCORRECTACTIVITY;
        }

        return ResultCode.SUCCESS;
    }

    public static void main(String[] args) {
        ResultCode result = new ConsoleMain().work(args);
        System.exit(result.getValue());
    }
}
