package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.session.PostTreeResponse;
import com.mmlm.useradmin.dto.session.SessionCodeResponse;
import com.mmlm.useradmin.dto.user.UserSimpleResponse;
import com.mmlm.useradmin.entity.SysPost;
import com.mmlm.useradmin.entity.SysSessionCode;
import com.mmlm.useradmin.entity.SysUser;
import com.mmlm.useradmin.repository.SysPostRepository;
import com.mmlm.useradmin.repository.SysSessionCodeRepository;
import com.mmlm.useradmin.repository.SysUserPostRepository;
import com.mmlm.useradmin.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionCodeService {

    private final SysSessionCodeRepository sessionCodeRepository;
    private final SysPostRepository postRepository;
    private final SysUserRepository userRepository;
    private final SysUserPostRepository userPostRepository;

    public SessionCodeService(SysSessionCodeRepository sessionCodeRepository,
                             SysPostRepository postRepository,
                             SysUserRepository userRepository,
                             SysUserPostRepository userPostRepository) {
        this.sessionCodeRepository = sessionCodeRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.userPostRepository = userPostRepository;
    }

    // 树结构：获取所有岗位及其用户
    public List<PostTreeResponse> getPostTree() {
        List<SysPost> posts = postRepository.findAll();
        return posts.stream().map(post -> {
            PostTreeResponse resp = new PostTreeResponse();
            resp.setPostId(post.getId());
            resp.setPostName(post.getName());
            resp.setLeaderUserId(post.getLeaderUserId());

            // 获取岗位下的用户
            List<Long> userIds = userPostRepository.findByPostIdIn(List.of(post.getId()))
                    .stream().map(up -> up.getUserId()).collect(Collectors.toList());
            List<UserSimpleResponse> users = new ArrayList<>();
            if (!userIds.isEmpty()) {
                userRepository.findAllById(userIds).forEach(user -> {
                    UserSimpleResponse ur = new UserSimpleResponse();
                    ur.setId(user.getId());
                    ur.setUsername(user.getUsername());
                    ur.setNickname(user.getNickname());
                    ur.setPhone(user.getPhone());
                    ur.setStatus(user.getStatus());
                    ur.setPostId(post.getId());
                    ur.setPostName(post.getName());
                    users.add(ur);
                });
            }
            resp.setUsers(users);

            // 获取该岗位的会话编码
            List<SessionCodeResponse> codes = sessionCodeRepository.findByPostId(post.getId())
                    .stream().map(this::toResponse).collect(Collectors.toList());
            resp.setSessionCodes(codes);

            return resp;
        }).collect(Collectors.toList());
    }

    // 根据ID获取会话编码
    public SessionCodeResponse getById(Long id) {
        List<SysSessionCode> code = sessionCodeRepository.findByUserId(id);
//                .orElseThrow(() -> new BusinessException("会话编码不存在"));
        code.sort(Comparator.comparing(SysSessionCode::getUpdatedAt));
        if (code == null || code.size() <= 0) {
            new BusinessException("会话编码不存在");
        }
        return toResponse(code.get(0));
    }

    // 生成会话编码
    @Transactional
    public SessionCodeResponse generateCode(Long userId, Long postId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        List<SysSessionCode> existingCodes = sessionCodeRepository.findByUserId(userId);
        if (existingCodes != null && !existingCodes.isEmpty()) {
            throw new BusinessException("该用户已有会话编码，无需重复生成");
        }

        SysPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("岗位不存在"));

        // 生成唯一编码
        String code = generateUniqueCode();

        LocalDateTime now = LocalDateTime.now();
        SysSessionCode sessionCode = new SysSessionCode();
        sessionCode.setUserId(userId);
        sessionCode.setCode(code);
        sessionCode.setPostId(postId);
        sessionCode.setLeaderUserId(post.getLeaderUserId()); // 设置当前负责人id
        sessionCode.setIsActive(1);
        sessionCode.setCreatedAt(now);
        sessionCode.setUpdatedAt(now);
        sessionCodeRepository.save(sessionCode);

        // 如果是该岗位第一个编码，设置为负责人
        if (post.getLeaderUserId() == null) {
            post.setLeaderUserId(userId);
            post.setUpdatedAt(now);
            postRepository.save(post);
        }

        return toResponse(sessionCode);
    }

    // 修改会话编码指向的用户
    @Transactional
    public SessionCodeResponse updateCodeUser(Long id, Long userId) {
        SysSessionCode code = sessionCodeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("会话编码不存在"));
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }

        // 记录转移备注
        String oldUserName = "";
        if (code.getUserId() != null) {
            SysUser oldUser = userRepository.findById(code.getUserId()).orElse(null);
            if (oldUser != null) {
                oldUserName = oldUser.getNickname() != null ? oldUser.getNickname() : oldUser.getUsername();
            }
        }
        SysUser newUser = userRepository.findById(userId).orElse(null);
        String newUserName = newUser != null ? (newUser.getNickname() != null ? newUser.getNickname() : newUser.getUsername()) : String.valueOf(userId);

        String transferNote = "编码从 " + oldUserName + " 转移到 " + newUserName;
        String oldRemark = code.getRemark();
        if (oldRemark == null || oldRemark.isEmpty()) {
            code.setRemark(transferNote);
        } else {
            code.setRemark(oldRemark + "；" + transferNote);
        }

        code.setUserId(userId);
        code.setUpdatedAt(LocalDateTime.now());
        sessionCodeRepository.save(code);
        return toResponse(code);
    }

    // 设置负责人
    @Transactional
    public void setLeader(Long postId, Long userId) {
        SysPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("岗位不存在"));
        // 验证用户是否属于该岗位
        boolean userInPost = !userPostRepository.findByPostIdIn(List.of(postId)).stream()
                .filter(up -> up.getUserId().equals(userId)).collect(Collectors.toList()).isEmpty();
        if (!userInPost) {
            throw new BusinessException("该用户不属于此岗位");
        }
        post.setLeaderUserId(userId);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        // 更新该岗位所有编码的leader_user_id为新的负责人
        List<SysSessionCode> codes = sessionCodeRepository.findByPostId(postId);
        LocalDateTime now = LocalDateTime.now();
        codes.forEach(code -> {
            code.setLeaderUserId(userId);
            code.setUpdatedAt(now);
        });
        sessionCodeRepository.saveAll(codes);
    }

    // 删除会话编码
    @Transactional
    public void deleteCode(Long id) {
        if (!sessionCodeRepository.existsById(id)) {
            throw new BusinessException("会话编码不存在");
        }
        sessionCodeRepository.deleteById(id);
    }

    private String generateUniqueCode() {
        String code;
        int retry = 0;
        do {
            code = "SC" + System.currentTimeMillis() + (retry++);
        } while (sessionCodeRepository.existsByCode(code) && retry < 10);
        return code;
    }

    private SessionCodeResponse toResponse(SysSessionCode code) {
        SessionCodeResponse resp = new SessionCodeResponse();
        resp.setId(code.getId());
        resp.setUserId(code.getUserId());
        resp.setLeaderUserId(code.getLeaderUserId());
        SysUser user = userRepository.findById(code.getUserId()).orElse(null);
        if (user != null) {
            resp.setUserName(user.getUsername());
            resp.setUserNickname(user.getNickname());
        }
        resp.setCode(code.getCode());
        resp.setPostId(code.getPostId());
        SysPost post = postRepository.findById(code.getPostId()).orElse(null);
        if (post != null) resp.setPostName(post.getName());
        resp.setIsActive(code.getIsActive());
        resp.setRemark(code.getRemark());
        resp.setCreatedAt(code.getCreatedAt());
        resp.setUpdatedAt(code.getUpdatedAt());
        return resp;
    }
}
