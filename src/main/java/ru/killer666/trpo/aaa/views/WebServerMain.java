package ru.killer666.trpo.aaa.views;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WebServerMain extends AbstractHandler {
    private static final Logger logger = LogManager.getLogger(WebServerMain.class);

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

    }

    public static void main(String[] args) throws Exception {
        Option optionPort = new Option("p", "port", true, "Port to listen");
        optionPort.setArgs(1);
        optionPort.setRequired(true);

        Options options = new Options()
                .addOption(optionPort);

        CommandLine commandLine = new DefaultParser().parse(options, args);
        int port = Integer.parseInt(commandLine.getOptionValue("port"));

        WebServerMain.logger.warn("Setting port to " + port);

        Server server = new Server(port);

        HashSessionIdManager idmanager = new HashSessionIdManager();
        server.setSessionIdManager(idmanager);

        ContextHandler context = new ContextHandler("/");
        server.setHandler(context);

        HashSessionManager manager = new HashSessionManager();
        SessionHandler sessions = new SessionHandler(manager);
        context.setHandler(sessions);

        sessions.setHandler(new WebServerMain());

        WebServerMain.logger.warn("Starting listening...");

        server.start();
        server.join();
    }
}
