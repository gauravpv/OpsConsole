package com.opsconsole.admin;

public interface SshRemoteExecutor {

    SshCommandResult execute(ManagedServer server, String command);

    SshCommandResult executeWithInput(ManagedServer server, String command, String stdin);
}
