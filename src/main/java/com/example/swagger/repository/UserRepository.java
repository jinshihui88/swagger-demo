package com.example.swagger.repository;

import com.example.swagger.dto.UserResponse;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存用户仓储，仅用于演示，保证 CRUD 行为自洽（新增可查、删除后查不到）。
 * 真实项目应替换为 JdbcTemplate / JPA / MyBatis 等持久层实现。
 */
@Repository
public class UserRepository {

    private final ConcurrentMap<Long, UserResponse> users = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1003L);

    public UserRepository() {
        users.put(1001L, new UserResponse(1001L, "张三", "zhangsan@example.com"));
        users.put(1002L, new UserResponse(1002L, "李四", "lisi@example.com"));
    }

    public List<UserResponse> findAll() {
        return users.values().stream()
                .sorted(Comparator.comparing(UserResponse::getId))
                .toList();
    }

    public Optional<UserResponse> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(id));
    }

    public UserResponse save(String username, String email) {
        long id = sequence.getAndIncrement();
        UserResponse user = new UserResponse(id, username, email);
        users.put(id, user);
        return user;
    }

    public boolean deleteById(Long id) {
        return id != null && users.remove(id) != null;
    }

    /** 按姓名模糊匹配 + 内存分页 */
    public List<UserResponse> search(String username, int pageNum, int pageSize) {
        return findAll().stream()
                .filter(user -> username == null || username.isBlank()
                        || user.getUsername().contains(username))
                .skip((long) (pageNum - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }
}
