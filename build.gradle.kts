plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.jgoodies:jgoodies-common:1.8.1")
    implementation("com.jgoodies:jgoodies-forms:1.9.0")
    implementation("io.vertx:vertx-core:4.4.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.6")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-preview")
}