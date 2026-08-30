$ErrorActionPreference = "Stop"
$root = "D:\Personal Projects\Dashboard"
$javaRoots = @(
    (Join-Path $root "src\main\java\com\opsconsole"),
    (Join-Path $root "src\test\java\com\opsconsole")
)

# old relative path (under com/opsconsole) -> new relative path
$relMoves = [ordered]@{
    "activity/ActivityEvent.java" = "activity/domain/ActivityEvent.java"
    "activity/ActivityType.java" = "activity/domain/ActivityType.java"
    "activity/ActivityFeedService.java" = "activity/service/ActivityFeedService.java"

    "admin/ServiceAdminApiController.java" = "admin/controller/ServiceAdminApiController.java"
    "admin/AdminApiExceptionHandler.java" = "admin/controller/AdminApiExceptionHandler.java"
    "admin/ServiceAdminService.java" = "admin/service/ServiceAdminService.java"
    "admin/AdminActionLogger.java" = "admin/service/AdminActionLogger.java"
    "admin/AdminActionLogMaintenance.java" = "admin/service/AdminActionLogMaintenance.java"
    "admin/AdminCatalogInitializer.java" = "admin/service/AdminCatalogInitializer.java"
    "admin/AdminActionLogRepository.java" = "admin/repository/AdminActionLogRepository.java"
    "admin/ManagedServerRepository.java" = "admin/repository/ManagedServerRepository.java"
    "admin/ManagedServiceRepository.java" = "admin/repository/ManagedServiceRepository.java"
    "admin/AdminAction.java" = "admin/domain/AdminAction.java"
    "admin/AdminActionLog.java" = "admin/domain/AdminActionLog.java"
    "admin/AdminActionStatus.java" = "admin/domain/AdminActionStatus.java"
    "admin/ManagedServer.java" = "admin/domain/ManagedServer.java"
    "admin/ManagedService.java" = "admin/domain/ManagedService.java"
    "admin/SshCommandResult.java" = "admin/domain/SshCommandResult.java"
    "admin/AdminProperties.java" = "admin/config/AdminProperties.java"
    "admin/SshExecutorConfiguration.java" = "admin/config/SshExecutorConfiguration.java"
    "admin/ServiceAdminException.java" = "admin/exception/ServiceAdminException.java"
    "admin/DevSshRemoteExecutor.java" = "admin/ssh/DevSshRemoteExecutor.java"
    "admin/LiveSshRemoteExecutor.java" = "admin/ssh/LiveSshRemoteExecutor.java"
    "admin/SshRemoteExecutor.java" = "admin/ssh/SshRemoteExecutor.java"
    "admin/AdminActionLabels.java" = "admin/util/AdminActionLabels.java"
    "admin/AdminPathValidator.java" = "admin/util/AdminPathValidator.java"
    "admin/SshOutputFormatter.java" = "admin/util/SshOutputFormatter.java"

    "apitester/ApiTesterController.java" = "apitester/controller/ApiTesterController.java"
    "apitester/ApiTesterProxyService.java" = "apitester/service/ApiTesterProxyService.java"
    "apitester/ApiTesterProxyRequest.java" = "apitester/dto/ApiTesterProxyRequest.java"
    "apitester/ApiTesterProxyResponse.java" = "apitester/dto/ApiTesterProxyResponse.java"

    "auth/RoleAdminApiController.java" = "auth/controller/RoleAdminApiController.java"
    "auth/RoleAdminService.java" = "auth/service/RoleAdminService.java"
    "auth/NavAccessService.java" = "auth/service/NavAccessService.java"
    "auth/UserProvisioningService.java" = "auth/service/UserProvisioningService.java"
    "auth/OpsUserDetailsService.java" = "auth/service/OpsUserDetailsService.java"
    "auth/AzureOidcUserService.java" = "auth/service/AzureOidcUserService.java"
    "auth/AuthDataInitializer.java" = "auth/service/AuthDataInitializer.java"
    "auth/AppUserRepository.java" = "auth/repository/AppUserRepository.java"
    "auth/AppRoleRepository.java" = "auth/repository/AppRoleRepository.java"
    "auth/RoleTabAccessRepository.java" = "auth/repository/RoleTabAccessRepository.java"
    "auth/AppUser.java" = "auth/domain/AppUser.java"
    "auth/AppRole.java" = "auth/domain/AppRole.java"
    "auth/AppTab.java" = "auth/domain/AppTab.java"
    "auth/RoleTabAccess.java" = "auth/domain/RoleTabAccess.java"
    "auth/OpsUserPrincipal.java" = "auth/domain/OpsUserPrincipal.java"
    "auth/OpsOidcUser.java" = "auth/domain/OpsOidcUser.java"
    "auth/CurrentUser.java" = "auth/domain/CurrentUser.java"
    "auth/SecurityConfig.java" = "auth/security/SecurityConfig.java"
    "auth/AuthSecurityBeans.java" = "auth/security/AuthSecurityBeans.java"
    "auth/LoginSuccessHandler.java" = "auth/security/LoginSuccessHandler.java"
    "auth/NavAccessInterceptor.java" = "auth/security/NavAccessInterceptor.java"
    "auth/WebMvcAuthConfig.java" = "auth/security/WebMvcAuthConfig.java"
    "auth/AuthModelAdvice.java" = "auth/security/AuthModelAdvice.java"
    "auth/AuthProperties.java" = "auth/config/AuthProperties.java"
    "web/LoginController.java" = "auth/controller/LoginController.java"
    "web/UserAdminController.java" = "auth/controller/UserAdminController.java"

    "health/api/HealthApiController.java" = "health/controller/HealthApiController.java"
    "health/web/HealthPageController.java" = "health/controller/HealthPageController.java"
    "health/SystemHealthMonitor.java" = "health/service/SystemHealthMonitor.java"
    "health/ActuatorHealthService.java" = "health/service/ActuatorHealthService.java"
    "health/ModelHubHealthService.java" = "health/service/ModelHubHealthService.java"
    "health/ModelHubWatchlistService.java" = "health/service/ModelHubWatchlistService.java"
    "health/MonitorRegistrationService.java" = "health/service/MonitorRegistrationService.java"
    "health/HealthHistoryService.java" = "health/service/HealthHistoryService.java"
    "health/MonitorCatalogInitializer.java" = "health/service/MonitorCatalogInitializer.java"
    "health/MonitoredHostRepository.java" = "health/repository/MonitoredHostRepository.java"
    "health/MonitoredHost.java" = "health/domain/MonitoredHost.java"
    "health/HealthStatus.java" = "health/domain/HealthStatus.java"
    "health/SystemHealthView.java" = "health/domain/SystemHealthView.java"
    "health/HealthRegionGroup.java" = "health/domain/HealthRegionGroup.java"
    "health/ServiceEndpointMatcher.java" = "health/domain/ServiceEndpointMatcher.java"
    "health/dto/HealthRefreshResponse.java" = "health/dto/HealthRefreshResponse.java"
    "health/MonitorDetailsResponse.java" = "health/dto/MonitorDetailsResponse.java"
    "health/RegisterMonitorRequest.java" = "health/dto/RegisterMonitorRequest.java"
    "health/ModelHubEnvironmentOption.java" = "health/dto/ModelHubEnvironmentOption.java"
    "health/HealthProperties.java" = "health/config/HealthProperties.java"
    "health/MonitorRegistrationException.java" = "health/exception/MonitorRegistrationException.java"

    "web/PageController.java" = "web/controller/PageController.java"
    "common/api/ErrorResponse.java" = "common/dto/ErrorResponse.java"
}

