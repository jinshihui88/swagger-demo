package com.example.swagger.controller.user;

import com.example.swagger.dto.UserCreateRequest;
import com.example.swagger.dto.UserResponse;
import com.example.swagger.exception.ResourceNotFoundException;
import com.example.swagger.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户新增、查询、搜索、上传和删除接口")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "查询用户列表", description = "获取系统中的用户列表")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public List<UserResponse> list() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public UserResponse getById(
            @Parameter(description = "用户ID", example = "1001")
            @PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建用户", description = "根据请求参数创建用户")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数不合法")
    })
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return userRepository.save(request.getUsername(), request.getEmail());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "删除用户")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public void delete(
            @Parameter(description = "用户ID", example = "1001")
            @PathVariable Long id) {
        if (!userRepository.deleteById(id)) {
            throw new ResourceNotFoundException("用户", id);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "按姓名模糊匹配并分页，演示 @Parameter 查询参数")
    public List<UserResponse> search(
            @Parameter(description = "用户姓名", example = "张三")
            @RequestParam(required = false) String username,

            @Parameter(description = "当前页码（从 1 开始）", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,

            @Parameter(description = "每页数量（1-100）", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("分页参数不合法：pageNum>=1，1<=pageSize<=100");
        }
        return userRepository.search(username, pageNum, pageSize);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "演示 multipart/form-data 文件上传参数的文档生成")
    public String upload(
            @Parameter(description = "待上传文件")
            @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        return file.getOriginalFilename();
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息", description = "演示 Swagger UI 的 Authorize 携带 JWT")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public UserResponse profile() {
        return new UserResponse(1001L, "张三", "zhangsan@example.com");
    }
}
