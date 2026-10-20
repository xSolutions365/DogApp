import com.android.build.api.dsl.ApkSigningConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.security.KeyStore
import java.security.MessageDigest

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    ktlintRuleset(libs.compose.rules.ktlint)
}

android {
    namespace = "com.example.dogapp"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.example.dogapp"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    androidResources {
        noCompress += "tflite"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            buildConfigField("String", "SIGNING_CERT_SHA256", "\"${signingCertSha256(signingConfig)}\"")
        }
        release {
            buildConfigField("String", "SIGNING_CERT_SHA256", "\"${signingCertSha256(signingConfig)}\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    lint {
        abortOnError = true
        warningsAsErrors = true
        checkDependencies = true
    }
}

ktlint {
    version.set(libs.versions.ktlint.get())
    filter {
        exclude { element -> element.file.path.contains("${File.separator}build${File.separator}") }
    }
}

fun signingCertSha256(config: ApkSigningConfig?): String {
    val storeFile = config?.storeFile?.takeIf { it.exists() } ?: return ""
    val keyStore = KeyStore.getInstance(config.storeType ?: KeyStore.getDefaultType())
    storeFile.inputStream().use { keyStore.load(it, config.storePassword?.toCharArray()) }
    val certificate = keyStore.getCertificate(config.keyAlias) ?: return ""
    return MessageDigest.getInstance("SHA-256").digest(certificate.encoded).joinToString("") { "%02X".format(it) }
}
