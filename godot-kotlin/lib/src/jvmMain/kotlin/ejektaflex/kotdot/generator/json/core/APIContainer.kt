package ejektaflex.kotdot.generator.json.core

data class APIContainer(
        val type: String,
        val version: APIVersion,
        val next: APIContainer? = null,
        val api: List<CoreMethod>
) {
    val allMethods: List<CoreMethod>
        get() {
            return mutableListOf<CoreMethod>() + api + (next?.api ?: listOf())
        }
}