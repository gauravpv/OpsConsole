$ErrorActionPreference = "Stop"
$root = "D:\Personal Projects\Dashboard"

$classToFqn = @{}
Get-ChildItem (Join-Path $root "src\main\java\com\opsconsole") -Recurse -Filter "*.java" | ForEach-Object {
    $rel = $_.FullName.Replace((Join-Path $root "src\main\java\"), "").Replace('\', '/')
    if ($rel -eq "com/opsconsole/OpsConsoleApplication.java") { return }
    $class = [System.IO.Path]::GetFileNameWithoutExtension($rel)
    $fqn = ($rel -replace '\.java$', '') -replace '/', '.'
    $classToFqn[$class] = $fqn
}

function Fix-File([string]$filePath) {
    $content = Get-Content $filePath -Raw
    $original = $content

    # Remove invalid package-only imports added by mistake
    $content = [regex]::Replace($content, '(?m)^import com\.opsconsole\.[^;]+\.(config|domain|dto|exception|repository|service|controller|security|ssh|util);\s*\r?\n', '')

    if ($content -notmatch '(?m)^package\s+([^;]+);') { return }
    $pkg = $Matches[1].Trim()

    $needed = New-Object System.Collections.Generic.HashSet[string]
    foreach ($class in $classToFqn.Keys) {
        $fqn = $classToFqn[$class]
        $targetPkg = $fqn.Substring(0, $fqn.LastIndexOf('.'))
        if ($targetPkg -eq $pkg) { continue }
        if ($content -notmatch "\b$class\b") { continue }
        if ($content -match "import\s+$([regex]::Escape($fqn));") { continue }
        [void]$needed.Add("import $fqn;")
    }

    foreach ($importLine in ($needed | Sort-Object)) {
        if ($content -notmatch [regex]::Escape($importLine)) {
            if ($content -match '(?m)^import\s+') {
                $lastImport = [regex]::Matches($content, '(?m)^import\s+[^;]+;') | Select-Object -Last 1
                $insertAt = $lastImport.Index + $lastImport.Length
                $content = $content.Insert($insertAt, "`r`n" + $importLine)
            } else {
                $content = [regex]::Replace($content, '(?m)(^package\s+[^;]+;\s*)', "`$1`r`n$importLine`r`n", 1)
            }
        }
    }

    $content = [regex]::Replace($content, '(\r?\n){3,}', "`r`n`r`n")

    if ($content -ne $original) {
        Set-Content -Path $filePath -Value $content -NoNewline
    }
}

Get-ChildItem (Join-Path $root "src") -Recurse -Filter "*.java" | ForEach-Object {
    Fix-File $_.FullName
}

Write-Host "Import cleanup complete."
