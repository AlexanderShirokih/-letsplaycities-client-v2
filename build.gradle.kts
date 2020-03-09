import org.gradle.api.publish.maven.MavenPom
import groovy.util.Node

plugins {
    id("com.jfrog.bintray") version "1.8.1"
    `maven-publish`
    kotlin("jvm") version "1.3.61"
}

group = "ru.quandastudio.lps"
version = "1.0.1"

val artifactName = "lps-client"

dependencies {
    implementation("ru.aleshi:java-android-websocket-client:1.2.3")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("com.squareup.retrofit2:retrofit:2.7.1")
    implementation("com.squareup.retrofit2:converter-gson:2.7.1")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation("junit:junit:4.12")
}

val sourcesJar by tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts.add("archives", sourcesJar)

fun addDependencies(pom: MavenPom) = pom.withXml {
    val node = asNode()

    node.appendNode("name", "lps-client")
    node.appendNode("description", "API client for Letsplaycities game")
    node.appendNode("url", "https://github.com/AlexanderShirokih/letsplaycities-client-v2")

    val depsNode: Node =
        ((asNode().get("dependencies") as groovy.util.NodeList?)?.get(0) as Node?) ?: asNode().appendNode("dependencies")
    depsNode.let { depNode ->
        configurations.compile.get().allDependencies.forEach {
            depNode.appendNode("dependency").apply {
                appendNode("groupId", it.group)
                appendNode("artifactId", it.name)
                appendNode("version", it.version)
            }
        }
    }

}

val publicationName = "lpsClientLibrary"

publishing {
    publications {
        create<MavenPublication>("lpsClientLibrary") {
            artifactId = artifactName
            groupId = "ru.aleshi.lps"
            version = project.version.toString()
            from(components["java"])
            artifact(tasks["sourcesJar"])
            addDependencies(pom)
        }
    }
}

bintray {
    user = project.properties["bintray_user"] as String?
    key = project.properties["bintray_api_key"] as String?
    publish = true
    setPublications(publicationName)
    pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
        repo = "maven"
        name = artifactName
        desc = "API client for Letsplaycities game"
        githubRepo = "AlexanderShirokih/letsplaycities-client-v2"
        vcsUrl = "https://github.com/AlexanderShirokih/letsplaycities-client-v2"

        version(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.VersionConfig> {
            name = project.version.toString()
            gpg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.GpgConfig> {
                sign = true
                passphrase = project.properties["bintray_gpg_passphrase"] as String?
            })
        })
        setLabels("kotlin")
        setLicenses("Apache-2.0")
    })
}

repositories {
    maven(url = "https://dl.bintray.com/alexandershirokih/maven")
    jcenter()
    mavenCentral()
}