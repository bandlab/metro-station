package com.bandlab.metro.station

public interface HasStationEntries {

    public fun <T> findEntryFactory(): T

    public companion object {

        public fun <T> findFrom(graph: Any): T = try {
            @Suppress("UNCHECKED_CAST")
            graph as T
        } catch (e: Exception) {
            throw IllegalStateException(
                """
                Cannot find StationEntry factory in graph: $graph. Make sure your StationEntry has a correct 
                parentScope, and the module is seen by the graph. 
                """.trimIndent(),
                e
            )
        }
    }
}
