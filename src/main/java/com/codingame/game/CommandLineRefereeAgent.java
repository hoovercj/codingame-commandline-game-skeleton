package com.codingame.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class CommandLineRefereeAgent extends Agent {

    private OutputStream processStdin;
    private InputStream processStdout;
    private InputStream processStderr;
    private String[] commandLine;
    private Process process;

    public CommandLineRefereeAgent(String[] commandLine) {
        super();
        try {
            this.commandLine = commandLine;
        } catch (Exception e) {
        }
    }

    @Override
    protected OutputStream getInputStream() {
        return processStdin;
    }

    @Override
    protected InputStream getOutputStream() {
        return processStdout;
    }

    @Override
    protected InputStream getErrorStream() {
        return processStderr;
    }

    @Override
    public void execute() {

        try {
            this.process = Runtime.getRuntime().exec(commandLine, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to launch " + commandLine, e);
        }
        processStdin = process.getOutputStream();
        processStdout = process.getInputStream();
        processStderr = process.getErrorStream();

        super.execute();
    }

    @Override
    public String getOutput(int nbLine, long timeout) {
        String output = super.getOutput(nbLine, timeout);
        return output;
    }

    /**
     * Launch the agent. After the call, agent is ready to process input / output
     *
     * @throws Exception
     *             if an error occurs
     */

    @Override
    protected void runInputOutput() throws Exception {

    }

    @Override
    public void destroy() {
    }
}
