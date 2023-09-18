package de.heikozelt.ballakotlin2.model

interface Solver {

    fun findSolution(gs: GameState): SearchResult
}