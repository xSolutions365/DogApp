package com.example.dogapp.core.data.security

object PinnedHosts {
    private const val GTS_ROOT_R4 = "mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="
    private const val GTS_ROOT_R1 = "hxqRlPTu1bMS/0DITB1SSu0vd4u/8l8TjPgfaAp63Gc="
    private const val AMAZON_ROOT_CA_1 = "++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI="
    private const val AMAZON_ROOT_CA_3 = "NqvDJlas/GRcYbcWE8S/IceH9cq77kg0jVhZeAPXq8k="

    private val googleTrustServices = setOf(GTS_ROOT_R4, GTS_ROOT_R1)
    private val amazonTrustServices = setOf(AMAZON_ROOT_CA_1, AMAZON_ROOT_CA_3)

    val pins: Map<String, Set<String>> =
        mapOf(
            "api.thedogapi.com" to googleTrustServices,
            "dog.ceo" to googleTrustServices,
            "api.api-ninjas.com" to amazonTrustServices,
        )
}
