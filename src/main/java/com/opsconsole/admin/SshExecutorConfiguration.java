package com.opsconsole.admin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SshExecutorConfiguration {

    @Bean
    DevSshRemoteExecutor devSshRemoteExecutor() {
        return new DevSshRemoteExecutor();
    }

    @Bean
    LiveSshRemoteExecutor liveSshRemoteExecutor(AdminProperties adminProperties) {
        return new LiveSshRemoteExecutor(adminProperties);
    }

    @Bean
    public SshRemoteExecutor sshRemoteExecutor(
            AdminProperties adminProperties,
            DevSshRemoteExecutor devExecutor,
            LiveSshRemoteExecutor liveExecutor
    ) {
        if (adminProperties.isDevMode()) {
            return devExecutor;
        }
        return liveExecutor;
    }
}
