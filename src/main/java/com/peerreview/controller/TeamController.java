package com.peerreview.controller;

import com.peerreview.dto.AppDTOs.ChatMessageRequest;
import com.peerreview.dto.AppDTOs.ChatMessageResponse;
import com.peerreview.dto.AppDTOs.TeamRequest;
import com.peerreview.dto.AppDTOs.TeamResponse;
import com.peerreview.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // GET /api/teams
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAll() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    // POST /api/teams — admin creates team with optional member limit
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody TeamRequest req) {
        return ResponseEntity.ok(teamService.createTeam(req));
    }

    // DELETE /api/teams/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok("Team deleted successfully");
    }

    // NEW: POST /api/teams/{id}/join — student joins a team
    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<TeamResponse> joinTeam(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(teamService.joinTeam(id, principal.getName()));
    }

    // NEW: POST /api/teams/{id}/leave — student leaves a team
    @PostMapping("/{id}/leave")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<TeamResponse> leaveTeam(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(teamService.leaveTeam(id, principal.getName()));
    }

    // GET /api/teams/{id}/messages
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long id) {
        return ResponseEntity.ok(teamService.getMessages(id));
    }

    // POST /api/teams/{id}/messages
    @PostMapping("/{id}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody ChatMessageRequest req,
            Principal principal) {
        return ResponseEntity.ok(teamService.sendMessage(id, req, principal.getName()));
    }
}
