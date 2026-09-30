package com.example.dogapp.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class ArchitectureTest {

    private data class SourceFile(val path: String, val packageName: String, val imports: List<String>)

    private val root = "com.example.dogapp"
    private val mainSourceSet = Regex("(^|/)src/[A-Za-z]+Main/kotlin/")
    private val layeredPackage = Regex("""^${Regex.escape(root)}\.(app|core\.(domain|data|presentation)|feature\.\w+\.(domain|data|presentation|di))(\..+)?$""")
    private val featurePackage = Regex("""^${Regex.escape(root)}\.feature\.(\w+)(\..+)?$""")
    private val presentationPackage = Regex("""^${Regex.escape(root)}\.(core|feature\.\w+)\.presentation(\..+)?$""")
    private val dataPackage = Regex("""^${Regex.escape(root)}\.(core|feature\.\w+)\.data(\..+)?$""")
    private val domainPackage = Regex("""^${Regex.escape(root)}\.(core|feature\.\w+)\.domain(\..+)?$""")
    private val domainAllowedImportPrefixes = listOf("kotlin.", "kotlinx.coroutines.", "$root.core.domain.")

    private val sourceFiles: List<SourceFile> = File("src").walkTopDown()
        .filter { it.isFile && it.extension == "kt" && mainSourceSet.containsMatchIn(it.invariantSeparatorsPath) }
        .map { file ->
            val lines = file.readLines()
            SourceFile(
                path = file.invariantSeparatorsPath,
                packageName = lines.first { it.startsWith("package ") }.removePrefix("package ").trim(),
                imports = lines.filter { it.startsWith("import ") }.map { it.removePrefix("import ").substringBefore(" as ").trim() },
            )
        }
        .toList()

    private fun assertNoViolations(rule: String, violations: List<String>) {
        if (violations.isNotEmpty()) fail("$rule\n" + violations.joinToString("\n"))
    }

    private fun importViolations(appliesTo: (SourceFile) -> Boolean, forbidden: (SourceFile, String) -> Boolean): List<String> =
        sourceFiles.filter(appliesTo).flatMap { file ->
            file.imports.filter { forbidden(file, it) }.map { "${file.path}: import $it" }
        }

    @Test
    fun mainSourcesAreScanned() {
        assertTrue(sourceFiles.isNotEmpty(), "No main source files found under ${File("src").absolutePath}")
    }

    @Test
    fun packagesFollowLayerLayout() {
        assertNoViolations(
            "Packages must be the root package, app, core.{domain,data,presentation} or feature.<name>.{domain,data,presentation,di}:",
            sourceFiles
                .filter { it.packageName != root && !layeredPackage.matches(it.packageName) }
                .map { "${it.path}: package ${it.packageName}" },
        )
    }

    @Test
    fun domainDependsOnlyOnKotlinCoroutinesAndDomain() {
        assertNoViolations(
            "Domain must not depend on frameworks, platforms, data or presentation:",
            importViolations(
                appliesTo = { domainPackage.matches(it.packageName) },
                forbidden = { file, import ->
                    val ownFeatureDomain = featurePackage.find(file.packageName)?.let { "$root.feature.${it.groupValues[1]}.domain." }
                    domainAllowedImportPrefixes.none { import.startsWith(it) } &&
                        (ownFeatureDomain == null || !import.startsWith(ownFeatureDomain))
                },
            ),
        )
    }

    @Test
    fun dataDoesNotDependOnPresentationOrApp() {
        assertNoViolations(
            "Data must not depend on presentation or app:",
            importViolations(
                appliesTo = { dataPackage.matches(it.packageName) },
                forbidden = { _, import -> presentationPackage.matches(import.substringBeforeLast('.')) || import.startsWith("$root.app.") },
            ),
        )
    }

    @Test
    fun presentationDoesNotDependOnDataOrApp() {
        assertNoViolations(
            "Presentation must not depend on data or app:",
            importViolations(
                appliesTo = { presentationPackage.matches(it.packageName) },
                forbidden = { _, import -> dataPackage.matches(import.substringBeforeLast('.')) || import.startsWith("$root.app.") },
            ),
        )
    }

    @Test
    fun coreDoesNotDependOnFeaturesOrApp() {
        assertNoViolations(
            "Core must not depend on features or app:",
            importViolations(
                appliesTo = { it.packageName.startsWith("$root.core.") },
                forbidden = { _, import -> import.startsWith("$root.feature.") || import.startsWith("$root.app.") },
            ),
        )
    }

    @Test
    fun featuresDoNotDependOnEachOther() {
        assertNoViolations(
            "Features must not depend on other features:",
            importViolations(
                appliesTo = { featurePackage.matches(it.packageName) },
                forbidden = { file, import ->
                    val ownFeature = featurePackage.find(file.packageName)!!.groupValues[1]
                    val importedFeature = featurePackage.find(import)?.groupValues?.get(1)
                    importedFeature != null && importedFeature != ownFeature
                },
            ),
        )
    }
}
