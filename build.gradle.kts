plugins {
    `maven-publish`
    kotlin("jvm") version "1.3.50"
}

group = "ru.quandastudio.lps"
version = "0.3.2"

val sourcesJar by tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts.add("archives", sourcesJar)

publishing {
    publications {
        create<MavenPublication>("lpsClientLibrary") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
    repositories {
        mavenLocal()
    }
}

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("tech.gusavila92:java-android-websocket-client:1.2.3")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation("junit:junit:4.12")
}