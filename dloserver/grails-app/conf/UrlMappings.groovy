class UrlMappings {

    static mappings = {
        "/readings/$id?"(resource: "readings")
        "/receipts/$id?"(resource: "receipts")
        "/deliveries/$id?"(resource: "deliveries")
        "/healthcheck/$id?"(resource: "healthcheck")

        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(view: "/index")
        "500"(view: '/error')
    }
}