function Get-NewPackage([string]$relPath) {
    $dir = Split-Path $relPath -Parent
    $dir = $dir -replace '\\', '.'
    $dir = $dir -replace '/', '.'
    if ([string]::IsNullOrEmpty($dir)) {
        return "com.opsconsole"
    }
    return "com.opsconsole.$dir"
}

$importReplacements = @()
foreach ($entry in $relMoves.GetEnumerator()) {
    $oldRel = $entry.Key -replace '\\', '/'
    $newRel = $entry.Value -replace '\\', '/'
    $oldClass = [System.IO.Path]::GetFileNameWithoutExtension($oldRel)
    $oldDir = Split-Path $oldRel -Parent
    if ($oldDir -eq "") { $oldFqn = "com.opsconsole.$oldClass" }
    else { $oldFqn = "com.opsconsole.$($oldDir -replace '/','.').$oldClass" }
    $newFqn = "$(Get-NewPackage $newRel).$oldClass"
    if ($oldFqn -ne $newFqn) {
        $importReplacements += [pscustomobject]@{ Old = $oldFqn; New = $newFqn; Len = $oldFqn.Length }
    }
}
$importReplacements = $importReplacements | Sort-Object Len -Descending

foreach ($javaRoot in $javaRoots) {
    foreach ($entry in $relMoves.GetEnumerator()) {
        $oldRel = $entry.Key -replace '\\', '/'
        $newRel = $entry.Value -replace '\\', '/'
        $oldPath = Join-Path $javaRoot ($oldRel -replace '/', '\')
        $newPath = Join-Path $javaRoot ($newRel -replace '/', '\')
        if (-not (Test-Path $oldPath)) { continue }
        $newDir = Split-Path $newPath -Parent
        New-Item -ItemType Directory -Force -Path $newDir | Out-Null
        $pkg = Get-NewPackage $newRel
        $content = Get-Content $oldPath -Raw
        if ($content -match '(?m)^package\s+[^;]+;') {
            $content = [regex]::Replace($content, '(?m)^package\s+[^;]+;', "package $pkg;")
        }
        Set-Content -Path $newPath -Value $content -NoNewline
        if ($oldPath -ne $newPath) {
            Remove-Item $oldPath -Force
        }
    }
}

$allJava = Get-ChildItem (Join-Path $root "src") -Recurse -Filter "*.java"
foreach ($file in $allJava) {
    $content = Get-Content $file.FullName -Raw
    $updated = $content
    foreach ($rep in $importReplacements) {
        $updated = $updated.Replace($rep.Old, $rep.New)
    }
    if ($updated -ne $content) {
        Set-Content -Path $file.FullName -Value $updated -NoNewline
    }
}

# Update OpsConsoleApplication imports
$appPath = Join-Path $root "src\main\java\com\opsconsole\OpsConsoleApplication.java"
$app = Get-Content $appPath -Raw
$app = $app.Replace("com.opsconsole.admin.AdminProperties", "com.opsconsole.admin.config.AdminProperties")
$app = $app.Replace("com.opsconsole.auth.AuthProperties", "com.opsconsole.auth.config.AuthProperties")
$app = $app.Replace("com.opsconsole.health.HealthProperties", "com.opsconsole.health.config.HealthProperties")
Set-Content -Path $appPath -Value $app -NoNewline

Write-Host "Migration complete. $($importReplacements.Count) import mappings applied."
