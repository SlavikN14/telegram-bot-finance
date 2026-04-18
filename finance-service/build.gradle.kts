tasks.register("test") {
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("test") })
}