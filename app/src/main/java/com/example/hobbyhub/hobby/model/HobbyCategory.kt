package com.example.hobbyhub.hobby.model

enum class HobbyCategory(private val displayName: String) {
    ARTS_CRAFTS("Arts & Crafts"),
    FOOD_COOKING("Food & Cooking"),
    OUTDOOR_ACTIVITIES("Outdoor Activities"),
    FITNESS_WELLNESS("Fitness & Wellness"),
    TECHNOLOGY_SCIENCE("Technology & Science"),
    MUSIC_PERFORMANCE("Music & Performance"),
    MISCELLANEOUS("Miscellaneous");

    override fun toString(): String {
        return displayName
    }
}
