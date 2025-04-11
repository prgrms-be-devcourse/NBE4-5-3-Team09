package com.coing.global.security.resolver

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class CustomAuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
    baseUri: String
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver = DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, baseUri)

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val req = defaultResolver.resolve(request)
        return customize(req, request)
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        val req = defaultResolver.resolve(request, clientRegistrationId)
        return customize(req, request)
    }

    private fun customize(
        req: OAuth2AuthorizationRequest?,
        request: HttpServletRequest
    ): OAuth2AuthorizationRequest? {
        if (req == null) return null

        val purpose = request.getParameter("purpose") ?: "login"
        val additionalParameters = HashMap(req.additionalParameters)
        additionalParameters["state"] = purpose

        return OAuth2AuthorizationRequest.from(req)
            .state(purpose)
            .additionalParameters(additionalParameters)
            .build()
    }
}
