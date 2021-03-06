import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sonarqube.gradle.SonarQubeExtension

/***
 * This plugin is instantiated every time when you apply the this plugin to build.gradle in feature module
 * <code>apply plugin: 'dev.nikhi1.plugin.android'</code>
 */
class AndroidModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (project.hasProperty("android")) {
            with(project) {
                plugins.apply("kotlin-android")
                plugins.apply("kotlin-android-extensions")
                plugins.apply("kotlin-kapt")
                plugins.apply("com.hiya.jacoco-android")
                configureSonarqube()
                configureJacoco()
                configureAndroidBlock()
                if (name != "test_shared") {
                    configureCommonDependencies()
                    configureCoreModuleForOtherModules()
                }
                configureTestSharedDependencies()
                configureTestSharedModuleForOtherModules()
            }
        }
    }
}

internal fun Project.configureAndroidBlock() = extensions.getByType<BaseExtension>().run {

    buildToolsVersion(Versions.Android.buildTools)
    compileSdkVersion(Versions.Android.compileSDK)

    defaultConfig {
        minSdkVersion(Versions.Android.minSDK)
        targetSdkVersion(Versions.Android.targetSDK)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dataBinding.isEnabled = true

    tasks.withType(KotlinCompile::class.java).configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }

    testOptions {
        unitTests.apply {
            isReturnDefaultValues = true
        }
    }

    buildTypes {
        getByName("debug") {
            isTestCoverageEnabled = false
        }
    }
}

internal fun Project.configureCommonDependencies() {

    extensions.getByType<BaseExtension>().run {
        dependencies {
            add("implementation", Libs.Design.material)
            add("implementation", Libs.AndroidX.coreKtx)
            add("implementation", Libs.Networking.retrofit)
            add("implementation", Libs.koinViewModel)
            add("implementation", Libs.AndroidX.Lifecycle.ext)
            add("implementation", Libs.AndroidX.Lifecycle.viewModel)
            add("implementation", Libs.AndroidX.Lifecycle.viewModelKtx)
            add("implementation", Libs.AndroidX.Lifecycle.liveDataKtx)
            add("implementation", Libs.coroutines)
            add("implementation", Libs.AndroidX.coroutines)
            add("implementation", Libs.Networking.gson)
            add("implementation", Libs.Networking.responseConverter)

            if (name != "core") {
                add("implementation", Libs.AndroidX.Navigation.navigator)
            }
        }
    }
}

internal fun Project.configureTestSharedDependencies() {
    if (name != "test_shared") return

    extensions.getByType<BaseExtension>().run {
        dependencies {

            add("implementation", Libs.AndroidX.Lifecycle.ext)
            add("implementation", Libs.AndroidX.Lifecycle.viewModel)
            add("implementation", Libs.AndroidX.Lifecycle.viewModelKtx)
            add("implementation", Libs.AndroidX.Lifecycle.liveDataKtx)

            add("implementation", Libs.Testing.junit)
            add("implementation", Libs.Testing.mockk)
            add("implementation", Libs.Testing.archCore)
            add("implementation", Libs.Testing.corountines)
        }
    }
}

internal fun Project.configureCoreModuleForOtherModules() {
    if (name == "core") return
    val core = findProject(":core") as Project
    extensions.getByName("android").apply {
        when (this) {
            is BaseExtension -> {
                dependencies {
                    add("implementation", core)
                }
            }
        }
    }
}

internal fun Project.configureTestSharedModuleForOtherModules() {
    if (name == "test_shared") return
    val test_shared = findProject(":test_shared") as Project
    extensions.getByName("android").apply {
        when (this) {
            is BaseExtension -> {
                dependencies {
                    add("testImplementation", test_shared)
                    add("testImplementation", Libs.Testing.junit)
                    add("testImplementation", Libs.Testing.mockk)
                    add("testImplementation", Libs.Testing.archCore)
                    add("testImplementation", Libs.Testing.corountines)
                }
            }
        }
    }
}

internal fun Project.configureSonarqube() {
    val plugin = rootProject.plugins.findPlugin("org.sonarqube")
    if (plugin == null) {
        println("Applying sonar qube")
        rootProject.plugins.apply("org.sonarqube")
        rootProject.extensions.getByType<SonarQubeExtension>().run {
            properties {
                property("sonar.projectKey", "nikhil-thakkar_eventbrite-clone")
                property("sonar.organization", "nikhil-thakkar")
                property("sonar.sources", "src/main/java")
                property("sonar.sources.coveragePlugin", "jacoco")
                property("sonar.host.url", "https://sonarcloud.io/")
                property("sonar.exclusions", "**/*.js,**/test/**, buildSrc/*")
                property("sonar.login", "")

            }
        }
    }
}

internal fun Project.configureJacoco() {
    if(name == "test_shared") return

    apply("${rootDir}/buildSrc/jacoco.gradle")
    apply("https://raw.githubusercontent.com/JakeWharton/SdkSearch/master/gradle/projectDependencyGraph.gradle")
    extensions.getByType<SonarQubeExtension>().run {
        properties {
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                "${buildDir}/jacoco/jacoco.xml"
            )
        }
    }
}