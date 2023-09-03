package de.heikozelt.ballakotlin2.model

interface Solver {

    suspend fun findSolution(gs: GameState): SearchResult
}