class UrlMappings {

    static mappings = {
        "/reading/$id?"(resource: "reading")

        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(view: "/index")
        "500"(view: '/error')
    }
}
