package org.example.authapiserver.controller

import org.example.authapiserver.dto.GetUserInfoResponseFromServer
import org.example.authapiserver.service.CognitoUserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/user")
class UserInfoController(
    private val cognitoUserService: CognitoUserService
) {
    @GetMapping("/{userId}")
    fun getUserInfo(@PathVariable userId: UUID): ResponseEntity<GetUserInfoResponseFromServer> {
        val result = cognitoUserService.getUserInfo(userId)
        return ResponseEntity.ok(GetUserInfoResponseFromServer.of(result))
    }
}
