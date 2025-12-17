/*
 * ProxyChat, a Velocity chat solution
 * Copyright (C) 2020 James Lyne
 *
 * Based on BungeeChat2 (https://github.com/AuraDevelopmentTeam/BungeeChat2)
 * Copyright (C) 2020 Aura Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    id("proxy-chat.java-conventions")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":ProxyChatApi"))
    implementation(":MessagesTranslator-1.4.3.41-DEV")
    implementation(libs.config)

    compileOnly(libs.vanishBridgeApi)
    compileOnly(libs.platformDetection)
    compileOnly(libs.proxyDiscordApi)

    testImplementation(project(":ProxyChatApi"))
    testImplementation(libs.junit)
    testImplementation(libs.mariadbServer)
    testImplementation(libs.commonsIO)
    testImplementation(libs.mockito)
    testImplementation(libs.powermockMockito)
    testImplementation(libs.powermockJunit)

    testCompileOnly(libs.spotbugsAnnotations)
}

description = "A velocity chat solution"

tasks {
    shadowJar {
        archiveClassifier = ""
        relocate("uk.co.notnull.VanishBridge", "uk.co.notnull.proxyqueues.shaded.vanishbridge")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("velocity-plugin.json") {
            expand("version" to project.version)
        }
    }
}
