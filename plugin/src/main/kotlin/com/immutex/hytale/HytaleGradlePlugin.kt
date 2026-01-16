package com.immutex.hytale

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.tasks.JavaExec
import java.io.File

open class HytaleExtension {
    var hytaleHome: String? = null
}

class HytaleGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("hytale", HytaleExtension::class.java)

        project.pluginManager.apply("java")

        project.afterEvaluate {
            val hytaleHomeDir = resolveHytaleHome(project, extension.hytaleHome)
            
            val installDir = File(hytaleHomeDir, "install")
            if (!installDir.exists()) {
                throw GradleException("hytale install directory not found at ${installDir.absolutePath}")
            }
            
            val patchlineDir = installDir.listFiles()?.firstOrNull { dir ->
                dir.isDirectory && File(dir, "package/game/latest/Server/HytaleServer.jar").exists()
            } ?: throw GradleException("could not find a valid hytale installation in ${installDir.absolutePath} (looking for package/game/latest/Server/HytaleServer.jar)")
            
            val serverJar = File(patchlineDir, "package/game/latest/Server/HytaleServer.jar")
            val assetsZip = File(patchlineDir, "package/game/latest/Assets.zip")
            
            project.logger.lifecycle("found hytale server jar at: ${serverJar.absolutePath}")
            project.dependencies.add("implementation", project.files(serverJar))
            
            project.tasks.register("runServer", JavaExec::class.java) { task ->
                task.group = "hytale"
                task.description = "runs the hytale test server"
                
                task.classpath = project.sourceSets.getByName("main").runtimeClasspath
                task.mainClass.set("com.hypixel.hytale.Main")
                
                val runDir = project.file("run")
                if (!runDir.exists()) runDir.mkdirs()
                task.workingDir = runDir
                
                val argsList = mutableListOf<String>()
                argsList.add("--allow-op")
                argsList.add("--disable-sentry")
                if (assetsZip.exists()) {
                    argsList.add("--assets=${assetsZip.absolutePath}")
                }
                
                val mainSourceSet = project.sourceSets.getByName("main")
                val resourceDir = mainSourceSet.resources.srcDirs.firstOrNull()
                
                val modPaths = mutableListOf<String>()
                
                if (resourceDir != null && resourceDir.parentFile.exists()) {
                     modPaths.add(resourceDir.parentFile.absolutePath)
                }

                val userModsDir = File(hytaleHomeDir, "UserData/Mods")
                if (userModsDir.exists()) {
                    modPaths.add(userModsDir.absolutePath)
                }
                
                if (modPaths.isNotEmpty()) {
                    argsList.add("--mods=${modPaths.joinToString(",")}")
                }
                
                task.args = argsList
                task.standardInput = System.`in`
            }
        }
    }
    
    private fun resolveHytaleHome(project: Project, configuredPath: String?): File {
        if (configuredPath != null) {
            return File(configuredPath)
        }
        
        val userHome = System.getProperty("user.home")
        val os = System.getProperty("os.name").lowercase()
        
        val path = when {
            os.contains("win") -> "$userHome/AppData/Roaming/Hytale"
            os.contains("mac") -> "$userHome/Library/Application Support/Hytale"
            os.contains("nux") || os.contains("nix") -> {
                val flatpak = "$userHome/.var/app/com.hypixel.HytaleLauncher/data/Hytale"
                if (File(flatpak).exists()) flatpak else "$userHome/.local/share/Hytale"
            }
            else -> throw GradleException("unsupported os for automatic hytale detection")
        }
        
        val file = File(path)
        if (!file.exists()) {
             throw GradleException("hytale not found at $path. please configure 'hytale.hytaleHome'")
        }
        return file
    }
    
    private val Project.sourceSets: org.gradle.api.tasks.SourceSetContainer
        get() = extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
}