package com.peerreview.service;

import com.peerreview.dto.AppDTOs.ChatMessageRequest;
import com.peerreview.dto.AppDTOs.ChatMessageResponse;
import com.peerreview.dto.AppDTOs.TeamRequest;
import com.peerreview.dto.AppDTOs.TeamResponse;
import com.peerreview.entity.ChatMessage;
import com.peerreview.entity.Team;
import com.peerreview.entity.User;
import com.peerreview.repository.ChatMessageRepository;
import com.peerreview.repository.TeamRepository;
import com.peerreview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository,
                       ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository) {
        this.teamRepository        = teamRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository        = userRepository;
    }

    // Get all teams with correct member counts
    public List<TeamResponse> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        List<TeamResponse> result = new ArrayList<>();
        for (Team t : teams) {
            result.add(toTeamResponse(t));
        }
        return result;
    }

    // Admin creates a team with optional member limit
    public TeamResponse createTeam(TeamRequest req) {
        Team team = new Team();
        team.setName(req.getName());
        team.setMaxMembers(req.getMaxMembers()); // NEW: store limit
        Team saved = teamRepository.save(team);
        return toTeamResponse(saved);
    }

 // Admin deletes a team
    public void deleteTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException("Team not found");
        }
        // Delete chat messages first to avoid foreign key constraint
        chatMessageRepository.deleteById(teamId);
        teamRepository.deleteById(teamId);
    }
    // NEW: Student joins a team (respects member limit)
    public TeamResponse joinTeam(Long teamId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Check if already a member
        if (team.getMembers().contains(student)) {
            return toTeamResponse(team); // already joined, just return
        }

        // Enforce member limit if set
        int maxMembers = team.getMaxMembers() != null ? team.getMaxMembers() : 0;
        if (maxMembers > 0 && team.getMembers().size() >= maxMembers) {
            throw new RuntimeException("Team is full! Maximum " + maxMembers + " members allowed.");
        }

        team.getMembers().add(student);
        Team saved = teamRepository.save(team);
        return toTeamResponse(saved);
    }

    // NEW: Student leaves a team
    public TeamResponse leaveTeam(Long teamId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        team.getMembers().remove(student);
        Team saved = teamRepository.save(team);
        return toTeamResponse(saved);
    }

    // Get all chat messages for a team
    public List<ChatMessageResponse> getMessages(Long teamId) {
        List<ChatMessage> messages = chatMessageRepository.findByTeamIdOrderBySentAtAsc(teamId);
        List<ChatMessageResponse> result = new ArrayList<>();
        for (ChatMessage m : messages) {
            result.add(toChatResponse(m));
        }
        return result;
    }

    // Student sends a chat message — also auto-joins team on first message
    public ChatMessageResponse sendMessage(Long teamId,
                                            ChatMessageRequest req,
                                            String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Auto-join team when sending first message
        if (!team.getMembers().contains(sender)) {
            int maxMembers = team.getMaxMembers();
            if (maxMembers > 0 && team.getMembers().size() >= maxMembers) {
                throw new RuntimeException("Team is full! Maximum " + maxMembers + " members allowed.");
            }
            team.getMembers().add(sender);
            teamRepository.save(team);
        }

        ChatMessage message = new ChatMessage();
        message.setTeam(team);
        message.setSender(sender);
        message.setText(req.getText());

        ChatMessage saved = chatMessageRepository.save(message);
        return toChatResponse(saved);
    }

    private TeamResponse toTeamResponse(Team t) {
        TeamResponse res = new TeamResponse();
        res.setId(t.getId());
        res.setName(t.getName());
        res.setMemberCount(t.getMembers().size()); // now correctly counts from DB
        res.setMaxMembers(t.getMaxMembers());       // NEW: include limit
        return res;
    }

    private ChatMessageResponse toChatResponse(ChatMessage m) {
        ChatMessageResponse res = new ChatMessageResponse();
        res.setId(m.getId());
        res.setSender(m.getSender().getName());
        res.setText(m.getText());
        res.setSentAt(m.getSentAt());
        return res;
    }
}
