package ru.killer666.trpo.aaa.views;

import lombok.Getter;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.killer666.trpo.aaa.domains.Resource;
import ru.killer666.trpo.aaa.exceptions.*;
import ru.killer666.trpo.aaa.services.AuthService;
import ru.killer666.trpo.aaa.services.HibernateService;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConsoleMain {

    private static final Logger logger = LogManager.getLogger(ConsoleMain.class);

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

            ConsoleMain.logger.warn("Started");

            // Creating hibernate service, auth service
            AuthService authService = new AuthService(new HibernateService("jdbc:h2:./aaa", "sa", "", "org.hibernate.dialect.H2Dialect"));

            // Auth user
            authService.authUser(commandLine.getOptionValue("login"), commandLine.getOptionValue("pass"));

            if (commandLine.hasOption("res")) {
                String resourceName = commandLine.getOptionValue("res");
                Resource resource = authService.getResourceByName(resourceName);

                if (!commandLine.hasOption("role"))
                    throw new MissingOptionException("Option not found: -role,--role");

                // Get role
                String strRole = commandLine.getOptionValue("role");
                Role role;
                try {
                    role = Role.valueOf(strRole);
                } catch (IllegalArgumentException e) {
                    throw new InvalidRoleException(strRole);
                }

                // Auth resource
                authService.authResource(resource, role);

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
                    format.setLenient(false);

                    authService.getLogOnUserAccounting().setLoginDate(format.parse(commandLine.getOptionValue("ds")));
                    authService.getLogOnUserAccounting().setLogoutDate(format.parse(commandLine.getOptionValue("de")));
                    authService.getLogOnUserAccounting().increaseVolume(Integer.parseInt(commandLine.getOptionValue("vol")));

                    if (!authService.getLogOnUserAccounting().getLoginDate().before(authService.getLogOnUserAccounting().getLogoutDate()))
                        throw new NumberFormatException("Incorrect login or logout dates!");

                    if (authService.getLogOnUserAccounting().getVolume() <= 0)
                        throw new NumberFormatException("Invalid volume number!");
                }

                authService.saveAccounting();
            }

            authService.clearAll();
        } catch (ParseException e) {
            ConsoleMain.printHelp(options);
            return ResultCode.INVALIDINPUT;
        } catch (UserNotFoundException e) {
            ConsoleMain.logger.warn("User {} not found in database!", e.getCauseUserName());
            return ResultCode.USERNOTFOUND;
        } catch (IncorrectPasswordException e) {
            ConsoleMain.logger.warn("Incorrect password {} for user {}!", e.getCausePassword(), e.getCauseUserName());
            return ResultCode.INCORRECTPASSWORD;
        } catch (InvalidRoleException e) {
            List<String> result = new ArrayList<>();

            for (Role role : Role.values())
                result.add(role.name());

            ConsoleMain.logger.warn("Invalid role: {}. Valid values are: {}", e.getCauseStr(), String.join(", ", result));
            return ResultCode.INVALIDROLE;
        } catch (ResourceNotFoundException | ResourceDeniedException e) {
            ConsoleMain.logger.warn("Resource {} {} for user {}!", e.getCauseResource(), e instanceof ResourceNotFoundException ? "not found" : "denied", e.getCauseUserName());
            return ResultCode.RESOURCEDENIED;
        } catch (java.text.ParseException | NumberFormatException e) {
            ConsoleMain.logger.warn("Incorrect activity for {}", e instanceof NumberFormatException ? "volume" : "start date or end date");
            return ResultCode.INCORRECTACTIVITY;
        } catch (SQLException e) {
            ConsoleMain.logger.error("Unknown error", e);
            return ResultCode.UNKNOWNERROR;
        }

        return ResultCode.SUCCESS;
    }

    public static void main(String[] args) {
        ResultCode result = new ConsoleMain().work(args);
        System.exit(result.getValue());
    }

    public enum ResultCode {
        SUCCESS(0),
        USERNOTFOUND(1),
        INCORRECTPASSWORD(2),
        INVALIDROLE(3),
        RESOURCEDENIED(4),
        INCORRECTACTIVITY(5),
        INVALIDINPUT(255),
        UNKNOWNERROR(255);

        @Getter
        private final int value;

        ResultCode(int i) {
            value = i;
        }
    }
}