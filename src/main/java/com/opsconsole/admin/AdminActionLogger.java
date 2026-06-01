package com.opsconsole.admin;

import com.opsconsole.auth.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminActionLogger {

    private final AdminActionLogRepository actionLogRepository;

    public AdminActionLogger(AdminActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }

    @Transactional
    public void log(AppUser actor, ManagedService service, AdminAction action, SshCommandResult result) {
        actionLogRepository.save(new AdminActionLog(
                actor.getId(),
                actor.getDisplayName(),
                service.getId(),
                service.getName(),
                action,
                result.success() ? AdminActionStatus.SUCCESS : AdminActionStatus.FAILED,
                summarize(result)
        ));
    }

    private static String summarize(SshCommandResult result) {
        String out = result.stdout() == null ? "" : result.stdout().trim();
        String err = result.stderr() == null ? "" : result.stderr().trim();
        if (!out.isBlank() && !err.isBlank()) {
            return out + " | " + err;
        }
        if (!out.isBlank()) {
            return out;
        }
        if (!err.isBlank()) {
            return err;
        }
        return "exit " + result.exitCode();
    }
}
