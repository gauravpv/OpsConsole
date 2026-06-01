package com.opsconsole.admin;

public record SshCommandResult(int exitCode, String stdout, String stderr) {
    public boolean success() {
        return exitCode == 0;
    }
}
