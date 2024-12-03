package com.example.hobbyhub.hobby.model

object HobbyData {
    val hobbies  = listOf(
        Hobby(
            name = "Photography",
            description = "Capture beautiful moments and learn the art of photography.",
            imageUrl = listOf(
                "https://example.com/images/photography1.jpg",
                "https://example.com/images/photography2.jpg"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),
        Hobby(
            name = "Painting",
            description = "Unleash your creativity with colors and brushes.",
            imageUrl = listOf(
                "https://example.com/images/painting1.jpg",
                "https://example.com/images/painting2.jpg"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),
        Hobby(
            name = "Cooking",
            description = "Learn to cook delicious meals and master culinary techniques.",
            imageUrl = listOf(
                "https://example.com/images/cooking1.jpg",
                "https://example.com/images/cooking2.jpg"
            ),
            category = HobbyCategory.FOOD_COOKING
        ),
        Hobby(
            name = "Hiking",
            description = "Explore nature and embark on thrilling hiking adventures.",
            imageUrl = listOf(
                "https://example.com/images/hiking1.jpg",
                "https://example.com/images/hiking2.jpg"
            ),
            category = HobbyCategory.OUTDOOR_ACTIVITIES
        ),
        Hobby(
            name = "Yoga",
            description = "Improve your flexibility and mindfulness through yoga.",
            imageUrl = listOf(
                "https://example.com/images/yoga1.jpg",
                "https://example.com/images/yoga2.jpg"
            ),
            category = HobbyCategory.FITNESS_WELLNESS
        ),
        Hobby(
            name = "Coding",
            description = "Learn to create software and apps with coding skills.",
            imageUrl = listOf(
                "https://example.com/images/coding1.jpg",
                "https://example.com/images/coding2.jpg"
            ),
            category = HobbyCategory.TECHNOLOGY_SCIENCE
        ),
        Hobby(
            name = "Playing Guitar",
            description = "Learn to play chords, melodies, and songs on the guitar.",
            imageUrl = listOf(
                "https://example.com/images/guitar1.jpg",
                "https://example.com/images/guitar2.jpg"
            ),
            category = HobbyCategory.MUSIC_PERFORMANCE
        ),
        Hobby(
            name = "Gardening",
            description = "Cultivate plants and flowers while connecting with nature.",
            imageUrl = listOf(
                "https://example.com/images/gardening1.jpg",
                "https://example.com/images/gardening2.jpg"
            ),
            category = HobbyCategory.MISCELLANEOUS
        ),
        // Arts & Crafts
        Hobby(
            name = "Knitting",
            description = "Create handmade sweaters, scarves, and other items using knitting needles and yarn.",
            imageUrl = listOf(
                "https://example.com/images/knitting1.jpg",
                "https://example.com/images/knitting2.jpg"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),
        Hobby(
            name = "Origami",
            description = "Learn the art of paper folding to create beautiful designs and shapes.",
            imageUrl = listOf(
                "https://example.com/images/origami1.jpg",
                "https://example.com/images/origami2.jpg"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),
        Hobby(
            name = "Pottery",
            description = "Craft functional and decorative ceramic objects with your hands.",
            imageUrl = listOf(
                "https://example.com/images/pottery1.jpg",
                "https://example.com/images/pottery2.jpg"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),

        // Food & Cooking
        Hobby(
            name = "Baking",
            description = "Experiment with pastries, cakes, and bread in your kitchen.",
            imageUrl = listOf(
                "https://example.com/images/baking1.jpg",
                "https://example.com/images/baking2.jpg"
            ),
            category = HobbyCategory.FOOD_COOKING
        ),
        Hobby(
            name = "Wine Tasting",
            description = "Refine your palate by sampling and learning about different wines.",
            imageUrl = listOf(
                "https://example.com/images/winetasting1.jpg",
                "https://example.com/images/winetasting2.jpg"
            ),
            category = HobbyCategory.FOOD_COOKING
        ),
        Hobby(
            name = "Fermentation",
            description = "Learn to create kombucha, sauerkraut, kimchi, and more fermented foods.",
            imageUrl = listOf(
                "https://example.com/images/fermentation1.jpg",
                "https://example.com/images/fermentation2.jpg"
            ),
            category = HobbyCategory.FOOD_COOKING
        ),

        // Outdoor Activities
        Hobby(
            name = "Rock Climbing",
            description = "Challenge yourself with indoor or outdoor rock climbing activities.",
            imageUrl = listOf(
                "https://example.com/images/rockclimbing1.jpg",
                "https://example.com/images/rockclimbing2.jpg"
            ),
            category = HobbyCategory.OUTDOOR_ACTIVITIES
        ),
        Hobby(
            name = "Bird Watching",
            description = "Discover the joys of observing and identifying bird species in nature.",
            imageUrl = listOf(
                "https://example.com/images/birdwatching1.jpg",
                "https://example.com/images/birdwatching2.jpg"
            ),
            category = HobbyCategory.OUTDOOR_ACTIVITIES
        ),
        Hobby(
            name = "Camping",
            description = "Connect with nature by spending nights under the stars.",
            imageUrl = listOf(
                "https://example.com/images/camping1.jpg",
                "https://example.com/images/camping2.jpg"
            ),
            category = HobbyCategory.OUTDOOR_ACTIVITIES
        ),

        // Fitness & Wellness
        Hobby(
            name = "Meditation",
            description = "Improve focus and relaxation through guided or independent meditation.",
            imageUrl = listOf(
                "https://example.com/images/meditation1.jpg",
                "https://example.com/images/meditation2.jpg"
            ),
            category = HobbyCategory.FITNESS_WELLNESS
        ),
        Hobby(
            name = "Martial Arts",
            description = "Learn self-defense and improve physical fitness through martial arts.",
            imageUrl = listOf(
                "https://example.com/images/martialarts1.jpg",
                "https://example.com/images/martialarts2.jpg"
            ),
            category = HobbyCategory.FITNESS_WELLNESS
        ),
        Hobby(
            name = "Dancing",
            description = "Express yourself through a variety of dance styles, from salsa to hip-hop.",
            imageUrl = listOf(
                "https://example.com/images/dancing1.jpg",
                "https://example.com/images/dancing2.jpg"
            ),
            category = HobbyCategory.FITNESS_WELLNESS
        ),

        // Technology & Science
        Hobby(
            name = "Robotics",
            description = "Build and program robots to perform various tasks.",
            imageUrl = listOf(
                "https://example.com/images/robotics1.jpg",
                "https://example.com/images/robotics2.jpg"
            ),
            category = HobbyCategory.TECHNOLOGY_SCIENCE
        ),
        Hobby(
            name = "Astronomy",
            description = "Explore the stars and planets with telescopes and star maps.",
            imageUrl = listOf(
                "https://example.com/images/astronomy1.jpg",
                "https://example.com/images/astronomy2.jpg"
            ),
            category = HobbyCategory.TECHNOLOGY_SCIENCE
        ),
        Hobby(
            name = "3D Printing",
            description = "Learn to design and print 3D models for personal or professional projects.",
            imageUrl = listOf(
                "https://example.com/images/3dprinting1.jpg",
                "https://example.com/images/3dprinting2.jpg"
            ),
            category = HobbyCategory.TECHNOLOGY_SCIENCE
        ),

        // Music & Performance
        Hobby(
            name = "Singing",
            description = "Improve your vocal skills and confidence by practicing singing.",
            imageUrl = listOf(
                "https://example.com/images/singing1.jpg",
                "https://example.com/images/singing2.jpg"
            ),
            category = HobbyCategory.MUSIC_PERFORMANCE
        ),
        Hobby(
            name = "Piano Playing",
            description = "Learn to play classical, jazz, or pop music on the piano.",
            imageUrl = listOf(
                "https://example.com/images/piano1.jpg",
                "https://example.com/images/piano2.jpg"
            ),
            category = HobbyCategory.MUSIC_PERFORMANCE
        ),
        Hobby(
            name = "Stand-Up Comedy",
            description = "Write and perform your own comedic material to make people laugh.",
            imageUrl = listOf(
                "https://example.com/images/standup1.jpg",
                "https://example.com/images/standup2.jpg"
            ),
            category = HobbyCategory.MUSIC_PERFORMANCE
        ),

        // Miscellaneous
        Hobby(
            name = "Journaling",
            description = "Document your thoughts, feelings, and experiences in a journal.",
            imageUrl = listOf(
                "https://example.com/images/journaling1.jpg",
                "https://example.com/images/journaling2.jpg"
            ),
            category = HobbyCategory.MISCELLANEOUS
        ),
        Hobby(
            name = "Magic Tricks",
            description = "Learn and perform amazing tricks to impress your friends and family.",
            imageUrl = listOf(
                "https://example.com/images/magictricks1.jpg",
                "https://example.com/images/magictricks2.jpg"
            ),
            category = HobbyCategory.MISCELLANEOUS
        ),
        Hobby(
            name = "Puzzle Solving",
            description = "Challenge your brain with jigsaw puzzles, crosswords, and riddles.",
            imageUrl = listOf(
                "https://example.com/images/puzzles1.jpg",
                "https://example.com/images/puzzles2.jpg"
            ),
            category = HobbyCategory.MISCELLANEOUS
        )
    )
}