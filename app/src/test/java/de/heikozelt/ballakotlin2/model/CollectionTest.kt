package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Test
import java.util.Deque
import java.util.LinkedList
import java.util.Vector

class CollectionTest {

    @Test
    fun test_collection() {
        //val collection = Vector<String>()
        // Beginnt mit Object-Array mit Größe 10, dann 20, dann 40, dann 80, ...
        // increment = 0 bedeutet Verdopplung
        // Vector is synchronized

        //val collection = Vector<String>(8, 4)
        // Beginnt mit Object-Array mit Größe 8, dann 12, dann 16, ...

        //val collection = ArrayList<String>()
        // Beginnt mit Object-Array mitt Größe 0, dann 10, dann 15, dann 22, dann 33
        // also immer 50 % mehr
        // initialCapacity ist konfigurierbar
        // ArrayList ist nicht synchronized

        //val collection = LinkedList<String>()
        // für jedes Element gibt es eine Datenstruktur mit 3 Referenzen
        // item, next und prev
        // Benötigt viel Heap

        val collection = ArrayDeque<String>()
        // Beginnt mit Object-Array der Größe 0, dann 10, dann 15

        for(i in 0 until 100) {
            collection.add("element #$i")
        }
    }
}