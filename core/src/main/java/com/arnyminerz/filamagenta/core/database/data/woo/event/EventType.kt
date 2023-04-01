package com.arnyminerz.filamagenta.core.database.data.woo.event

enum class EventType(val keywords: Set<String>) {
    Breakfast(setOf("esmorzar", "almuerzo", "desayuno")),
    Lunch(setOf("comida", "dinar")),
    Dinner(setOf("cena", "sopar")),
}
