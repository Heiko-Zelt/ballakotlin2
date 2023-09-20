package de.heikozelt.ballakotlin2.model.solver

import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.SearchResult

interface Solver {

    fun findSolution(gs: GameState): SearchResult
}