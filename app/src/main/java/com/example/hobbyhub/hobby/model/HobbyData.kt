package com.example.hobbyhub.hobby.model

object HobbyData {
    val hobbies  = listOf(
        Hobby(
            name = "Photography",
            description = "Capture beautiful moments and learn the art of photography.",
            imageUrl = listOf(
                "https://www.adorama.com/alc/wp-content/uploads/2021/04/photography-camera-learning-feature.jpg",
                "https://lisaabangalore.com/wp-content/uploads/2023/03/Importance-of-Photography-Thumbnail.png"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),
        Hobby(
            name = "Painting",
            description = "Unleash your creativity with colors and brushes.",
            imageUrl = listOf(
                "https://mymodernmet.com/wp/wp-content/uploads/2018/05/painting-ideas-3-1.jpg",
                "https://finearttutorials.com/wp-content/uploads/2022/06/types-of-painting.png"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),
        Hobby(
            name = "Cooking",
            description = "Learn to cook delicious meals and master culinary techniques.",
            imageUrl = listOf(
                "https://info.ehl.edu/hubfs/Home-cooking.jpg",
                "https://www.njlifestylemag.com/content/images/size/w1304/2023/12/AdobeStock_267334945.jpeg"
            ),
            category = HobbyCategory.FOOD_COOKING
        ),
        Hobby(
            name = "Hiking",
            description = "Explore nature and embark on thrilling hiking adventures.",
            imageUrl = listOf(
                "https://www.travelyukon.com/themes/custom/cossette/src/assets/images//quiz/QUESTION1/Answer2.jpg",
                "https://i.natgeofe.com/n/7afda449-1780-4938-8342-2abe32326c86/Montblanchike_4x3.jpg"
            ),
            category = HobbyCategory.OUTDOOR_ACTIVITIES
        ),
        Hobby(
            name = "Yoga",
            description = "Improve your flexibility and mindfulness through yoga.",
            imageUrl = listOf(
                "https://www.ekhartyoga.com/media/image/articles/Laia_Bove_Mermaid-pose.jpg",
                "https://media.post.rvohealth.io/wp-content/uploads/2024/09/multiracial-group-practicing-yoga-in-studio-732x549-thumbnail.jpg"
            ),
            category = HobbyCategory.FITNESS_WELLNESS
        ),
        Hobby(
            name = "Coding",
            description = "Learn to create software and apps with coding skills.",
            imageUrl = listOf(
                "https://permutable.ai/wp-content/uploads/2024/03/19301.jpg",
                "https://www.goodwin.edu/enews/wp-content/uploads/2023/07/is-mbc-a-good-career-tiny-scaled.jpg"
            ),
            category = HobbyCategory.TECHNOLOGY_SCIENCE
        ),
        Hobby(
            name = "Playing Guitar",
            description = "Learn to play chords, melodies, and songs on the guitar.",
            imageUrl = listOf(
                "https://online.berklee.edu/takenote/wp-content/uploads/2021/01/acoustic_guitar_techniques_article_image_2021.jpg",
                "https://t4.ftcdn.net/jpg/03/29/75/71/360_F_329757181_6Xqh8yP73Cy4SR41k2DQTTYTvsasl90h.jpg"
            ),
            category = HobbyCategory.MUSIC_PERFORMANCE
        ),
        Hobby(
            name = "Gardening",
            description = "Cultivate plants and flowers while connecting with nature.",
            imageUrl = listOf(
                "https://t3.ftcdn.net/jpg/00/83/96/70/360_F_83967045_ctnjfJ3gzCgo1HKjsUYHb72IKpUQovaG.jpg",
                "https://cdn.mos.cms.futurecdn.net/JvbeUXjvJAdLnMUAM9UtgQ.jpg"
            ),
            category = HobbyCategory.MISCELLANEOUS
        ),
        // Arts & Crafts
        Hobby(
            name = "Knitting",
            description = "Create handmade sweaters, scarves, and other items using knitting needles and yarn.",
            imageUrl = listOf(
                "https://brownsheep.com/wp-content/uploads/2017/03/knittingyarn.jpg",
                "https://knit1purl1.ie/wp-content/uploads/2023/05/teengirl-knitting-at-home-handmade-and-hobby-.jpg"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),
        Hobby(
            name = "Origami",
            description = "Learn the art of paper folding to create beautiful designs and shapes.",
            imageUrl = listOf(
                "https://hey.georgie.nu/wp-content/uploads/origami.jpg",
                "https://st4.depositphotos.com/3994509/26359/i/450/depositphotos_263599838-stock-photo-origami-paper-swans.jpg"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),
        Hobby(
            name = "Pottery",
            description = "Craft functional and decorative ceramic objects with your hands.",
            imageUrl = listOf(
                "https://storage.googleapis.com/gweb-uniblog-publish-prod/images/gopi3.width-1300.jpg",
                "https://shop.vitcas.com/media/amasty/blog/cache/P/o/1000/690/Pottery-craft-ceramics.jpg"
            ),
            category = HobbyCategory.ARTS_CRAFTS
        ),

        // Food & Cooking
        Hobby(
            name = "Baking",
            description = "Experiment with pastries, cakes, and bread in your kitchen.",
            imageUrl = listOf(
                "https://whitecaps.in/storage/2023/04/IMG_0868-1.jpg",
                "https://ik.imagekit.io/munchery/blog/tr:w-768/how-to-master-basics-of-baking-at-home.jpeg"
            ),
            category = HobbyCategory.FOOD_COOKING
        ),
        Hobby(
            name = "Wine Tasting",
            description = "Refine your palate by sampling and learning about different wines.",
            imageUrl = listOf(
                "https://www.foodandwine.com/thmb/tvi94UKEgOZvnjVpZdgrt6gEaLo=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/What-Are-Tasting-Notes-and-How-to-Give-Them-FT-BLOG0623-c61918bcbfd04a52af0b96957cf31821.jpg",
                "https://www.winetraveler.com/wp-content/uploads/2018/03/wine-tasting-instructions-step-by-step-winetraveler.jpg"
            ),
            category = HobbyCategory.FOOD_COOKING
        ),
        Hobby(
            name = "Fermentation",
            description = "Learn to create kombucha, sauerkraut, kimchi, and more fermented foods.",
            imageUrl = listOf(
                "https://post.healthline.com/wp-content/uploads/2019/06/lacto-fermentation-fermented-pickled-jars-pickling-1296x728-header.jpg",
                "https://www.notechmagazine.com/wp-content/uploads/2018/07/P1022944.jpg"
            ),
            category = HobbyCategory.FOOD_COOKING
        ),

        // Outdoor Activities
        Hobby(
            name = "Rock Climbing",
            description = "Challenge yourself with indoor or outdoor rock climbing activities.",
            imageUrl = listOf(
                "https://www.urban-xtreme.com.au/wp-content/uploads/2020/09/UX_WEBSITE_IMAGES_CLIMBERS_YOUTH-scaled.jpg",
                "https://www.wfla.com/wp-content/uploads/sites/71/2023/04/GettyImages-828532530.jpg?w=2560&h=1440&crop=1"
            ),
            category = HobbyCategory.OUTDOOR_ACTIVITIES
        ),
        Hobby(
            name = "Bird Watching",
            description = "Discover the joys of observing and identifying bird species in nature.",
            imageUrl = listOf(
                "https://i.cbc.ca/1.4842389.1538143978!/fileImage/httpImage/bird-watching-binoculars.jpg",
                "https://media.cntraveler.com/photos/60393d70ed1e37b700b6145f/4:3/w_2220,h_1665,c_limit/F47P11-2.jpg"
            ),
            category = HobbyCategory.OUTDOOR_ACTIVITIES
        ),
        Hobby(
            name = "Camping",
            description = "Connect with nature by spending nights under the stars.",
            imageUrl = listOf(
                "https://i.natgeofe.com/n/84b65902-fec3-4fb0-91fa-e16df38a9b8c/NationalGeographic_2692996_4x3.jpg",
                "https://settocamp.com/wp-content/uploads/2021/06/Benefits-of-camping.jpg"
            ),
            category = HobbyCategory.OUTDOOR_ACTIVITIES
        ),

        // Fitness & Wellness
        Hobby(
            name = "Meditation",
            description = "Improve focus and relaxation through guided or independent meditation.",
            imageUrl = listOf(
                "https://www.verywellmind.com/thmb/lmjACRUlHuZcHijdfo5dYplWQro=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/GettyImages-539661087-58d2e5e65f9b5846830df9aa.jpg",
                "https://regionalneurological.com/wp-content/uploads/2019/05/AdobeStock_141860204.jpeg"
            ),
            category = HobbyCategory.FITNESS_WELLNESS
        ),
        Hobby(
            name = "Martial Arts",
            description = "Learn self-defense and improve physical fitness through martial arts.",
            imageUrl = listOf(
                "https://actionkarate.net/wp-content/uploads/2024/01/martial-arts.jpg",
                "https://th-thumbnailer.cdn-si-edu.com/1_3MMNqGawCFA8jAtQ-vEL4SwzI=/1000x750/filters:no_upscale()/https://tf-cmsv2-smithsonianmag-media.s3.amazonaws.com/filer/36/a4/36a4e18b-e200-450d-851b-1914b38546f7/mobile_opener.jpg"
            ),
            category = HobbyCategory.FITNESS_WELLNESS
        ),
        Hobby(
            name = "Dancing",
            description = "Express yourself through a variety of dance styles, from salsa to hip-hop.",
            imageUrl = listOf(
                "https://www.ibsafoundation.org/hubfs/Asset/CulturaeSalute/Insight/shutterstock_1823945150.jpg",
                "https://thedancecentre.ca/wp-content/uploads/2021/04/Goh-Ballet.jpg"
            ),
            category = HobbyCategory.FITNESS_WELLNESS
        ),

        // Technology & Science
        Hobby(
            name = "Robotics",
            description = "Build and program robots to perform various tasks.",
            imageUrl = listOf(
                "https://globalfintechseries.com/wp-content/uploads/0_B1nMAW5C3-S-W0a8.jpeg",
                "https://greatscience.com/sites/default/files/Website%20Banner%20%20-%20Robotics.png"
            ),
            category = HobbyCategory.TECHNOLOGY_SCIENCE
        ),
        Hobby(
            name = "Astronomy",
            description = "Explore the stars and planets with telescopes and star maps.",
            imageUrl = listOf(
                "https://sociable.co/wp-content/uploads/2019/06/galaxy-telescope.jpg",
                "https://cdn.mos.cms.futurecdn.net/UqKrHFjGaYQ3Fi9rACt6S9.jpg"
            ),
            category = HobbyCategory.TECHNOLOGY_SCIENCE
        ),
        Hobby(
            name = "3D Printing",
            description = "Learn to design and print 3D models for personal or professional projects.",
            imageUrl = listOf(
                "https://hlhrapid.com/wp-content/uploads/2022/11/fused-deposition-modeling.jpg",
                "https://www.ulprospector.com/knowledge/media/2019/04/3D-printing-iStock-492679832-600x400.jpg"
            ),
            category = HobbyCategory.TECHNOLOGY_SCIENCE
        ),

        // Music & Performance
        Hobby(
            name = "Singing",
            description = "Improve your vocal skills and confidence by practicing singing.",
            imageUrl = listOf(
                "https://blog.mugafi.com/wp-content/uploads/2021/05/young-pretty-woman-happy-motivated-singing-song-with-microphone-presenting-event-having-party-enjoy-moment-scaled.jpg",
                "https://sonomusic.com.au/wp-content/uploads/2019/03/Performance-Showcase-Vocal-En.png"
            ),
            category = HobbyCategory.MUSIC_PERFORMANCE
        ),
        Hobby(
            name = "Piano Playing",
            description = "Learn to play classical, jazz, or pop music on the piano.",
            imageUrl = listOf(
                "https://www.pianoforte.com.au/wp-content/uploads/order_3691_-_image_11.jpg",
                "https://enthu.com/blog/wp-content/uploads/2022/02/piano-g47f7a77c5_1920-1024x768.jpg"
            ),
            category = HobbyCategory.MUSIC_PERFORMANCE
        ),
        Hobby(
            name = "Stand-Up Comedy",
            description = "Write and perform your own comedic material to make people laugh.",
            imageUrl = listOf(
                "https://www.teacheracademy.eu/wp-content/uploads/2023/01/Stand-Up-Comedian.jpg",
                "https://comedycarnival.co.uk/wp-content/uploads/2022/04/Leicester-Square-Comedy-Club-2-e1649097245592.jpg"
            ),
            category = HobbyCategory.MUSIC_PERFORMANCE
        ),

        // Miscellaneous
        Hobby(
            name = "Journaling",
            description = "Document your thoughts, feelings, and experiences in a journal.",
            imageUrl = listOf(
                "https://images.squarespace-cdn.com/content/v1/56c8cc2240261dffb291d622/1704077267720-ZRIN8WQV5LPLXOB5BH4G/how-journaling-helped-my-mental-health-and-how-it-didnt-sarica-studio-blog.jpg",
                "https://www.betterup.com/hubfs/Google%20Drive%20Integration/Delivery%20URL%20-%20BetterUp%20-%20how%20to%20start%20journaling%20%5BARTICLE%5D-1.png"
            ),
            category = HobbyCategory.MISCELLANEOUS
        ),
        Hobby(
            name = "Magic Tricks",
            description = "Learn and perform amazing tricks to impress your friends and family.",
            imageUrl = listOf(
                "https://www.dynamicevents.ie/static/a547ca20f0abbab54a162b4f9da8b56d/fb60a/online_magic_show_featured.jpg",
                "https://www.magicshow.sk/images/galeria/kuzla_pre_dospelych/peter_sestak_kuzla_pre_dospleych_5.jpg"
            ),
            category = HobbyCategory.MISCELLANEOUS
        ),
        Hobby(
            name = "Puzzle Solving",
            description = "Challenge your brain with jigsaw puzzles, crosswords, and riddles.",
            imageUrl = listOf(
                "https://thumbs.dreamstime.com/b/group-people-doing-puzzle-table-151311294.jpg",
                "https://m.media-amazon.com/images/I/81LUxn8qr9L._AC_UF350,350_QL80_.jpg"
            ),
            category = HobbyCategory.MISCELLANEOUS
        )
    )
}